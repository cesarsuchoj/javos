@echo off
REM ============================================================
REM Javos - Parar Aplicacao (Windows)
REM ============================================================
REM Uso: Duplo clique em stop.bat  -ou-  execute no prompt
REM ============================================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

REM ---- Detecta docker compose ----
docker compose version >nul 2>&1
if not errorlevel 1 (
    set COMPOSE_CMD=docker compose
) else (
    docker-compose --version >nul 2>&1
    if not errorlevel 1 (
        set COMPOSE_CMD=docker-compose
    ) else (
        echo ERRO: Docker Compose nao encontrado.
        pause
        exit /b 1
    )
)

echo.
echo ==========================================
echo    Parando JAVOS
echo ==========================================
echo.
%COMPOSE_CMD% down
if errorlevel 1 (
    echo.
    echo ERRO: Falha ao parar a aplicacao.
    pause
    exit /b 1
)

echo.
echo [OK] Aplicacao parada com sucesso.
echo.
pause
