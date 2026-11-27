<#
.SYNOPSIS
    Verificación H1b de punta a punta (#159): un usuario del realm `vetcat`
    entra con PKCE, ve sus clínicas y obtiene el token de tenant.

.DESCRIPTION
    Con el stack levantado y la API corriendo, hace los pasos de #159:

      1. Crea (o reutiliza) un usuario de prueba en el realm `vetcat` y le da
         una clínica de prueba en `catalogo.usuario_clinica`.
      2. Obtiene el token de identidad con Authorization Code + PKCE (S256),
         como la app: sin flujo directo y sin secreto de cliente.
      3. GET  /api/v1/sesion/clinicas        con el token de identidad.
      4. POST /api/v1/sesion/clinicas/{id}   y lee el token de tenant.
      5. Una ruta de negocio sin token responde 401; con el token de tenant,
         registrar y consultar un propietario responde 201/200; con un rol
         ajeno (`cliente` en otra clínica), registrar responde 403.
      6. El token de identidad no abre una ruta de negocio (401).

    Solo toca datos de prueba: un usuario `h1b@vetcat.dev` en Keycloak, las
    clínicas «Clinica H1b» y «Clinica H1b cliente» en el catálogo y un
    propietario de prueba en el esquema de la primera. Nada de esto va en el
    export del realm.

    El esquema de «Clinica H1b» tiene que estar migrado: corre antes
    `.\scripts\sembrar-demo.ps1`, que migra el esquema de todas las clínicas.

.PARAMETER ApiUrl
    Dónde escucha la API. Por defecto 8090, porque Keycloak ocupa el 8080.
    Antes de correr el script, en otra terminal:
        cd backend
        $env:SERVER_PORT = '8090'
        $env:VETCAT_DB_URL = 'jdbc:postgresql://localhost:<POSTGRES_PORT de .env>/vetcat_db'
        $env:VETCAT_DB_PASSWORD = '<VETCAT_DB_PASSWORD de infrastructure\docker\.env>'
        $env:VETCAT_TOKEN_CLAVE = '<VETCAT_TOKEN_CLAVE de infrastructure\docker\.env>'
        .\gradlew.bat bootRun
    VETCAT_DB_URL hace falta si POSTGRES_PORT no es 5432: sin ella la API se
    conecta al PostgreSQL que ya tenga la máquina en ese puerto.

.PARAMETER SoloKeycloak
    Hace solo los pasos 1 y 2. Útil para probar el realm sin la API.

.EXAMPLE
    .\scripts\verificar-h1b.ps1

.EXAMPLE
    .\scripts\verificar-h1b.ps1 -SoloKeycloak
#>
[CmdletBinding()]
param(
    [string]$EnvFile = '',
    [string]$ComposeFile = '',
    [string]$KeycloakUrl = '',
    [string]$ApiUrl = 'http://localhost:8090',
    [string]$Correo = 'h1b@vetcat.dev',
    [string]$Contrasena = 'H1b-solo-local-2026',
    [string]$NombreClinica = 'Clinica H1b',
    [ValidateSet('clinica_admin', 'veterinario', 'recepcionista', 'cliente')]
    [string]$Rol = 'clinica_admin',
    [switch]$SoloKeycloak
)

$ErrorActionPreference = 'Stop'
# Sin esto, la consola de Windows PowerShell muestra los acentos rotos y el SQL
# que se manda a psql por la tubería pierde los caracteres no ASCII.
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }
$OutputEncoding = New-Object System.Text.UTF8Encoding($false)

