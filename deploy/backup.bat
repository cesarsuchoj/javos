@echo off
REM ============================================================
REM Javos - Backup dos Dados (Windows)
REM ============================================================
REM Cria um backup completo do banco de dados em um arquivo .tar.gz
REM Uso: Duplo clique em backup.bat  -ou-  execute no prompt
REM
REM Para especificar pasta de destino:
REM   backup.bat C:\Users\Usuario\Desktop\backups
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

REM ---- Configuracao ----
if "%~1"=="" (
    set BACKUP_DIR=%~dp0backups
) else (
    set BACKUP_DIR=%~1
)

set VOLUME_NAME=deploy_javos-data

REM ---- Gera timestamp ----
for /f "tokens=2 delims==" %%a in ('wmic os get localdatetime /value') do set DATETIME=%%a
set TIMESTAMP=%DATETIME:~0,8%_%DATETIME:~8,6%
set BACKUP_FILE=javos-backup-%TIMESTAMP%.tar.gz

echo.
echo ==========================================
echo    Backup do JAVOS
echo ==========================================
echo.

REM ---- Verifica se o volume existe ----
docker volume inspect %VOLUME_NAME% >nul 2>&1
if errorlevel 1 (
    echo ERRO: Volume de dados '%VOLUME_NAME%' nao encontrado.
    echo   A aplicacao precisa ter sido inicializada ao menos uma vez.
    echo.
    pause
    exit /b 1
)

REM ---- Cria pasta de destino ----
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo   Data/hora:    %DATE% %TIME%
echo   Arquivo:      %BACKUP_FILE%
echo   Destino:      %BACKUP_DIR%
echo.
echo Criando backup...

REM Cria o backup usando um container temporario Alpine
docker run --rm ^
    -v "%VOLUME_NAME%:/data:ro" ^
    -v "%BACKUP_DIR%:/backup" ^
    alpine:3 ^
    tar czf "/backup/%BACKUP_FILE%" -C /data .

if errorlevel 1 (
    echo.
    echo ERRO: Falha ao criar o backup.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo    Backup concluido!
echo ==========================================
echo.
echo   Arquivo: %BACKUP_DIR%\%BACKUP_FILE%
echo.
echo   Para restaurar este backup, execute:
echo     restore.bat "%BACKUP_DIR%\%BACKUP_FILE%"
echo.

REM Abre a pasta de backups no Explorer
explorer "%BACKUP_DIR%"

pause
