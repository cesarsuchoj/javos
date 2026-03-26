@echo off
REM ============================================================
REM Javos - Script de Instalacao (Windows)
REM ============================================================
REM Execute este script UMA VEZ na primeira instalacao.
REM Uso: Duplo clique em install.bat  -ou-  execute no prompt
REM ============================================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ==========================================
echo    Instalacao do JAVOS
echo ==========================================
echo.

REM ---- Verifica Docker ----
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Docker nao encontrado!
    echo.
    echo   Instale o Docker Desktop em:
    echo   https://www.docker.com/products/docker-desktop
    echo.
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('docker --version') do echo [OK] Docker encontrado: %%i

REM ---- Verifica Docker Compose ----
docker compose version >nul 2>&1
if not errorlevel 1 (
    set COMPOSE_CMD=docker compose
) else (
    docker-compose --version >nul 2>&1
    if not errorlevel 1 (
        set COMPOSE_CMD=docker-compose
    ) else (
        echo.
        echo ERRO: Docker Compose nao encontrado!
        echo.
        echo   Instale o Docker Desktop (inclui Docker Compose) em:
        echo   https://www.docker.com/products/docker-desktop
        echo.
        pause
        exit /b 1
    )
)
echo [OK] Docker Compose encontrado

REM ---- Verifica se o Docker esta rodando ----
docker info >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERRO: Docker nao esta em execucao!
    echo.
    echo   Inicie o Docker Desktop e tente novamente.
    echo.
    pause
    exit /b 1
)
echo [OK] Docker esta em execucao

REM ---- Cria arquivo .env ----
echo.
if not exist .env (
    echo Criando arquivo de configuracao (.env^)...
    copy .env.example .env >nul
    echo [OK] Arquivo .env criado
    echo.
    echo   Dica: Edite o arquivo .env para personalizar a
    echo   configuracao (porta, banco de dados, etc.^).
) else (
    echo [OK] Arquivo .env ja existe, mantendo configuracoes atuais
)

REM ---- Build das imagens Docker ----
echo.
echo Compilando a aplicacao (isso pode levar alguns minutos^)...
echo (Aguarde, nao feche esta janela^)
echo.
%COMPOSE_CMD% build
if errorlevel 1 (
    echo.
    echo ERRO: Falha ao compilar a aplicacao.
    echo Verifique os logs acima para mais detalhes.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo    Instalacao concluida com sucesso!
echo ==========================================
echo.
echo   Para iniciar a aplicacao, execute:
echo     start.bat
echo.
echo   Depois acesse:
echo     http://localhost:8080
echo.
pause
