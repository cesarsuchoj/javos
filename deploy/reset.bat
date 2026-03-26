@echo off
REM ============================================================
REM Javos - Resetar Banco de Dados (Windows)
REM ============================================================
REM ATENCAO: Este script apaga TODOS os dados do banco!
REM Use apenas se quiser comecar do zero.
REM Uso: Duplo clique em reset.bat  -ou-  execute no prompt
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
echo    Reset do Banco de Dados - JAVOS
echo ==========================================
echo.
echo   ATENCAO: Esta operacao apagara TODOS os
echo   dados do banco de dados!
echo.
set /p CONFIRM=   Tem certeza? Digite 'sim' para confirmar: 

if /i not "%CONFIRM%"=="sim" (
    echo.
    echo   Operacao cancelada.
    echo.
    pause
    exit /b 0
)

echo.
echo Parando a aplicacao...
%COMPOSE_CMD% down

echo Removendo volumes de dados...
%COMPOSE_CMD% down -v

echo.
echo [OK] Banco de dados resetado com sucesso.
echo.
echo   Execute start.bat para iniciar novamente.
echo.
pause
