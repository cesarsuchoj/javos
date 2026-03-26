# ============================================================
# Javos - Ver Logs (Windows PowerShell)
# ============================================================
# Uso: .\logs.ps1
# Pressione Ctrl+C para sair.
# ============================================================

#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# ---- Detecta docker compose ----
$ComposeCmd = $null
try {
    & docker compose version 2>&1 | Out-Null
    $ComposeCmd = "docker compose"
} catch {
    try {
        & docker-compose --version 2>&1 | Out-Null
        $ComposeCmd = "docker-compose"
    } catch {
        Write-Host "ERRO: Docker Compose nao encontrado." -ForegroundColor Red
        Read-Host "Pressione Enter para sair"
        exit 1
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Logs do JAVOS  (Ctrl+C para sair)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($ComposeCmd -eq "docker compose") {
    & docker compose logs --follow --tail=100
} else {
    & docker-compose logs --follow --tail=100
}
