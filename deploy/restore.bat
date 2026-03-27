@echo off
REM ============================================================
REM Javos - Restaurar Backup (Windows)
REM ============================================================
REM Restaura um backup do banco de dados a partir de um arquivo .tar.gz
REM Uso: Arraste o arquivo .tar.gz para cima deste script
REM      -ou- execute no prompt:
REM      restore.bat "C:\caminho\para\javos-backup-XXXXXXXX.tar.gz"
REM
REM ATENCAO: A restauracao SUBSTITUI todos os dados atuais!
REM ============================================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

set VOLUME_NAME=deploy_javos-data

echo.
echo ==========================================
echo    Restaurar Backup - JAVOS
echo ==========================================
echo.

REM ---- Verifica argumento ----
if "%~1"=="" (
    echo   Nenhum arquivo de backup especificado.
    echo.
    echo   Uso: %~nx0 ^<arquivo_de_backup^>
    echo.
    echo   Backups disponíveis em .\backups\:
    if exist "%~dp0backups\*.tar.gz" (
        dir /b "%~dp0backups\*.tar.gz" 2>nul
    ) else (
        echo     (nenhum backup encontrado^)
    )
    echo.
    echo   Dica: Arraste um arquivo .tar.gz para cima deste script.
    echo.
    pause
    exit /b 1
)

set BACKUP_FILE=%~1

REM ---- Verifica se o arquivo existe ----
if not exist "%BACKUP_FILE%" (
    echo ERRO: Arquivo de backup nao encontrado:
    echo   %BACKUP_FILE%
    echo.
    pause
    exit /b 1
)

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

echo   Arquivo:  %~nx1
echo.
echo   ATENCAO: Esta operacao substituira TODOS os
echo   dados atuais pelos dados do backup!
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
%COMPOSE_CMD% down 2>nul

echo Restaurando dados do backup...

REM Garante que o volume existe
docker volume create %VOLUME_NAME% >nul 2>&1

REM Extrai o caminho e nome do arquivo de backup
for %%F in ("%BACKUP_FILE%") do (
    set BACKUP_DIR_PATH=%%~dpF
    set BACKUP_NAME=%%~nxF
)

REM Remove trailing backslash se existir
set BACKUP_DIR_PATH=%BACKUP_DIR_PATH:~0,-1%

docker run --rm ^
    -v "%VOLUME_NAME%:/data" ^
    -v "%BACKUP_DIR_PATH%:/backup:ro" ^
    alpine:3 ^
    sh -c "rm -rf /data/* /data/..?* /data/.[!.]* 2>/dev/null; tar xzf \"/backup/%BACKUP_NAME%\" -C /data"

if errorlevel 1 (
    echo.
    echo ERRO: Falha ao restaurar o backup.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo    Backup restaurado com sucesso!
echo ==========================================
echo.
echo   Execute start.bat para iniciar a aplicacao.
echo.
pause
