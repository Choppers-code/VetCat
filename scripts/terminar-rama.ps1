<#
.SYNOPSIS
    Termina la rama actual: la sube, abre el PR y lo fusiona con squash.

.DESCRIPTION
    Equivale a `git flow feature finish`, pero el merge lo hace GitHub:
    pasa por la aprobación y por el CI. Si el PR no está aprobado,
    no fusiona y no borra nada.

    La primera ejecución abre el PR y se detiene. La segunda, ya con la
    aprobación, fusiona y elimina la rama.

.EXAMPLE
    .\scripts\terminar-rama.ps1 -Issue 27
#>
[CmdletBinding()]
param(
    [int]$Issue = 0
)

$ErrorActionPreference = 'Continue'

# Sin esto, la consola de Windows PowerShell muestra los acentos rotos.
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

function Fallar {
    param([string]$Mensaje)
    Write-Host $Mensaje -ForegroundColor Red
    exit 1
}

$rama = (& git branch --show-current).Trim()
if ($rama -notmatch '^(feature|bugfix)/(\d+)-') {
    Fallar "No estás en una rama de trabajo. Rama actual: $rama"
}

$numero = [int]$Matches[2]
if ($Issue -ne 0 -and $Issue -ne $numero) {
    Fallar "Estás en $rama, que es del issue #$numero, no del #$Issue."
}

$sucio = & git status --porcelain
if ($sucio) {
    Write-Host 'Tienes cambios sin confirmar; no se suben solos:' -ForegroundColor Yellow
    $sucio | ForEach-Object { Write-Host "  $_" }
    Fallar 'Confirma o guarda los cambios antes de terminar la rama.'
}

Write-Host "Subiendo $rama..." -ForegroundColor Cyan
$salida = & git push -u origin $rama 2>&1
if ($LASTEXITCODE -ne 0) {
    $salida | ForEach-Object { Write-Host "$_" -ForegroundColor Yellow }
    Fallar 'No se pudo subir la rama.'
}

& gh pr view --json 'number' 2>&1 | Out-Null
$hayPr = ($LASTEXITCODE -eq 0)

if (-not $hayPr) {
    $titulo = (& gh issue view $numero --json 'title' -q '.title').Trim()
    if (-not $titulo) { $titulo = "Issue #$numero" }

    Write-Host "Abriendo PR: $titulo" -ForegroundColor Cyan
    $salida = & gh pr create --base develop --title $titulo --body "Closes #$numero" 2>&1
    if ($LASTEXITCODE -ne 0) {
        $salida | ForEach-Object { Write-Host "$_" -ForegroundColor Yellow }
        Fallar 'No se pudo abrir el PR.'
    }
    Write-Host ''
    Write-Host 'PR abierto. Ahora necesita la aprobación del equipo del área.' -ForegroundColor Yellow
    Write-Host 'Vuelve a ejecutar este script cuando esté aprobado y el CI en verde.' -ForegroundColor Yellow
    exit 0
}

Write-Host 'Fusionando con squash...' -ForegroundColor Cyan
$salida = & gh pr merge --squash --delete-branch 2>&1
if ($LASTEXITCODE -ne 0) {
    $salida | ForEach-Object { Write-Host "$_" -ForegroundColor Yellow }
    Write-Host ''
    Write-Host 'No se fusionó. No se ha borrado nada.' -ForegroundColor Yellow
    Write-Host 'Suele faltar la aprobación del equipo del área, o el CI está en rojo.' -ForegroundColor Yellow
    exit 1
}

& git switch develop 2>&1 | Out-Null
& git pull --ff-only 2>&1 | Out-Null

Write-Host ''
Write-Host "Hecho. #$numero fusionado y la rama eliminada." -ForegroundColor Green
