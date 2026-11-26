<#
.SYNOPSIS
    Siembra los datos de la demo del corte (#221) en el entorno local: dos
    clínicas con su esquema migrado y un usuario por cada papel que se enseña.

.DESCRIPTION
    Con el stack levantado (`docker compose up -d`) y el catálogo migrado
    (`docker compose run --rm migraciones`), hace tres cosas y se puede repetir
    sin duplicar nada:

      1. Crea las clínicas «Veterinaria Norte» y «Veterinaria Sur» si no existen.
      2. Migra el esquema de cada clínica del catálogo con el historial de
         `database/migrations/tenant` (servicio `migraciones-tenant`) y lo marca
         `aplicada` en `catalogo.migracion_tenant`. También repara cualquier
         clínica creada antes a mano, como la de `verificar-h1b.ps1`.
      3. Crea los usuarios en el realm `vetcat` y su pertenencia en
         `catalogo.usuario_clinica`:

           dos.clinicas@vetcat.dev   clinica_admin en Norte y veterinario en Sur
           recepcion@vetcat.dev      recepcionista en Norte
           cliente@vetcat.dev        cliente en Norte

    Con eso la demo tiene el paso 3 (elegir entre dos clínicas), el 6 (la ficha
    de Norte con el token de Sur da 404) y el 7 (el cliente recibe 403).

    Solo para local: no toca el export del realm ni corre contra un Keycloak que
    no esté en localhost. No necesita la API levantada.

.PARAMETER Contrasena
    La misma para los tres usuarios. Solo local.

.EXAMPLE
    .\scripts\sembrar-demo.ps1
#>
[CmdletBinding()]
param(
    [string]$EnvFile = '',
    [string]$ComposeFile = '',
    [string]$KeycloakUrl = '',
    [string]$Contrasena = 'Demo-solo-local-2026'
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
function Show-Resumen {
    $fallas = @($resultados | Where-Object { $_.Estado -ne 'OK' }).Count
    $ok = @($resultados | Where-Object { $_.Estado -eq 'OK' }).Count
    Write-Host ''
    Write-Host ("Resumen: {0} OK, {1} fallas." -f $ok, $fallas) -ForegroundColor $(if ($fallas) { 'Red' } else { 'Green' })
    if ($fallas) { exit 1 } else { exit 0 }
}

# --- Configuración desde .env ---------------------------------------------------
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
if (-not $KeycloakUrl) {
    $puerto = if ($entorno['KEYCLOAK_PORT']) { $entorno['KEYCLOAK_PORT'] } else { '8080' }
    $KeycloakUrl = "http://localhost:$puerto"
}
$KeycloakUrl = $KeycloakUrl.TrimEnd('/')
# Candado: estos usuarios y contraseñas son de demostración y no deben llegar a
# un Keycloak compartido.
if (([uri]$KeycloakUrl).Host -notin @('localhost', '127.0.0.1')) {
    throw "Este script solo siembra un Keycloak local; recibí $KeycloakUrl."
}

function Invoke-Psql([string]$Sql) {
    $usuario = $entorno['VETCAT_DB_USER']; $base = $entorno['VETCAT_DB']
    $salida = $Sql | & docker compose -f $ComposeFile --env-file $EnvFile exec -T postgres `
        psql -U $usuario -d $base -v ON_ERROR_STOP=1 -tA -q 2>&1
    if ($LASTEXITCODE -ne 0) { throw "psql falló: $salida" }
    return @($salida | ForEach-Object { "$_".Trim() } | Where-Object { $_ -ne '' })
}

$clinicas = @('Veterinaria Norte', 'Veterinaria Sur')
$usuarios = @(
    @{ Correo = 'dos.clinicas@vetcat.dev'; Nombre = 'Demo'; Apellido = 'Dos Clinicas'
       Roles = @(@{ Clinica = 'Veterinaria Norte'; Rol = 'clinica_admin' }, @{ Clinica = 'Veterinaria Sur'; Rol = 'veterinario' }) },
    @{ Correo = 'recepcion@vetcat.dev'; Nombre = 'Demo'; Apellido = 'Recepcion'
       Roles = @(@{ Clinica = 'Veterinaria Norte'; Rol = 'recepcionista' }) },
    @{ Correo = 'cliente@vetcat.dev'; Nombre = 'Demo'; Apellido = 'Cliente'
       Roles = @(@{ Clinica = 'Veterinaria Norte'; Rol = 'cliente' }) }
)

Write-Host ''
Write-Host "Keycloak: $KeycloakUrl   Base: $($entorno['VETCAT_DB'])" -ForegroundColor Cyan
Write-Host ''

# --- 1. Clínicas ------------------------------------------------------------------
$idDe = @{}
try {
    foreach ($nombre in $clinicas) {
        $id = Invoke-Psql @"
WITH nueva AS (
    INSERT INTO catalogo.clinica (nombre, zona_horaria)
    SELECT '$nombre', 'America/Mexico_City'
    WHERE NOT EXISTS (SELECT 1 FROM catalogo.clinica WHERE nombre = '$nombre')
    RETURNING id
)
SELECT coalesce((SELECT id FROM nueva), (SELECT min(id) FROM catalogo.clinica WHERE nombre = '$nombre'));
"@ | Select-Object -Last 1
        $idDe[$nombre] = $id
        Add-Resultado "1. Clínica $nombre" 'OK' "id $id"
    }
} catch {
    Add-Resultado '1. Clínicas' 'FALLA' $_.Exception.Message
    Show-Resumen
}

# --- 2. Esquema de cada clínica ------------------------------------------------------
# Toda clínica activa queda registrada en migracion_tenant y se le pasa Flyway:
# Flyway no repite lo ya aplicado, así que también pone al día un esquema viejo.
try {
    $esquemas = Invoke-Psql @"
INSERT INTO catalogo.migracion_tenant (esquema)
SELECT esquema FROM catalogo.clinica
WHERE estado = 'ACTIVA'
ON CONFLICT (esquema) DO NOTHING;
SELECT esquema FROM catalogo.clinica WHERE estado = 'ACTIVA' ORDER BY id;
"@
} catch {
    Add-Resultado '2. Esquemas' 'FALLA' $_.Exception.Message
    Show-Resumen
}
foreach ($esquema in $esquemas) {
    if ($esquema -notmatch '^clinica_\d{3,}$') { Add-Resultado "2. Esquema $esquema" 'FALLA' 'nombre inesperado'; continue }
    # docker compose escribe su progreso en stderr; con 'Stop', Windows PowerShell
    # lo tomaría como error. Aquí se decide por el código de salida.
    $ErrorActionPreference = 'Continue'
    $salida = & docker compose -f $ComposeFile --env-file $EnvFile run --rm -T `
        -e "FLYWAY_SCHEMAS=$esquema,ext" -e "FLYWAY_DEFAULT_SCHEMA=$esquema" migraciones-tenant 2>&1
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($codigo -ne 0) {
        Invoke-Psql "UPDATE catalogo.migracion_tenant SET estado = 'fallida', ultimo_intento = now(), actualizado_en = now() WHERE esquema = '$esquema';" | Out-Null
        Add-Resultado "2. Esquema $esquema" 'FALLA' ((@($salida) | Select-Object -Last 5) -join ' | ')
        continue
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
    Add-Resultado "2. Esquema $esquema" 'OK' "aplicada, versión $version"
}

# --- 3. Usuarios y pertenencias -------------------------------------------------------
try {
    $admin = Invoke-RestMethod -Method Post -Uri "$KeycloakUrl/realms/master/protocol/openid-connect/token" `
        -ContentType 'application/x-www-form-urlencoded' -Body @{
            grant_type = 'password'; client_id = 'admin-cli'
            username = $entorno['KEYCLOAK_ADMIN']; password = $entorno['KEYCLOAK_ADMIN_PASSWORD']
        }
    $auth = @{ Authorization = "Bearer $($admin.access_token)" }
} catch {
    Add-Resultado '3. Admin de Keycloak' 'FALLA' $_.Exception.Message
    Show-Resumen
}
$base = "$KeycloakUrl/admin/realms/vetcat"

foreach ($u in $usuarios) {
    try {
        $correo = $u.Correo
        $buscar = "$base/users?email=$([uri]::EscapeDataString($correo))&exact=true"
        # Invoke-RestMethod entrega un arreglo JSON como un solo objeto; ForEach-Object lo desenvuelve.
        $existente = @(Invoke-RestMethod -Uri $buscar -Headers $auth | ForEach-Object { $_ })
        $cuerpo = @{
            username = $correo; email = $correo; firstName = $u.Nombre; lastName = $u.Apellido
            enabled = $true; emailVerified = $true; requiredActions = @()
        } | ConvertTo-Json
        if ($existente.Count -eq 0) {
            Invoke-RestMethod -Method Post -Uri "$base/users" -Headers $auth -ContentType 'application/json' -Body $cuerpo | Out-Null
            $existente = @(Invoke-RestMethod -Uri $buscar -Headers $auth | ForEach-Object { $_ })
        } else {
            Invoke-RestMethod -Method Put -Uri "$base/users/$($existente[0].id)" -Headers $auth -ContentType 'application/json' -Body $cuerpo | Out-Null
        }
        $sub = $existente[0].id
        Invoke-RestMethod -Method Put -Uri "$base/users/$sub/reset-password" -Headers $auth -ContentType 'application/json' `
            -Body (@{ type = 'password'; value = $Contrasena; temporary = $false } | ConvertTo-Json) | Out-Null

        $filas = ($u.Roles | ForEach-Object { "('$sub', $($idDe[$_.Clinica]), '$($_.Rol)')" }) -join ",`n       "
        Invoke-Psql @"
INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol)
VALUES $filas
ON CONFLICT (keycloak_id, clinica_id, rol) DO UPDATE SET estado = 'ACTIVO';
SELECT 'ok';
"@ | Out-Null
        $detalle = ($u.Roles | ForEach-Object { "$($_.Rol) en $($_.Clinica)" }) -join ', '
        Add-Resultado "3. $correo" 'OK' $detalle
    } catch {
        Add-Resultado "3. $($u.Correo)" 'FALLA' $_.Exception.Message
    }
}

Write-Host ''
Write-Host "Contraseña de los tres usuarios: $Contrasena" -ForegroundColor Cyan
Show-Resumen
