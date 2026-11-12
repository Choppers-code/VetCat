<#
.SYNOPSIS
    Comprueba que la máquina tenga lo necesario para trabajar en VetCat.

.DESCRIPTION
    Revisa git, GitHub CLI (y la sesión iniciada), docker, java y flutter.
    Devuelve código 1 si falta algo obligatorio, para poder usarlo en CI.

.EXAMPLE
    .\scripts\verificar-entorno.ps1
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Continue'

# Sin esto, la consola de Windows PowerShell muestra los acentos rotos.
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

$resultados = @()

function Add-Comprobacion {
    param(
        [string]$Herramienta,
        [string]$Estado,
        [string]$Detalle,
        [bool]$Obligatoria
    )
    $script:resultados += [pscustomobject]@{
        Herramienta = $Herramienta
        Estado      = $Estado
        Detalle     = $Detalle
        Obligatoria = $Obligatoria
    }
}

function Test-Herramienta {
    param(
        [string]$Nombre,
        [string]$Ejecutable,
        [string[]]$Argumentos = @('--version'),
        [bool]$Obligatoria = $false,
        [string]$Ayuda = ''
    )
    $cmd = Get-Command $Ejecutable -ErrorAction SilentlyContinue
    if (-not $cmd) {
        Add-Comprobacion $Nombre 'FALTA' $Ayuda $Obligatoria
        return
    }
    $salida = @(& $Ejecutable @Argumentos 2>&1)
    $texto = ''
    if ($salida.Count -gt 0) { $texto = "$($salida[0])".Trim() }
    Add-Comprobacion $Nombre 'OK' $texto $Obligatoria
}

# --- Obligatorio hoy: git y gh -------------------------------------------------
Test-Herramienta -Nombre 'git' -Ejecutable 'git' -Argumentos @('--version') -Obligatoria $true -Ayuda 'instala Git para Windows'

$gh = Get-Command 'gh' -ErrorAction SilentlyContinue
if (-not $gh) {
    Add-Comprobacion 'gh' 'FALTA' 'instala GitHub CLI: winget install GitHub.cli' $true
    Add-Comprobacion 'gh (sesión)' 'FALTA' 'hace falta gh para abrir el PR' $true
} else {
    $salidaGh = @(& gh --version 2>&1)
    Add-Comprobacion 'gh' 'OK' "$($salidaGh[0])".Trim() $true

    $usuario = (& gh api user --jq .login 2>$null)
    if ($LASTEXITCODE -eq 0 -and $usuario) {
        Add-Comprobacion 'gh (sesión)' 'OK' "sesión iniciada como $("$usuario".Trim())" $true
    } else {
        Add-Comprobacion 'gh (sesión)' 'FALTA' 'ejecuta: gh auth login' $true
    }
}

# --- Necesario a partir de la fase 1 -------------------------------------------
Test-Herramienta -Nombre 'docker' -Ejecutable 'docker' -Argumentos @('--version') -Ayuda 'instala Docker Desktop'
Test-Herramienta -Nombre 'docker compose' -Ejecutable 'docker' -Argumentos @('compose', 'version') -Ayuda 'viene con Docker Desktop'
Test-Herramienta -Nombre 'flutter' -Ejecutable 'flutter' -Argumentos @('--version') -Ayuda 'instala Flutter 3.x'

$java = Get-Command 'java' -ErrorAction SilentlyContinue
if (-not $java) {
    Add-Comprobacion 'java (JDK 21)' 'FALTA' 'instala Eclipse Temurin 21' $false
} else {
    $salidaJava = @(& java -version 2>&1)
    $textoJava = ''
    if ($salidaJava.Count -gt 0) { $textoJava = "$($salidaJava[0])".Trim() }
    if ($textoJava -match '"(\d+)') {
        $mayor = [int]$Matches[1]
        if ($mayor -ge 21) {
            Add-Comprobacion 'java (JDK 21)' 'OK' $textoJava $false
        } else {
            Add-Comprobacion 'java (JDK 21)' 'VIEJO' "$textoJava — se necesita 21 o superior" $false
        }
    } else {
        Add-Comprobacion 'java (JDK 21)' 'OK' $textoJava $false
    }
}

# --- Informe -------------------------------------------------------------------
Write-Host ''
Write-Host 'Entorno de VetCat' -ForegroundColor Cyan
Write-Host '=================' -ForegroundColor Cyan
Write-Host ''
$resultados | Format-Table -AutoSize -Property Herramienta, Estado, Detalle
Write-Host ''

$problemas = @($resultados | Where-Object { $_.Estado -ne 'OK' })

if ($problemas.Count -eq 0) {
    Write-Host 'Todo listo.' -ForegroundColor Green
    exit 0
}

$faltanObligatorias = @($problemas | Where-Object { $_.Obligatoria -eq $true })

if ($faltanObligatorias.Count -gt 0) {
    Write-Host 'Falta lo obligatorio para contribuir:' -ForegroundColor Red
    foreach ($p in $faltanObligatorias) {
        Write-Host "  - $($p.Herramienta): $($p.Detalle)" -ForegroundColor Red
    }
    Write-Host ''
    Write-Host 'Mira la sección Requisitos del README.'
    exit 1
}

Write-Host 'Puedes contribuir. Lo que falta solo hace falta a partir de la fase 1.' -ForegroundColor Yellow
exit 0