$resultados = New-Object System.Collections.Generic.List[object]
function Show-Resumen {
    $fallas = @($resultados | Where-Object { $_.Estado -eq 'FALLA' }).Count
    $ok = @($resultados | Where-Object { $_.Estado -eq 'OK' }).Count
    $pend = @($resultados | Where-Object { $_.Estado -eq 'PENDIENTE' }).Count
    Write-Host ''
    Write-Host ("Resumen: {0} OK, {1} pendientes, {2} fallas." -f $ok, $pend, $fallas) -ForegroundColor $(if ($fallas) { 'Red' } else { 'Green' })
    if ($fallas) { exit 1 } else { exit 0 }
}
function Add-Resultado([string]$Paso, [string]$Estado, [string]$Detalle) {
    $resultados.Add([pscustomobject]@{ Paso = $Paso; Estado = $Estado; Detalle = $Detalle })
    $color = switch ($Estado) { 'OK' { 'Green' } 'FALLA' { 'Red' } default { 'Yellow' } }
    Write-Host ("[{0}] {1} — {2}" -f $Estado, $Paso, $Detalle) -ForegroundColor $color
}

# --- Configuración desde .env --------------------------------------------------
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
$ApiUrl = $ApiUrl.TrimEnd('/')
$redirectUri = if ($entorno['VETCAT_APP_REDIRECT_URI']) { $entorno['VETCAT_APP_REDIRECT_URI'] } else { 'vetcat://callback' }

