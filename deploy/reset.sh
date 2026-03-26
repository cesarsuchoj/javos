#!/usr/bin/env bash
# ============================================================
# Javos - Resetar Banco de Dados (Linux / macOS)
# ============================================================
# ATENÇÃO: Este script apaga TODOS os dados do banco!
# Use apenas se quiser começar do zero.
# Uso: ./reset.sh
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
echo "   Reset do Banco de Dados - JAVOS"
echo "=========================================="
echo ""
echo "  ATENCAO: Esta operacao apagara TODOS os"
echo "  dados do banco de dados!"
echo ""
read -r -p "  Tem certeza? Digite 'sim' para confirmar: " CONFIRM

if [ "$CONFIRM" != "sim" ]; then
    echo ""
    echo "  Operacao cancelada."
    echo ""
    exit 0
fi

echo ""
echo "Parando a aplicacao..."
$COMPOSE_CMD down

echo "Removendo volumes de dados..."
$COMPOSE_CMD down -v

echo ""
echo "[OK] Banco de dados resetado com sucesso."
echo ""
echo "  Execute ./start.sh para iniciar novamente."
echo ""
