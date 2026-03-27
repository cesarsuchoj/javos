#!/usr/bin/env bash
# ============================================================
# Javos - Iniciar Aplicação (macOS)
# ============================================================
# Uso: ./start-macos.sh
#
# Compatível com macOS Intel e Apple Silicon (M1/M2/M3)
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

ARCH="$(uname -m)"

# ---- Verifica macOS ----
if [ "$(uname -s)" != "Darwin" ]; then
    echo "ERRO: Este script é exclusivo para macOS."
    echo "  Para Linux, use: ./start.sh"
    echo ""
    exit 1
fi

# ---- Detecta docker compose ----
if docker compose version &>/dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &>/dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo "ERRO: Docker Compose não encontrado. Execute ./install-macos.sh primeiro."
    exit 1
fi

# ---- Verifica se o Docker está rodando ----
if ! docker info &>/dev/null; then
    echo ""
    echo "ERRO: Docker não está em execução!"
    echo ""
    echo "  Iniciando o Docker Desktop automaticamente..."
    open -a Docker
    echo "  Aguardando o Docker iniciar (pode levar até 30 segundos)..."
    WAIT=0
    while ! docker info &>/dev/null; do
        if [ $WAIT -ge 60 ]; then
            echo ""
            echo "  O Docker demorou demais para iniciar."
            echo "  Abra o Docker Desktop manualmente e tente novamente."
            exit 1
        fi
        sleep 3
        WAIT=$((WAIT + 3))
        printf "."
    done
    echo ""
    echo "[OK] Docker iniciado!"
fi

# ---- Verifica se .env existe ----
if [ ! -f .env ]; then
    echo "Arquivo .env não encontrado. Execute ./install-macos.sh primeiro."
    exit 1
fi

# Lê a porta configurada (padrão: 8080)
APP_PORT=$(grep -E '^APP_PORT=' .env | cut -d'=' -f2 | tr -d '[:space:]' || echo "8080")
APP_PORT="${APP_PORT:-8080}"

# No Apple Silicon, garantimos que imagens multi-plataforma sejam usadas
if [ "$ARCH" = "arm64" ]; then
    export DOCKER_DEFAULT_PLATFORM=linux/amd64
fi

echo ""
echo "=========================================="
echo "   Iniciando JAVOS"
echo "=========================================="
echo ""
echo "Iniciando containers..."
$COMPOSE_CMD up -d

echo ""
echo "Aguardando a aplicação ficar pronta..."

# Aguarda até 120 segundos pela aplicação responder
MAX_WAIT=120
ELAPSED=0
until curl -sf "http://localhost:${APP_PORT}/" >/dev/null 2>&1; do
    if [ $ELAPSED -ge $MAX_WAIT ]; then
        echo ""
        echo "  A aplicação ainda não respondeu após ${MAX_WAIT}s."
        echo "  Verifique os logs com: ./logs-macos.sh"
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
echo "   JAVOS está rodando!"
echo "=========================================="
echo ""
echo "  Acesse: http://localhost:${APP_PORT}"
echo ""

# Abre automaticamente no navegador padrão do macOS
echo "  Abrindo no navegador..."
open "http://localhost:${APP_PORT}" 2>/dev/null || true

echo ""
echo "  Para ver os logs:  ./logs-macos.sh"
echo "  Para parar:        ./stop-macos.sh"
echo "  Para backup:       ./backup-macos.sh"
echo ""
