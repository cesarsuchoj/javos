@echo off
REM ============================================================
REM Javos - Iniciar Aplicacao (Windows)
REM ============================================================
REM Uso: Duplo clique em start.bat  -ou-  execute no prompt
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
        echo ERRO: Docker Compose nao encontrado. Execute install.bat primeiro.
        pause
        exit /b 1
    )
)

REM ---- Verifica se .env existe ----
if not exist .env (
    echo Arquivo .env nao encontrado. Execute install.bat primeiro.
    pause
    exit /b 1
)

REM ---- Le a porta configurada (padrao: 8080) ----
set APP_PORT=8080
for /f "tokens=2 delims==" %%i in ('findstr /b "APP_PORT=" .env 2^>nul') do set APP_PORT=%%i

echo.
echo ==========================================
echo    Iniciando JAVOS
echo ==========================================
echo.
echo Iniciando containers...
%COMPOSE_CMD% up -d
if errorlevel 1 (
    echo.
    echo ERRO: Falha ao iniciar a aplicacao.
    echo Verifique os logs com: logs.bat
    pause
    exit /b 1
)

echo.
echo Aguardando a aplicacao ficar pronta...
set /a ELAPSED=0
set /a MAX_WAIT=120

:WAIT_LOOP
curl -sf "http://localhost:%APP_PORT%/" >nul 2>&1
if not errorlevel 1 goto READY
if !ELAPSED! geq !MAX_WAIT! goto TIMEOUT
timeout /t 5 /nobreak >nul
set /a ELAPSED+=5
<nul set /p ".=."
goto WAIT_LOOP

:TIMEOUT
echo.
echo   A aplicacao ainda nao respondeu apos %MAX_WAIT%s.
echo   Verifique os logs com: logs.bat
pause
exit /b 1

:READY
echo.
echo.
echo ==========================================
echo    JAVOS esta rodando!
echo ==========================================
echo.
echo   Acesse: http://localhost:%APP_PORT%
echo.
echo   Para ver os logs:  logs.bat
echo   Para parar:        stop.bat
echo.
pause
