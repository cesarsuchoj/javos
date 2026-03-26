#!/usr/bin/env bash
# ============================================================
# Javos - Parar Aplicação (macOS)
# ============================================================
# Uso: ./stop-macos.sh
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Verifica macOS ----
if [ "$(uname -s)" != "Darwin" ]; then
    echo "ERRO: Este script é exclusivo para macOS."
    echo "  Para Linux, use: ./stop.sh"
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
echo "   Parando JAVOS"
echo "=========================================="
echo ""
$COMPOSE_CMD down

echo ""
echo "[OK] Aplicação parada com sucesso."
echo ""
echo "  Para iniciar novamente: ./start-macos.sh"
echo ""