# --- Utilidades ------------------------------------------------------------------
function ConvertTo-Base64Url([byte[]]$Bytes) {
    [Convert]::ToBase64String($Bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

function Read-Jwt([string]$Token) {
    $parte = $Token.Split('.')[1].Replace('-', '+').Replace('_', '/')
    switch ($parte.Length % 4) { 2 { $parte += '==' } 3 { $parte += '=' } }
    [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($parte)) | ConvertFrom-Json
}

# Las peticiones del login van con HttpClient y sin seguir redirecciones: así se
# lee el `code` de la redirección a vetcat://callback igual en Windows PowerShell
# 5.1 y en PowerShell 7. Keycloak marca sus cookies como Secure y .NET no las
# reenvía por http://localhost, así que se guardan y se mandan a mano.
Add-Type -AssemblyName System.Net.Http
$manejador = New-Object System.Net.Http.HttpClientHandler
$manejador.AllowAutoRedirect = $false
$manejador.UseCookies = $false
$cliente = New-Object System.Net.Http.HttpClient($manejador)
$cookies = @{}

function Invoke-Http([string]$Metodo, [string]$Url, [hashtable]$Formulario = $null, [string]$Bearer = '', [string]$Json = '') {
    $peticion = New-Object System.Net.Http.HttpRequestMessage([System.Net.Http.HttpMethod]::new($Metodo), $Url)
    if ($cookies.Count -gt 0) {
        $peticion.Headers.TryAddWithoutValidation('Cookie', (($cookies.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" }) -join '; ')) | Out-Null
    }
    if ($Bearer) { $peticion.Headers.TryAddWithoutValidation('Authorization', "Bearer $Bearer") | Out-Null }
    if ($null -ne $Formulario) {
        $pares = New-Object 'System.Collections.Generic.List[System.Collections.Generic.KeyValuePair[string,string]]'
        foreach ($k in $Formulario.Keys) { $pares.Add((New-Object 'System.Collections.Generic.KeyValuePair[string,string]' ($k, "$($Formulario[$k])"))) }
        $peticion.Content = New-Object System.Net.Http.FormUrlEncodedContent -ArgumentList (,$pares)
    }
    if ($Json) {
        $peticion.Content = New-Object System.Net.Http.StringContent($Json, [Text.Encoding]::UTF8, 'application/json')
    }
    $respuesta = $cliente.SendAsync($peticion).GetAwaiter().GetResult()
    $valores = $null
    if ($respuesta.Headers.TryGetValues('Set-Cookie', [ref]$valores)) {
        foreach ($v in $valores) { if ($v -match '^\s*([^=;\s]+)=([^;]*)') { $cookies[$Matches[1]] = $Matches[2] } }
    }
    $loc = if ($respuesta.Headers.Location) { "$($respuesta.Headers.Location.OriginalString)" } else { '' }
    $cuerpo = $respuesta.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    return [pscustomobject]@{ Codigo = [int]$respuesta.StatusCode; Location = $loc; Cuerpo = $cuerpo }
}

function Invoke-Psql([string]$Sql) {
    $usuario = $entorno['VETCAT_DB_USER']; $base = $entorno['VETCAT_DB']
    $salida = $Sql | & docker compose -f $ComposeFile --env-file $EnvFile exec -T postgres `
        psql -U $usuario -d $base -v ON_ERROR_STOP=1 -tA -q 2>&1
    if ($LASTEXITCODE -ne 0) { throw "psql falló: $salida" }
    return (@($salida) | Where-Object { "$_".Trim() -ne '' } | Select-Object -Last 1)
}

Write-Host ''
Write-Host "Keycloak: $KeycloakUrl   API: $ApiUrl   Usuario: $Correo" -ForegroundColor Cyan
Write-Host ''

# --- Paso 1: usuario de prueba y su clínica ---------------------------------------
try {
    $admin = Invoke-RestMethod -Method Post -Uri "$KeycloakUrl/realms/master/protocol/openid-connect/token" `
        -ContentType 'application/x-www-form-urlencoded' -Body @{
            grant_type = 'password'; client_id = 'admin-cli'
            username = $entorno['KEYCLOAK_ADMIN']; password = $entorno['KEYCLOAK_ADMIN_PASSWORD']
        }
    $auth = @{ Authorization = "Bearer $($admin.access_token)" }
    $base = "$KeycloakUrl/admin/realms/vetcat"

    # Invoke-RestMethod entrega un arreglo JSON como un solo objeto; ForEach-Object lo desenvuelve.
    $existente = @(Invoke-RestMethod -Uri "$base/users?email=$([uri]::EscapeDataString($Correo))&exact=true" -Headers $auth | ForEach-Object { $_ })
    $usuario = @{
        username = $Correo; email = $Correo; firstName = 'Prueba'; lastName = 'H1b'
        enabled = $true; emailVerified = $true; requiredActions = @()
    }
    if ($existente.Count -eq 0) {
        Invoke-RestMethod -Method Post -Uri "$base/users" -Headers $auth -ContentType 'application/json' `
            -Body ($usuario | ConvertTo-Json) | Out-Null
        $existente = @(Invoke-RestMethod -Uri "$base/users?email=$([uri]::EscapeDataString($Correo))&exact=true" -Headers $auth | ForEach-Object { $_ })
        $accion = 'creado'
    } else {
        Invoke-RestMethod -Method Put -Uri "$base/users/$($existente[0].id)" -Headers $auth -ContentType 'application/json' `
            -Body ($usuario | ConvertTo-Json) | Out-Null
        $accion = 'reutilizado'
    }
    $sub = $existente[0].id
    Invoke-RestMethod -Method Put -Uri "$base/users/$sub/reset-password" -Headers $auth -ContentType 'application/json' `
        -Body (@{ type = 'password'; value = $Contrasena; temporary = $false } | ConvertTo-Json) | Out-Null
    Add-Resultado '1. Usuario en Keycloak' 'OK' "$Correo $accion (sub $sub)"
} catch {
    Add-Resultado "1. Usuario en Keycloak" "FALLA" $_.Exception.Message
    Show-Resumen
}

$clinicaId = $null
if (-not $SoloKeycloak) {
    try {
        $nombreSql = $NombreClinica.Replace("'", "''")
        $clinicaId = Invoke-Psql @"
WITH nueva AS (
    INSERT INTO catalogo.clinica (nombre, zona_horaria)
    SELECT '$nombreSql', 'America/Mexico_City'
    WHERE NOT EXISTS (SELECT 1 FROM catalogo.clinica WHERE nombre = '$nombreSql')
    RETURNING id
)
SELECT id FROM nueva UNION ALL SELECT id FROM catalogo.clinica WHERE nombre = '$nombreSql' LIMIT 1;
"@
        Invoke-Psql @"
INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol)
VALUES ('$sub', $clinicaId, '$Rol')
ON CONFLICT (keycloak_id, clinica_id, rol) DO UPDATE SET estado = 'ACTIVO';
SELECT 'ok';
"@ | Out-Null
        Add-Resultado '1. Clínica del usuario' 'OK' "clínica $clinicaId «$NombreClinica», rol $Rol"
    } catch {
        Add-Resultado '1. Clínica del usuario' 'FALLA' $_.Exception.Message
        Show-Resumen
    }
}

# --- Paso 2: token de identidad con Authorization Code + PKCE ---------------------
$identidad = $null
try {
    $bytes = New-Object byte[] 32
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $verifier = ConvertTo-Base64Url $bytes
    $challenge = ConvertTo-Base64Url ([Security.Cryptography.SHA256]::Create().ComputeHash([Text.Encoding]::ASCII.GetBytes($verifier)))
    $oidc = "$KeycloakUrl/realms/vetcat/protocol/openid-connect"
    $authUrl = "$oidc/auth?client_id=vetcat-app&response_type=code&scope=openid" +
        "&redirect_uri=$([uri]::EscapeDataString($redirectUri))&code_challenge=$challenge&code_challenge_method=S256&state=h1b"

    $login = Invoke-Http 'GET' $authUrl
    if ($login.Cuerpo -notmatch '<form[^>]*action="([^"]+)"') { throw "Keycloak no mostró el formulario de login (HTTP $($login.Codigo))." }
    $accionForm = [Net.WebUtility]::HtmlDecode($Matches[1])
    $envio = Invoke-Http 'POST' $accionForm @{ username = $Correo; password = $Contrasena; credentialId = '' }
    if ($envio.Location -notmatch '[?&]code=([^&]+)') {
        throw "Keycloak no devolvió el code (HTTP $($envio.Codigo)). ¿El usuario tiene acciones pendientes o la contraseña cambió?"
    }
    $code = [uri]::UnescapeDataString($Matches[1])
    $token = Invoke-RestMethod -Method Post -Uri "$oidc/token" -ContentType 'application/x-www-form-urlencoded' -Body @{
        grant_type = 'authorization_code'; client_id = 'vetcat-app'; code = $code
        redirect_uri = $redirectUri; code_verifier = $verifier
    }
    $identidad = $token.access_token
    $jwt = Read-Jwt $identidad
    $aud = @($jwt.aud) -join ','
    $sinTenant = -not ($jwt.PSObject.Properties.Name -contains 'tenant_id')
    $detalle = "sub=$($jwt.sub) azp=$($jwt.azp) aud=$aud vida=$($jwt.exp - $jwt.iat)s sin tenant_id=$sinTenant"
    $ok = ($jwt.sub -eq $sub) -and ($jwt.azp -eq 'vetcat-app') -and ($aud -match 'vetcat-api') -and $sinTenant
    Add-Resultado '2. Token de identidad (PKCE S256)' ($(if ($ok) { 'OK' } else { 'FALLA' })) $detalle
} catch {
    Add-Resultado "2. Token de identidad (PKCE S256)" "FALLA" $_.Exception.Message
}

if ($SoloKeycloak -or -not $identidad) {
    Show-Resumen
}

# --- Pasos 3 y 4: sesión ----------------------------------------------------------------
try {
    Invoke-Http 'GET' "$ApiUrl/api/v1/sesion/clinicas" | Out-Null
} catch {
    Add-Resultado '3-6. API' 'FALLA' ("No responde $ApiUrl. Arranca la API como dice Get-Help .\scripts\verificar-h1b.ps1 -Full")
    Write-Host ''; Show-Resumen
}
$tenant = $null
try {
    $r = Invoke-Http 'GET' "$ApiUrl/api/v1/sesion/clinicas" $null $identidad
    if ($r.Codigo -ne 200) { throw "HTTP $($r.Codigo): $($r.Cuerpo)" }
    $sobre = $r.Cuerpo | ConvertFrom-Json
    $mia = @($sobre.data) | Where-Object { "$($_.id)" -eq "$clinicaId" }
    $estado = if ($mia) { 'OK' } else { 'FALLA' }
    Add-Resultado '3. GET /sesion/clinicas' $estado ("HTTP 200; " + ((@($sobre.data) | ForEach-Object { "$($_.id) $($_.nombre) [$((@($_.roles)) -join ',')]" }) -join ' | '))
} catch {
    Add-Resultado '3. GET /sesion/clinicas' 'FALLA' $_.Exception.Message
}

try {
    $r = Invoke-Http 'POST' "$ApiUrl/api/v1/sesion/clinicas/$clinicaId" $null $identidad
    if ($r.Codigo -ne 200) { throw "HTTP $($r.Codigo): $($r.Cuerpo)" }
    $tenant = ($r.Cuerpo | ConvertFrom-Json).data.token
    $t = Read-Jwt $tenant
    $ok = ("$($t.tenant_id)" -eq "$clinicaId") -and ($t.sub -eq $sub) -and ((@($t.roles)) -contains $Rol) -and (($t.exp - $t.iat) -eq 900)
    Add-Resultado '4. POST /sesion/clinicas/{id}' ($(if ($ok) { 'OK' } else { 'FALLA' })) `
        "token de tenant: iss=$($t.iss) tenant_id=$($t.tenant_id) roles=$((@($t.roles)) -join ',') vida=$($t.exp - $t.iat)s"
} catch {
    Add-Resultado '4. POST /sesion/clinicas/{id}' 'FALLA' $_.Exception.Message
}

# --- Pasos 5 y 6: la cadena de negocio ----------------------------------------------------
# Cualquier ruta fuera de /sesion y /plataforma pasa por la cadena de negocio.
$rutaNegocio = "$ApiUrl/api/v1/propietarios"
$r = Invoke-Http 'GET' $rutaNegocio
Add-Resultado '5. Ruta de negocio sin token' ($(if ($r.Codigo -eq 401) { 'OK' } else { 'FALLA' })) "HTTP $($r.Codigo) $($r.Cuerpo)"

# 5b. Con el token de tenant: registrar (o reutilizar) un propietario y leer su ficha.
# El teléfono es fijo para que repetir el script no cree otra ficha.
$telefonoH1b = '555 000 0159'
try {
    if (-not $tenant) { throw 'no hay token de tenant (falló el paso 4)' }
    $esquema = Invoke-Psql "SELECT c.esquema || '|' || coalesce(m.estado, 'sin registrar') FROM catalogo.clinica c LEFT JOIN catalogo.migracion_tenant m ON m.esquema = c.esquema WHERE c.id = $clinicaId;"
    $esquema, $migracion = "$esquema".Split('|')
    if ($esquema -notmatch '^clinica_\d{3,}$') { throw "esquema inesperado: $esquema" }
    if ($migracion -ne 'aplicada') {
        throw "el esquema $esquema está «$migracion»: corre .\scripts\sembrar-demo.ps1 para migrarlo"
    }
    $propietarioId = Invoke-Psql "SELECT id FROM $esquema.propietario WHERE telefono_normalizado = regexp_replace('$telefonoH1b', '\D', '', 'g') ORDER BY id LIMIT 1;"
    $alta = 'reutilizado'
    if (-not $propietarioId) {
        $cuerpo = @{ nombre = 'Propietario H1b'; telefono = $telefonoH1b } | ConvertTo-Json -Compress
        $r = Invoke-Http 'POST' $rutaNegocio $null $tenant $cuerpo
        if ($r.Codigo -ne 201) { throw "POST HTTP $($r.Codigo): $($r.Cuerpo)" }
        $propietarioId = ($r.Cuerpo | ConvertFrom-Json).data.id
        $alta = 'POST 201'
    }
    $r = Invoke-Http 'GET' "$rutaNegocio/$propietarioId" $null $tenant
    $ficha = if ($r.Codigo -eq 200) { ($r.Cuerpo | ConvertFrom-Json).data } else { $null }
    $ok = ($r.Codigo -eq 200) -and ("$($ficha.id)" -eq "$propietarioId")
    Add-Resultado '5. Con token de tenant (clinica_admin)' ($(if ($ok) { 'OK' } else { 'FALLA' })) `
        "$alta; GET /propietarios/$propietarioId HTTP $($r.Codigo) «$($ficha.nombre)» en $esquema"
} catch {
    Add-Resultado '5. Con token de tenant (clinica_admin)' 'FALLA' $_.Exception.Message
}

# 5c. Rol ajeno: el mismo usuario es `cliente` en otra clínica. Con ese token de
# tenant, registrar un propietario es 403 (Matriz_roles.md: `C —`). La segunda
# clínica no necesita esquema: el permiso se niega antes de tocar datos.
try {
    $nombreCliente = "$NombreClinica cliente".Replace("'", "''")
    $clinicaCliente = Invoke-Psql @"
WITH nueva AS (
    INSERT INTO catalogo.clinica (nombre, zona_horaria)
    SELECT '$nombreCliente', 'America/Mexico_City'
    WHERE NOT EXISTS (SELECT 1 FROM catalogo.clinica WHERE nombre = '$nombreCliente')
    RETURNING id
)
SELECT coalesce((SELECT id FROM nueva), (SELECT min(id) FROM catalogo.clinica WHERE nombre = '$nombreCliente'));
"@
    Invoke-Psql @"
INSERT INTO catalogo.usuario_clinica (keycloak_id, clinica_id, rol)
VALUES ('$sub', $clinicaCliente, 'cliente')
ON CONFLICT (keycloak_id, clinica_id, rol) DO UPDATE SET estado = 'ACTIVO';
SELECT 'ok';
"@ | Out-Null
    $r = Invoke-Http 'POST' "$ApiUrl/api/v1/sesion/clinicas/$clinicaCliente" $null $identidad
    if ($r.Codigo -ne 200) { throw "token de cliente: HTTP $($r.Codigo): $($r.Cuerpo)" }
    $tokenCliente = ($r.Cuerpo | ConvertFrom-Json).data.token
    $roles = (@((Read-Jwt $tokenCliente).roles)) -join ','
    $cuerpo = @{ nombre = 'No debe guardarse'; telefono = '555 000 0403' } | ConvertTo-Json -Compress
    $r = Invoke-Http 'POST' $rutaNegocio $null $tokenCliente $cuerpo
    $sobre = try { $r.Cuerpo | ConvertFrom-Json } catch { $null }
    $ok = ($r.Codigo -eq 403) -and ($roles -eq 'cliente') -and ($null -ne $sobre) -and ($sobre.success -eq $false)
    Add-Resultado '5. Rol ajeno (cliente) registra propietario' ($(if ($ok) { 'OK' } else { 'FALLA' })) `
        "clínica $clinicaCliente roles=$roles; HTTP $($r.Codigo) $($r.Cuerpo)"
} catch {
    Add-Resultado '5. Rol ajeno (cliente) registra propietario' 'FALLA' $_.Exception.Message
}

$r = Invoke-Http 'GET' $rutaNegocio $null $identidad
Add-Resultado '6. Token de identidad en ruta de negocio' ($(if ($r.Codigo -eq 401) { 'OK' } else { 'FALLA' })) "HTTP $($r.Codigo) $($r.Cuerpo)"

Show-Resumen
