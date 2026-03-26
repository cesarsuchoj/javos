#!/usr/bin/env bash
# ============================================================
# Javos - Restaurar Backup (macOS)
# ============================================================
# Restaura um backup do banco de dados a partir de um arquivo .tar.gz
# Uso: ./restore-macos.sh <arquivo_de_backup>
#
# Exemplos:
#   ./restore-macos.sh backups/javos-backup-20240101_120000.tar.gz
#   ./restore-macos.sh ~/Desktop/javos-backup-20240101_120000.tar.gz
#
# ATENÇÃO: A restauração SUBSTITUI todos os dados atuais!
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Verifica macOS ----
if [ "$(uname -s)" != "Darwin" ]; then
    echo "ERRO: Este script é exclusivo para macOS."
    echo "  Para Linux, use: ./restore.sh"
    echo ""
    exit 1
fi

VOLUME_NAME="deploy_javos-data"

# ---- Verifica argumento ----
if [ $# -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "   Restaurar Backup - JAVOS"
    echo "=========================================="
    echo ""
    echo "  Uso: $0 <arquivo_de_backup>"
    echo ""
    echo "  Backups disponíveis em ./backups/:"
    if find "$SCRIPT_DIR/backups/" -name "*.tar.gz" -maxdepth 1 2>/dev/null | grep -q .; then
        find "$SCRIPT_DIR/backups/" -name "*.tar.gz" -maxdepth 1 2>/dev/null | sort | while read -r f; do
            echo "    $(basename "$f")"
        done
    else
        echo "    (nenhum backup encontrado)"
    fi
    echo ""
    exit 1
fi

BACKUP_FILE="$1"

# Resolve caminho relativo
if [[ "$BACKUP_FILE" != /* ]]; then
    BACKUP_FILE="$SCRIPT_DIR/$BACKUP_FILE"
fi

# ---- Verifica se o arquivo existe ----
if [ ! -f "$BACKUP_FILE" ]; then
    echo ""
    echo "ERRO: Arquivo de backup não encontrado: $BACKUP_FILE"
    echo ""
    exit 1
fi

# ---- Detecta docker compose ----
if docker compose version &>/dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &>/dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo "ERRO: Docker Compose não encontrado."
    exit 1
fi

BACKUP_SIZE=$(du -sh "$BACKUP_FILE" | cut -f1)

echo ""
echo "=========================================="
echo "   Restaurar Backup - JAVOS"
echo "=========================================="
echo ""
echo "  Arquivo:  $(basename "$BACKUP_FILE")"
echo "  Tamanho:  $BACKUP_SIZE"
echo ""
echo "  ATENÇÃO: Esta operação substituirá TODOS os"
echo "  dados atuais pelos dados do backup!"
echo ""

# Exibe diálogo nativo do macOS para confirmação
if command -v osascript &>/dev/null; then
    RESULT=$(osascript -e 'button returned of (display dialog "Esta operação substituirá TODOS os dados atuais pelos dados do backup.\n\nTem certeza que deseja continuar?" buttons {"Cancelar", "Restaurar"} default button "Cancelar" with icon caution)' 2>/dev/null || echo "Cancelar")
    if [ "$RESULT" != "Restaurar" ]; then
        echo "  Operação cancelada."
        echo ""
        exit 0
    fi
else
    read -r -p "  Tem certeza? Digite 'sim' para confirmar: " CONFIRM
    if [ "$CONFIRM" != "sim" ]; then
        echo ""
        echo "  Operação cancelada."
        echo ""
        exit 0
    fi
fi

echo ""
echo "Parando a aplicação..."
$COMPOSE_CMD down 2>/dev/null || true

echo "Restaurando dados do backup..."

# Garante que o volume existe
docker volume create "$VOLUME_NAME" &>/dev/null || true

# Restaura o backup
BACKUP_DIR="$(dirname "$BACKUP_FILE")"
BACKUP_NAME="$(basename "$BACKUP_FILE")"

docker run --rm \
    -v "${VOLUME_NAME}:/data" \
    -v "${BACKUP_DIR}:/backup:ro" \
    alpine:3 \
    sh -c "rm -rf /data/* /data/..?* /data/.[!.]* 2>/dev/null; tar xzf \"/backup/${BACKUP_NAME}\" -C /data"

echo ""
echo "=========================================="
echo "   Backup restaurado com sucesso!"
echo "=========================================="
echo ""
echo "  Execute ./start-macos.sh para iniciar a aplicação."
echo ""
