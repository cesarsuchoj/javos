#!/usr/bin/env bash
# ============================================================
# Javos - Ver Logs (macOS)
# ============================================================
# Uso: ./logs-macos.sh
# Pressione Ctrl+C para sair.
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# ---- Verifica macOS ----
if [ "$(uname -s)" != "Darwin" ]; then
    echo "ERRO: Este script é exclusivo para macOS."
    echo "  Para Linux, use: ./logs.sh"
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

echo ""
echo "=========================================="
echo "   Logs do JAVOS  (Ctrl+C para sair)"
echo "=========================================="
echo ""
$COMPOSE_CMD logs --follow --tail=100
