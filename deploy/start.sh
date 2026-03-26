#!/usr/bin/env bash
# ============================================================
# Javos - Iniciar Aplicação (Linux / macOS)
# ============================================================
# Uso: ./start.sh
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
    echo "ERRO: Docker Compose nao encontrado. Execute ./install.sh primeiro."
    exit 1
fi

# ---- Verifica se .env existe ----
if [ ! -f .env ]; then
    echo "Arquivo .env nao encontrado. Execute ./install.sh primeiro."
    exit 1
fi

# Lê a porta configurada (padrão: 8080)
APP_PORT=$(grep -E '^APP_PORT=' .env | cut -d'=' -f2 | tr -d '[:space:]' || echo "8080")
APP_PORT="${APP_PORT:-8080}"

echo ""
echo "=========================================="
echo "   Iniciando JAVOS"
echo "=========================================="
echo ""
echo "Iniciando containers..."
$COMPOSE_CMD up -d

echo ""
echo "Aguardando a aplicacao ficar pronta..."

# Aguarda até 120 segundos pela aplicação responder
MAX_WAIT=120
ELAPSED=0
until curl -sf "http://localhost:${APP_PORT}/" >/dev/null 2>&1 || wget -q --spider "http://localhost:${APP_PORT}/" >/dev/null 2>&1; do
    if [ $ELAPSED -ge $MAX_WAIT ]; then
        echo ""
        echo "  A aplicacao ainda nao respondeu apos ${MAX_WAIT}s."
        echo "  Verifique os logs com: ./logs.sh"
        echo ""
        exit 1
    fi
    printf "."
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done

echo ""
echo ""
echo "=========================================="
echo "   JAVOS esta rodando!"
echo "=========================================="
echo ""
echo "  Acesse: http://localhost:${APP_PORT}"
echo ""
echo "  Para ver os logs:  ./logs.sh"
echo "  Para parar:        ./stop.sh"
echo ""
