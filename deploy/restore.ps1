# ============================================================
# Javos - Restaurar Backup (Windows PowerShell)
# ============================================================
# Restaura um backup do banco de dados a partir de um arquivo .tar.gz
# Uso: .\restore.ps1 <arquivo_de_backup>
#
# Exemplos:
#   .\restore.ps1 backups\javos-backup-20240101_120000.tar.gz
#   .\restore.ps1 "C:\Users\Usuario\Desktop\javos-backup-20240101.tar.gz"
#
# ATENCAO: A restauracao SUBSTITUI todos os dados atuais!
# ============================================================

#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

param(
    [string]$BackupFile = ""
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

$VolumeName = "deploy_javos-data"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Restaurar Backup - JAVOS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# ---- Verifica argumento ----
if ([string]::IsNullOrEmpty($BackupFile)) {
    Write-Host "  Nenhum arquivo de backup especificado." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  Uso: .\restore.ps1 <arquivo_de_backup>"
    Write-Host ""
    Write-Host "  Backups disponíveis em .\backups\:"
    $backupsDir = Join-Path $ScriptDir "backups"
    if (Test-Path $backupsDir) {
        $backups = Get-ChildItem -Path $backupsDir -Filter "*.tar.gz" | Sort-Object LastWriteTime -Descending
        if ($backups.Count -gt 0) {
            $backups | ForEach-Object {
                $size = [math]::Round($_.Length / 1KB, 1)
                Write-Host "    $($_.Name) ($size KB)"
            }
        } else {
            Write-Host "    (nenhum backup encontrado)"
        }
    } else {
        Write-Host "    (pasta de backups nao encontrada)"
    }
    Write-Host ""
    Write-Host "  Dica: Arraste o arquivo .ps1 sobre este script, ou execute:" -ForegroundColor Yellow
    Write-Host "    .\restore.ps1 'caminho\para\backup.tar.gz'" -ForegroundColor Cyan
    Write-Host ""
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Resolve caminho relativo
if (-not [System.IO.Path]::IsPathRooted($BackupFile)) {
    $BackupFile = Join-Path $ScriptDir $BackupFile
}

# ---- Verifica se o arquivo existe ----
if (-not (Test-Path $BackupFile)) {
    Write-Host "ERRO: Arquivo de backup nao encontrado:" -ForegroundColor Red
    Write-Host "  $BackupFile" -ForegroundColor Red
    Write-Host ""
    Read-Host "Pressione Enter para sair"
    exit 1
}

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

$BackupName = Split-Path -Leaf $BackupFile
$BackupDirPath = Split-Path -Parent $BackupFile
$BackupSize = [math]::Round((Get-Item $BackupFile).Length / 1KB, 1)

Write-Host "  Arquivo:  $BackupName" -ForegroundColor White
Write-Host "  Tamanho:  $BackupSize KB" -ForegroundColor White
Write-Host ""
Write-Host "  ATENCAO: Esta operacao substituira TODOS os" -ForegroundColor Red
Write-Host "  dados atuais pelos dados do backup!" -ForegroundColor Red
Write-Host ""

$confirm = Read-Host "  Tem certeza? Digite 'sim' para confirmar"

if ($confirm -ne "sim") {
    Write-Host ""
    Write-Host "  Operacao cancelada." -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Pressione Enter para sair"
    exit 0
}

Write-Host ""
Write-Host "Parando a aplicacao..."
try {
    if ($ComposeCmd -eq "docker compose") {
        & docker compose down 2>&1 | Out-Null
    } else {
        & docker-compose down 2>&1 | Out-Null
    }
} catch {}

Write-Host "Restaurando dados do backup..."

# Garante que o volume existe
& docker volume create $VolumeName 2>&1 | Out-Null

# Converte o caminho do Windows para formato Docker
$BackupDirDocker = $BackupDirPath.Replace("\", "/")

& docker run --rm `
    -v "${VolumeName}:/data" `
    -v "${BackupDirDocker}:/backup:ro" `
    alpine:3 `
    sh -c "rm -rf /data/* /data/..?* /data/.[!.]* 2>/dev/null; tar xzf `"/backup/$BackupName`" -C /data"

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERRO: Falha ao restaurar o backup." -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "   Backup restaurado com sucesso!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Execute .\start.ps1 para iniciar a aplicacao." -ForegroundColor White
Write-Host ""
Read-Host "Pressione Enter para sair"
