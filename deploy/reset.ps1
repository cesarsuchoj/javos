# ============================================================
# Javos - Resetar Banco de Dados (Windows PowerShell)
# ============================================================
# ATENCAO: Este script apaga TODOS os dados do banco!
# Use apenas se quiser comecar do zero.
# Uso: .\reset.ps1
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
Write-Host "   Reset do Banco de Dados - JAVOS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  ATENCAO: Esta operacao apagara TODOS os" -ForegroundColor Red
Write-Host "  dados do banco de dados!" -ForegroundColor Red
Write-Host ""

$confirm = Read-Host "  Tem certeza? Digite 'sim' para confirmar"

if ($confirm -ne "sim") {
    Write-Host ""
    Write-Host "  Operacao cancelada." -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Pressione Enter para sair"
    exit 0
}

# Faz backup de segurança antes de resetar
Write-Host ""
Write-Host "Fazendo backup de seguranca antes de resetar..."
if (Test-Path "$ScriptDir\backup.ps1") {
    try {
        & "$ScriptDir\backup.ps1"
    } catch {
        Write-Host "  [AVISO] Nao foi possivel fazer backup automatico. Continuando..." -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Parando a aplicacao..."
if ($ComposeCmd -eq "docker compose") {
    & docker compose down
} else {
    & docker-compose down
}

Write-Host "Removendo volumes de dados..."
if ($ComposeCmd -eq "docker compose") {
    & docker compose down -v
} else {
    & docker-compose down -v
}

Write-Host ""
Write-Host "[OK] Banco de dados resetado com sucesso." -ForegroundColor Green
Write-Host ""
Write-Host "  Execute .\start.ps1 para iniciar novamente." -ForegroundColor White
Write-Host ""
Read-Host "Pressione Enter para sair"
