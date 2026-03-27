# ============================================================
# Javos - Backup dos Dados (Windows PowerShell)
# ============================================================
# Cria um backup completo do banco de dados em um arquivo .tar.gz
# Uso: .\backup.ps1 [pasta_destino]
#
# Exemplos:
#   .\backup.ps1                                 (salva em .\backups\)
#   .\backup.ps1 C:\Users\Usuario\Desktop\backups
# ============================================================

#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

param(
    [string]$BackupDir = ""
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

if ([string]::IsNullOrEmpty($BackupDir)) {
    $BackupDir = Join-Path $ScriptDir "backups"
}

$VolumeName = "deploy_javos-data"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = "javos-backup-$Timestamp.tar.gz"

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

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   Backup do JAVOS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# ---- Verifica se o volume existe ----
try {
    & docker volume inspect $VolumeName 2>&1 | Out-Null
} catch {
    Write-Host "ERRO: Volume de dados '$VolumeName' nao encontrado." -ForegroundColor Red
    Write-Host "  A aplicacao precisa ter sido inicializada ao menos uma vez." -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Pressione Enter para sair"
    exit 1
}

# ---- Cria pasta de destino ----
New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null

Write-Host "  Data/hora:    $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')"
Write-Host "  Arquivo:      $BackupFile"
Write-Host "  Destino:      $BackupDir"
Write-Host ""
Write-Host "Criando backup..."

# Converte o caminho do Windows para formato Docker (usa forward slashes)
$BackupDirDocker = $BackupDir.Replace("\", "/")

& docker run --rm `
    -v "${VolumeName}:/data:ro" `
    -v "${BackupDirDocker}:/backup" `
    alpine:3 `
    tar czf "/backup/$BackupFile" -C /data .

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERRO: Falha ao criar o backup." -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

$BackupPath = Join-Path $BackupDir $BackupFile
$BackupSize = (Get-Item $BackupPath).Length
$BackupSizeKB = [math]::Round($BackupSize / 1KB, 1)

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "   Backup concluido!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Arquivo: $BackupDir\$BackupFile" -ForegroundColor White
Write-Host "  Tamanho: $BackupSizeKB KB" -ForegroundColor White
Write-Host ""
Write-Host "  Para restaurar este backup, execute:" -ForegroundColor White
Write-Host "    .\restore.ps1 `"$BackupDir\$BackupFile`"" -ForegroundColor Cyan
Write-Host ""

# Abre a pasta de backups no Explorer
Start-Process "explorer.exe" $BackupDir

Read-Host "Pressione Enter para sair"
