#!/usr/bin/env bash
# ============================================================
# Javos - Ver Logs (Linux / macOS)
# ============================================================
# Uso: ./logs.sh
# Pressione Ctrl+C para sair.
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Detecta docker compose ----
if docker compose version &>/dev/null 2>&1>/dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &>/dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo "ERRO: Docker Compose nao encontrado."
    exit 1
fi

echo ""
echo "=========================================="
echo "   Logs do JAVOS  (Ctrl+C para sair)"
echo "=========================================="
echo ""
$COMPOSE_CMD logs --follow --tail=100
