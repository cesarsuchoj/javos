# ============================================================
# Javos - Parar Aplicacao (Windows PowerShell)
# ============================================================
# Uso: .\stop.ps1
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
Write-Host "   Parando JAVOS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($ComposeCmd -eq "docker compose") {
    & docker compose down
} else {
    & docker-compose down
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERRO: Falha ao parar a aplicacao." -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""
Write-Host "[OK] Aplicacao parada com sucesso." -ForegroundColor Green
Write-Host ""
Write-Host "  Para iniciar novamente: .\start.ps1" -ForegroundColor White
Write-Host ""
Read-Host "Pressione Enter para sair"
