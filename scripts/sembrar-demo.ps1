<#
.SYNOPSIS
    Siembra los datos de la demo del corte (#221) en el entorno local: dos
    clínicas con su esquema migrado y un usuario por cada papel que se enseña.

.DESCRIPTION
    Con el stack levantado (`docker compose up -d`) y el catálogo migrado
    (`docker compose run --rm migraciones`), hace cinco cosas y se puede repetir
    sin duplicar nada:

      1. Crea las clínicas «Veterinaria Norte» y «Veterinaria Sur» si no existen.
      2. Migra el esquema de toda clínica activa con `migrar-esquemas.ps1`, que
         también se puede correr solo. Repara cualquier clínica creada antes a
         mano, como la de `verificar-h1b.ps1`.
      3. Crea los usuarios en el realm `vetcat` y su pertenencia en
         `catalogo.usuario_clinica`:

           dos.clinicas@vetcat.dev   clinica_admin y veterinario en Norte,
                                     veterinario en Sur
           recepcion@vetcat.dev      recepcionista en Norte
           cliente@vetcat.dev        cliente en Norte

      4. Propietarios y mascotas: Ana Torres (Luna y Rocky) y Beto Ruiz (Michi)
         en Norte; Carla Mendez (Nube) en Sur.
      5. Agenda de Norte: «Consultorio 1» con horario de 08:00 a 20:00 todos los
         días, para crear citas (paso 8) sin SQL a mano.

    Con eso la demo tiene el paso 3 (elegir entre dos clínicas), el 4 y el 5
    (búsqueda, ficha y alta), el 6 (el 404 se enseña con Beto, id 2: el id 1
    existe en las dos clínicas), el 7 (el cliente recibe 403) y el 8 (al final
    se imprimen el veterinarioId, el consultorioId y el mascotaId que usa).

    Solo para local: no toca el export del realm ni corre contra un Keycloak que
    no esté en localhost. No necesita la API levantada.

.PARAMETER Contrasena
    La misma para los tres usuarios. Solo local. Si no se pasa, se lee de
    VETCAT_DEMO_CONTRASENA en el .env.

.EXAMPLE
    .\scripts\sembrar-demo.ps1

.NOTES
    Correr la app contra este entorno (teléfono por USB o emulador):

        adb reverse tcp:8080 tcp:8080
        adb reverse tcp:8090 tcp:8090
        cd frontend\apps\vetcat_app
        flutter run --dart-define=KEYCLOAK_EMISOR=http://localhost:8080/realms/vetcat --dart-define=API_BASE_URL=http://localhost:8090/api/v1

    - API_BASE_URL lleva /api/v1: sin él la app llama fuera de la API, recibe
      401 y cierra la sesión sin avisar.
    - Con adb reverse la app usa localhost igual que los scripts, así que la
      API arranca sin cambiar VETCAT_KEYCLOAK_EMISOR.
    - Si `adb` no se reconoce en PowerShell:
        Set-Alias adb "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
#>
[CmdletBinding()]
param(
    [string]$EnvFile = '',
    [string]$ComposeFile = '',
    [string]$KeycloakUrl = '',
    [string]$Contrasena = ''
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
if (-not $Contrasena) { $Contrasena = $entorno['VETCAT_DEMO_CONTRASENA'] }
if (-not $Contrasena) {
    $Contrasena = 'Demo-solo-local-2026'
    Write-Host "Aviso: falta VETCAT_DEMO_CONTRASENA en $EnvFile; uso la de .env.example." -ForegroundColor Yellow
}
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
       Roles = @(@{ Clinica = 'Veterinaria Norte'; Rol = 'clinica_admin' }, @{ Clinica = 'Veterinaria Norte'; Rol = 'veterinario' },
                 @{ Clinica = 'Veterinaria Sur'; Rol = 'veterinario' }) },
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
# El runner de esquemas vive aparte (migrar-esquemas.ps1): migrar no es sembrar.
& ([IO.Path]::Combine($raiz, 'scripts', 'migrar-esquemas.ps1')) -EnvFile $EnvFile -ComposeFile $ComposeFile
if ($LASTEXITCODE -ne 0) {
    Add-Resultado '2. Esquemas' 'FALLA' 'migrar-esquemas.ps1 reportó fallas (detalle arriba)'
} else {
    Add-Resultado '2. Esquemas' 'OK' 'todas las clínicas activas al día'
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
$subDe = @{}

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
        $subDe[$correo] = $sub
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

# --- 4. Propietarios y mascotas de la demo -----------------------------------------
# La ficha de #167 ya lee mascota de tenant/V1: con estos datos los pasos 4, 6 y 7
# tienen algo que enseñar aunque #169 no haya entrado.
$semilla = @(
    @{ Clinica = 'Veterinaria Norte'; Nombre = 'Ana Torres'; Telefono = '555 111 0001'; Correo = 'ana@demo.vetcat.dev'
       Mascotas = @(
           @{ Nombre = 'Luna'; Especie = 'GATO'; Raza = 'Mestiza'; Sexo = 'HEMBRA'; Nacimiento = '2021-03-15'; Peso = '4.20' },
           @{ Nombre = 'Rocky'; Especie = 'PERRO'; Raza = 'Labrador'; Sexo = 'MACHO'; Nacimiento = '2019-11-02'; Peso = '28.50' }
       ) },
    @{ Clinica = 'Veterinaria Norte'; Nombre = 'Beto Ruiz'; Telefono = '555 111 0002'; Correo = 'beto@demo.vetcat.dev'
       Mascotas = @(
           @{ Nombre = 'Michi'; Especie = 'GATO'; Raza = 'Siames'; Sexo = 'MACHO'; Nacimiento = '2022-07-20'; Peso = '3.80' }
       ) },
    @{ Clinica = 'Veterinaria Sur'; Nombre = 'Carla Mendez'; Telefono = '555 222 0001'; Correo = 'carla@demo.vetcat.dev'
       Mascotas = @(
           @{ Nombre = 'Nube'; Especie = 'PERRO'; Raza = 'Mestiza'; Sexo = 'HEMBRA'; Nacimiento = '2020-01-10'; Peso = '12.40' }
       ) }
)

foreach ($p in $semilla) {
    try {
        $esquema = (Invoke-Psql "SELECT esquema FROM catalogo.clinica WHERE id = $($idDe[$p.Clinica]);" | Select-Object -Last 1)
        if ($esquema -notmatch '^clinica_\d{3,}$') { throw "esquema inesperado: $esquema" }
        $nombre = $p.Nombre.Replace("'", "''")
        $telefono = $p.Telefono.Replace("'", "''")
        $correo = $p.Correo.Replace("'", "''")
        $propietarioId = Invoke-Psql @"
WITH nuevo AS (
    INSERT INTO $esquema.propietario (nombre, telefono, correo)
    SELECT '$nombre', '$telefono', '$correo'
    WHERE NOT EXISTS (
        SELECT 1 FROM $esquema.propietario
        WHERE telefono_normalizado = regexp_replace('$telefono', '\D', '', 'g')
    )
    RETURNING id
)
SELECT coalesce(
    (SELECT id FROM nuevo),
    (SELECT id FROM $esquema.propietario
     WHERE telefono_normalizado = regexp_replace('$telefono', '\D', '', 'g') LIMIT 1)
);
"@ | Select-Object -Last 1
        if (-not $propietarioId) { throw "no se pudo crear $($p.Nombre)" }
        foreach ($m in $p.Mascotas) {
            $mascota = $m.Nombre.Replace("'", "''")
            $raza = $m.Raza.Replace("'", "''")
            Invoke-Psql @"
INSERT INTO $esquema.mascota (propietario_id, nombre, especie, raza, sexo, fecha_nacimiento, peso_kg)
SELECT $propietarioId, '$mascota', '$($m.Especie)', '$raza', '$($m.Sexo)', DATE '$($m.Nacimiento)', $($m.Peso)
WHERE NOT EXISTS (
    SELECT 1 FROM $esquema.mascota
    WHERE propietario_id = $propietarioId AND nombre = '$mascota'
);
"@ | Out-Null
        }
        Add-Resultado "4. $($p.Nombre)" 'OK' ("$($p.Mascotas.Count) mascota(s) en $($p.Clinica)")
    } catch {
        Add-Resultado "4. $($p.Nombre)" 'FALLA' $_.Exception.Message
    }
}

# --- 5. Agenda de Norte (paso 8) -------------------------------------------------------
# Un consultorio con horario todos los días, para que la cita del paso 8 caiga
# dentro del horario sea cual sea el día de la demo.
$paso8 = $null
try {
    $norte = $idDe['Veterinaria Norte']
    $esquema = (Invoke-Psql "SELECT esquema FROM catalogo.clinica WHERE id = $norte;" | Select-Object -Last 1)
    if ($esquema -notmatch '^clinica_\d{3,}$') { throw "esquema inesperado: $esquema" }
    $consultorioId = Invoke-Psql @"
WITH nuevo AS (
    INSERT INTO $esquema.consultorio (nombre)
    SELECT 'Consultorio 1'
    WHERE NOT EXISTS (SELECT 1 FROM $esquema.consultorio WHERE nombre = 'Consultorio 1')
    RETURNING id
)
SELECT coalesce((SELECT id FROM nuevo), (SELECT min(id) FROM $esquema.consultorio WHERE nombre = 'Consultorio 1'));
"@ | Select-Object -Last 1
    Invoke-Psql @"
INSERT INTO $esquema.horario_consultorio (consultorio_id, dia_semana, inicio, fin)
SELECT $consultorioId, d, '08:00', '20:00' FROM generate_series(1, 7) AS d
WHERE NOT EXISTS (
    SELECT 1 FROM $esquema.horario_consultorio h
    WHERE h.consultorio_id = $consultorioId AND h.dia_semana = d AND h.fecha_excepcion IS NULL
);
SELECT 'ok';
"@ | Out-Null
    $sub = $subDe['dos.clinicas@vetcat.dev']
    if (-not $sub) { throw 'no se creó dos.clinicas@vetcat.dev (ver paso 3)' }
    $veterinarioId = Invoke-Psql "SELECT id FROM catalogo.usuario_clinica WHERE keycloak_id = '$sub' AND clinica_id = $norte AND rol = 'veterinario' AND estado = 'ACTIVO';" | Select-Object -Last 1
    $mascotaId = Invoke-Psql @"
SELECT m.id FROM $esquema.mascota m JOIN $esquema.propietario p ON p.id = m.propietario_id
WHERE m.nombre = 'Luna' AND p.telefono_normalizado = '5551110001' LIMIT 1;
"@ | Select-Object -Last 1
    if (-not $veterinarioId -or -not $mascotaId) { throw "faltan datos: veterinarioId=$veterinarioId mascotaId=$mascotaId" }
    $paso8 = "veterinarioId=$veterinarioId, consultorioId=$consultorioId, mascotaId=$mascotaId (Luna)"
    Add-Resultado '5. Agenda de Norte' 'OK' "consultorio $consultorioId con horario de 08:00 a 20:00 todos los días"
} catch {
    Add-Resultado '5. Agenda de Norte' 'FALLA' $_.Exception.Message
}

Write-Host ''
Write-Host "Contraseña de los tres usuarios: $Contrasena" -ForegroundColor Cyan
Write-Host "Propietarios de demo: 2 en Norte y 1 en Sur, con mascotas. El 404 del paso 6 se enseña con Beto (id 2)." -ForegroundColor Cyan
if ($paso8) {
    Write-Host "Paso 8: POST /api/v1/citas en Norte con $paso8." -ForegroundColor Cyan
}
Write-Host 'App: ver Get-Help .\scripts\sembrar-demo.ps1 -Full (adb reverse y API_BASE_URL con /api/v1).' -ForegroundColor Cyan
Show-Resumen
