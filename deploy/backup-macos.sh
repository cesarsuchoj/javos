#!/usr/bin/env bash
# ============================================================
# Javos - Backup dos Dados (macOS)
# ============================================================
# Cria um backup completo do banco de dados em um arquivo .tar.gz
# Uso: ./backup-macos.sh [pasta_destino]
#
# Exemplos:
#   ./backup-macos.sh                        (salva em ./backups/)
#   ./backup-macos.sh ~/Desktop              (salva na Área de Trabalho)
#   ./backup-macos.sh /Volumes/ExternalDrive (salva em disco externo)
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Verifica macOS ----
if [ "$(uname -s)" != "Darwin" ]; then
    echo "ERRO: Este script é exclusivo para macOS."
    echo "  Para Linux, use: ./backup.sh"
    echo ""
    exit 1
fi

# ---- Configuração ----
BACKUP_DIR="${1:-$SCRIPT_DIR/backups}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="javos-backup-${TIMESTAMP}.tar.gz"
VOLUME_NAME="deploy_javos-data"

echo ""
echo "=========================================="
echo "   Backup do JAVOS"
echo "=========================================="
echo ""

# ---- Verifica se o volume existe ----
if ! docker volume inspect "$VOLUME_NAME" &>/dev/null; then
    echo "ERRO: Volume de dados '$VOLUME_NAME' não encontrado."
    echo "  A aplicação precisa ter sido inicializada ao menos uma vez."
    echo ""
    exit 1
fi

# ---- Cria pasta de destino ----
mkdir -p "$BACKUP_DIR"

echo "  Data/hora:    $(date '+%d/%m/%Y %H:%M:%S')"
echo "  Arquivo:      $BACKUP_FILE"
echo "  Destino:      $BACKUP_DIR"
echo ""
echo "Criando backup..."

# Cria o backup usando um container temporário Alpine
docker run --rm \
    -v "${VOLUME_NAME}:/data:ro" \
    -v "${BACKUP_DIR}:/backup" \
    alpine:3 \
    tar czf "/backup/${BACKUP_FILE}" -C /data .

BACKUP_SIZE=$(du -sh "$BACKUP_DIR/$BACKUP_FILE" | cut -f1)

echo ""
echo "=========================================="
echo "   Backup concluído!"
echo "=========================================="
echo ""
echo "  Arquivo: $BACKUP_DIR/$BACKUP_FILE"
echo "  Tamanho: $BACKUP_SIZE"
echo ""
echo "  Para restaurar este backup, execute:"
echo "    ./restore-macos.sh \"$BACKUP_DIR/$BACKUP_FILE\""
echo ""

# Abre a pasta do backup no Finder
if command -v open &>/dev/null; then
    open "$BACKUP_DIR"
fi
