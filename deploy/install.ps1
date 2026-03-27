# ============================================================
# Javos - Script de Instalacao (Windows PowerShell)
# ============================================================
# Execute este script UMA VEZ na primeira instalacao.
#
# Uso:
#   1. Clique com botao direito > "Executar com PowerShell"
#   2. Ou no terminal PowerShell:  .\install.ps1
#
# Se necessario, habilite scripts PowerShell com:
#   Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
# ============================================================

#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Instalacao do JAVOS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# ---- Verifica Docker ----
try {
    $dockerVersion = & docker --version 2>&1
    Write-Host "[OK] Docker encontrado: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "ERRO: Docker nao encontrado!" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Instale o Docker Desktop em:" -ForegroundColor Yellow
    Write-Host "  https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Pressione qualquer tecla para abrir o site de download..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Start-Process "https://www.docker.com/products/docker-desktop"
    exit 1
}

# ---- Verifica Docker Compose ----
$ComposeCmd = $null
try {
    & docker compose version 2>&1 | Out-Null
    $ComposeCmd = "docker compose"
    Write-Host "[OK] Docker Compose encontrado (plugin)" -ForegroundColor Green
} catch {
    try {
        & docker-compose --version 2>&1 | Out-Null
        $ComposeCmd = "docker-compose"
        Write-Host "[OK] Docker Compose encontrado (standalone)" -ForegroundColor Green
    } catch {
        Write-Host "ERRO: Docker Compose nao encontrado!" -ForegroundColor Red
        Write-Host ""
        Write-Host "  Instale o Docker Desktop (inclui Docker Compose):" -ForegroundColor Yellow
        Write-Host "  https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Pressione Enter para sair"
        exit 1
    }
}

# ---- Verifica se o Docker esta rodando ----
try {
    & docker info 2>&1 | Out-Null
    Write-Host "[OK] Docker esta em execucao" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "ERRO: Docker nao esta em execucao!" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Iniciando o Docker Desktop..." -ForegroundColor Yellow
    try {
        Start-Process "Docker Desktop"
        Write-Host "  Aguardando o Docker iniciar (pode levar ate 30 segundos)..." -ForegroundColor Yellow
        $waited = 0
        do {
            Start-Sleep -Seconds 3
            $waited += 3
            Write-Host -NoNewline "."
            $dockerRunning = (& docker info 2>&1) -notmatch "error"
        } while (-not $dockerRunning -and $waited -lt 60)
        Write-Host ""
        if (-not $dockerRunning) {
            throw "Docker nao iniciou a tempo"
        }
        Write-Host "[OK] Docker iniciado!" -ForegroundColor Green
    } catch {
        Write-Host ""
        Write-Host "  Abra o Docker Desktop manualmente e tente novamente." -ForegroundColor Yellow
        Read-Host "Pressione Enter para sair"
        exit 1
    }
}

# ---- Cria arquivo .env ----
Write-Host ""
if (-not (Test-Path ".env")) {
    Write-Host "Criando arquivo de configuracao (.env)..."
    Copy-Item ".env.example" ".env"
    Write-Host "[OK] Arquivo .env criado" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Dica: Edite o arquivo .env para personalizar a" -ForegroundColor Yellow
    Write-Host "  configuracao (porta, banco de dados, etc.)." -ForegroundColor Yellow
} else {
    Write-Host "[OK] Arquivo .env ja existe, mantendo configuracoes atuais" -ForegroundColor Green
}

# ---- Build das imagens Docker ----
Write-Host ""
Write-Host "Compilando a aplicacao (isso pode levar alguns minutos)..."
Write-Host "(Aguarde, nao feche esta janela)"
Write-Host ""

if ($ComposeCmd -eq "docker compose") {
    & docker compose build
} else {
    & docker-compose build
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERRO: Falha ao compilar a aplicacao." -ForegroundColor Red
    Write-Host "Verifique os logs acima para mais detalhes."
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "   Instalacao concluida com sucesso!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Para iniciar a aplicacao, execute:" -ForegroundColor White
Write-Host "    .\start.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Depois acesse:" -ForegroundColor White
Write-Host "    http://localhost:8080" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Outros comandos:" -ForegroundColor White
Write-Host "    .\stop.ps1       - Parar a aplicacao" -ForegroundColor White
Write-Host "    .\logs.ps1       - Ver logs" -ForegroundColor White
Write-Host "    .\backup.ps1     - Fazer backup dos dados" -ForegroundColor White
Write-Host ""
Read-Host "Pressione Enter para sair"
