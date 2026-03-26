@echo off
REM ============================================================
REM Javos - Ver Logs (Windows)
REM ============================================================
REM Uso: Duplo clique em logs.bat  -ou-  execute no prompt
REM Pressione Ctrl+C para sair.
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
echo    Logs do JAVOS  (Ctrl+C para sair)
echo ==========================================
echo.
%COMPOSE_CMD% logs --follow --tail=100
