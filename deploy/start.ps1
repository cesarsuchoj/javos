# ============================================================
# Javos - Iniciar Aplicacao (Windows PowerShell)
# ============================================================
# Uso: .\start.ps1
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
        Write-Host "ERRO: Docker Compose nao encontrado. Execute .\install.ps1 primeiro." -ForegroundColor Red
        Read-Host "Pressione Enter para sair"
        exit 1
    }
}

# ---- Verifica se o Docker esta rodando ----
$dockerRunning = $false
try {
    & docker info 2>&1 | Out-Null
    $dockerRunning = $true
} catch {}

if (-not $dockerRunning) {
    Write-Host ""
    Write-Host "Docker nao esta em execucao. Iniciando o Docker Desktop..." -ForegroundColor Yellow
    try {
        Start-Process "Docker Desktop"
        $waited = 0
        Write-Host "Aguardando o Docker iniciar..." -ForegroundColor Yellow
        do {
            Start-Sleep -Seconds 3
            $waited += 3
            Write-Host -NoNewline "."
            try { & docker info 2>&1 | Out-Null; $dockerRunning = $true } catch {}
        } while (-not $dockerRunning -and $waited -lt 60)
        Write-Host ""
        if (-not $dockerRunning) { throw "timeout" }
        Write-Host "[OK] Docker iniciado!" -ForegroundColor Green
    } catch {
        Write-Host ""
        Write-Host "ERRO: O Docker demorou para iniciar." -ForegroundColor Red
        Write-Host "  Abra o Docker Desktop manualmente e tente novamente." -ForegroundColor Yellow
        Read-Host "Pressione Enter para sair"
        exit 1
    }
}

# ---- Verifica se .env existe ----
if (-not (Test-Path ".env")) {
    Write-Host "Arquivo .env nao encontrado. Execute .\install.ps1 primeiro." -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Lê a porta configurada (padrão: 8080)
$AppPort = "8080"
$envContent = Get-Content ".env" -ErrorAction SilentlyContinue
foreach ($line in $envContent) {
    if ($line -match "^APP_PORT=(.+)") {
        $AppPort = $matches[1].Trim()
        break
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Iniciando JAVOS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Iniciando containers..."

if ($ComposeCmd -eq "docker compose") {
    & docker compose up -d
} else {
    & docker-compose up -d
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERRO: Falha ao iniciar a aplicacao." -ForegroundColor Red
    Write-Host "Verifique os logs com: .\logs.ps1" -ForegroundColor Yellow
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""
Write-Host "Aguardando a aplicacao ficar pronta..."

$maxWait = 120
$elapsed = 0
$ready = $false

do {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$AppPort/" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        if ($response.StatusCode -lt 500) { $ready = $true }
    } catch {}

    if (-not $ready) {
        if ($elapsed -ge $maxWait) {
            Write-Host ""
            Write-Host "  A aplicacao ainda nao respondeu apos $($maxWait)s." -ForegroundColor Yellow
            Write-Host "  Verifique os logs com: .\logs.ps1" -ForegroundColor Yellow
            Read-Host "Pressione Enter para sair"
            exit 1
        }
        Start-Sleep -Seconds 5
        $elapsed += 5
        Write-Host -NoNewline "."
    }
} while (-not $ready)

Write-Host ""
Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "   JAVOS esta rodando!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Acesse: http://localhost:$AppPort" -ForegroundColor Cyan
Write-Host ""

# Abre automaticamente no navegador padrão
Start-Process "http://localhost:$AppPort"

Write-Host "  Para ver os logs:  .\logs.ps1" -ForegroundColor White
Write-Host "  Para parar:        .\stop.ps1" -ForegroundColor White
Write-Host "  Para backup:       .\backup.ps1" -ForegroundColor White
Write-Host ""
Read-Host "Pressione Enter para sair"
