<#
.SYNOPSIS
    Crea la rama de trabajo de un issue, desde develop actualizado.

.DESCRIPTION
    Hace lo mismo que `git flow feature start`, sin instalar nada:
    cambia a develop, lo actualiza y crea feature/<issue>-<slug>,
    o bugfix/<issue>-<slug> con -Bugfix.

.EXAMPLE
    .\scripts\nueva-rama.ps1 -Issue 27 -Slug entorno-y-ramas

.EXAMPLE
    .\scripts\nueva-rama.ps1 -Issue 41 -Slug empalme-fantasma -Bugfix
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [int]$Issue,

    [Parameter(Mandatory = $true, Position = 1)]
    [string]$Slug,

    [switch]$Bugfix,

    [switch]$Forzar
)

$ErrorActionPreference = 'Continue'

# Sin esto, la consola de Windows PowerShell muestra los acentos rotos.
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

function Fallar {
    param([string]$Mensaje)
    Write-Host $Mensaje -ForegroundColor Red
    exit 1
}

if ($Slug -notmatch '^[a-z0-9]+(-[a-z0-9]+)*$') {
    Fallar 'El slug solo admite minúsculas, números y guiones. Ejemplo: agenda-de-citas'
}

$tipo = if ($Bugfix) { 'bugfix' } else { 'feature' }
$rama = "$tipo/$Issue-$Slug"

if (-not (Get-Command 'gh' -ErrorAction SilentlyContinue)) {
    Fallar 'Falta GitHub CLI. Instálalo con: winget install GitHub.cli'
}

& gh issue view $Issue --json 'number' 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Fallar "No existe el issue #$Issue en este repositorio."
}

$existente = & git branch --list $rama
if ($existente) {
    Fallar "La rama $rama ya existe. Cámbiate con: git switch $rama"
}

$sucio = & git status --porcelain
if ($sucio -and -not $Forzar) {
    Write-Host 'Tienes cambios sin confirmar:' -ForegroundColor Yellow
    $sucio | ForEach-Object { Write-Host "  $_" }
    Write-Host ''
    Write-Host 'Confirma con git commit, guarda con git stash, o repite con -Forzar.' -ForegroundColor Yellow
    exit 1
}

Write-Host "Creando $rama desde develop..." -ForegroundColor Cyan

& git switch develop 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo cambiar a develop.' }

& git pull --ff-only 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo actualizar develop. Revisa que no tengas cambios locales.' }

& git switch -c $rama 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) { Fallar 'No se pudo crear la rama.' }

Write-Host ''
Write-Host "Listo: $rama" -ForegroundColor Green
Write-Host ''
Write-Host 'Siguientes pasos:'
Write-Host "  1. Trabaja y confirma:  git commit -m `"tipo(scope): descripción`" -m `"Refs #$Issue`""
Write-Host "  2. Al terminar:         .\scripts\terminar-rama.ps1 -Issue $Issue"
