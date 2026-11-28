<#
.SYNOPSIS
    Migra en local el esquema de cada clínica activa del catálogo con el
    historial de `database/migrations/tenant`.

.DESCRIPTION
    Es el runner local de esquemas de clínica (docs/Despliegue_y_operacion.md §5).
    No crea datos ni usuarios: para eso está `sembrar-demo.ps1`, que lo llama.

    Para cada clínica con estado ACTIVA:
      1. La registra en `catalogo.migracion_tenant` si no estaba.
      2. Corre Flyway con el servicio `migraciones-tenant` del compose. Flyway no
         repite lo ya aplicado, así que también pone al día un esquema viejo.
      3. Deja el estado `aplicada` con su versión, o `fallida` si Flyway falla.

    Se puede repetir. Antes hay que tener el stack arriba y el catálogo migrado:
        docker compose -f infrastructure\docker\compose.yaml up -d
        docker compose -f infrastructure\docker\compose.yaml run --rm migraciones

.EXAMPLE
    .\scripts\migrar-esquemas.ps1
#>
[CmdletBinding()]
param(
    [string]$EnvFile = '',
    [string]$ComposeFile = ''
)

$ErrorActionPreference = 'Stop'
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }
$OutputEncoding = New-Object System.Text.UTF8Encoding($false)

$resultados = New-Object System.Collections.Generic.List[object]
function Add-Resultado([string]$Paso, [string]$Estado, [string]$Detalle) {
    $resultados.Add([pscustomobject]@{ Paso = $Paso; Estado = $Estado; Detalle = $Detalle })
    $color = if ($Estado -eq 'OK') { 'Green' } else { 'Red' }
    Write-Host ("[{0}] {1} — {2}" -f $Estado, $Paso, $Detalle) -ForegroundColor $color
}

# En Windows PowerShell 5.1, $PSScriptRoot llega vacío a los valores por defecto
# de param(); por eso las rutas se resuelven aquí, a partir de la raíz del repo.
$raiz = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
if (-not $EnvFile) { $EnvFile = [IO.Path]::Combine($raiz, 'infrastructure', 'docker', '.env') }
if (-not $ComposeFile) { $ComposeFile = [IO.Path]::Combine($raiz, 'infrastructure', 'docker', 'compose.yaml') }
if (-not (Test-Path $EnvFile)) { throw "No encuentro $EnvFile. Copia .env.example a .env primero." }
$entorno = @{}
foreach ($linea in Get-Content $EnvFile -Encoding UTF8) {
    if ($linea -match '^\s*([A-Z][A-Z0-9_]*)=(.*)$') { $entorno[$Matches[1]] = $Matches[2].Trim() }
}

function Invoke-Psql([string]$Sql) {
    $usuario = $entorno['VETCAT_DB_USER']; $base = $entorno['VETCAT_DB']
    $salida = $Sql | & docker compose -f $ComposeFile --env-file $EnvFile exec -T postgres `
        psql -U $usuario -d $base -v ON_ERROR_STOP=1 -tA -q 2>&1
    if ($LASTEXITCODE -ne 0) { throw "psql falló: $salida" }
    return @($salida | ForEach-Object { "$_".Trim() } | Where-Object { $_ -ne '' })
}

try {
    $esquemas = Invoke-Psql @"
INSERT INTO catalogo.migracion_tenant (esquema)
SELECT esquema FROM catalogo.clinica
WHERE estado = 'ACTIVA'
ON CONFLICT (esquema) DO NOTHING;
SELECT esquema FROM catalogo.clinica WHERE estado = 'ACTIVA' ORDER BY id;
"@
} catch {
    Add-Resultado 'Esquemas' 'FALLA' $_.Exception.Message
    $esquemas = @()
}

foreach ($esquema in $esquemas) {
    # Cada esquema va en su propio try: si falla el psql que registra su estado,
    # se cuenta como FALLA y se sigue con los demás; el resumen siempre sale.
    try {
        if ($esquema -notmatch '^clinica_\d{3,}$') { throw 'nombre inesperado' }
        # docker compose escribe su progreso en stderr; con 'Stop', Windows
        # PowerShell lo tomaría como error. Aquí se decide por el código de salida.
        $ErrorActionPreference = 'Continue'
        $salida = & docker compose -f $ComposeFile --env-file $EnvFile run --rm -T `
            -e "FLYWAY_SCHEMAS=$esquema,ext" -e "FLYWAY_DEFAULT_SCHEMA=$esquema" migraciones-tenant 2>&1
        $codigo = $LASTEXITCODE
        $ErrorActionPreference = 'Stop'
        if ($codigo -ne 0) {
            Invoke-Psql "UPDATE catalogo.migracion_tenant SET estado = 'fallida', ultimo_intento = now(), actualizado_en = now() WHERE esquema = '$esquema';" | Out-Null
            throw ((@($salida) | Select-Object -Last 5) -join ' | ')
        }
        $version = Invoke-Psql @"
UPDATE catalogo.migracion_tenant
SET estado = 'aplicada', ultimo_intento = now(), actualizado_en = now(),
    version_aplicada = (SELECT version FROM $esquema.flyway_schema_history
                        WHERE success AND version IS NOT NULL
                        ORDER BY installed_rank DESC LIMIT 1)
WHERE esquema = '$esquema'
RETURNING version_aplicada;
"@ | Select-Object -Last 1
        Add-Resultado "Esquema $esquema" 'OK' "aplicada, versión $version"
    } catch {
        $ErrorActionPreference = 'Stop'
        Add-Resultado "Esquema $esquema" 'FALLA' $_.Exception.Message
    }
}

$fallas = @($resultados | Where-Object { $_.Estado -ne 'OK' }).Count
if ($fallas) { exit 1 } else { exit 0 }
