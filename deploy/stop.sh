#!/usr/bin/env bash
# ============================================================
# Javos - Parar Aplicação (Linux / macOS)
# ============================================================
# Uso: ./stop.sh
# ============================================================

set -euo pipefail

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
echo "   Parando JAVOS"
echo "=========================================="
echo ""
$COMPOSE_CMD down

echo ""
echo "[OK] Aplicacao parada com sucesso."
echo ""
