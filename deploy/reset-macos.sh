#!/usr/bin/env bash
# ============================================================
# Javos - Resetar Banco de Dados (macOS)
# ============================================================
# ATENÇÃO: Este script apaga TODOS os dados do banco!
# Use apenas se quiser começar do zero.
# Uso: ./reset-macos.sh
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Verifica macOS ----
if [ "$(uname -s)" != "Darwin" ]; then
    echo "ERRO: Este script é exclusivo para macOS."
    echo "  Para Linux, use: ./reset.sh"
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
echo "   Reset do Banco de Dados - JAVOS"
echo "=========================================="
echo ""
echo "  ATENÇÃO: Esta operação apagará TODOS os"
echo "  dados do banco de dados!"
echo ""

# Exibe diálogo nativo do macOS para confirmação
if command -v osascript &>/dev/null; then
    RESULT=$(osascript -e 'button returned of (display dialog "Esta operação apagará TODOS os dados do banco de dados do JAVOS.\n\nTem certeza que deseja continuar?" buttons {"Cancelar", "Confirmar"} default button "Cancelar" with icon caution)' 2>/dev/null || echo "Cancelar")
    if [ "$RESULT" != "Confirmar" ]; then
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
echo "Fazendo backup de segurança antes de resetar..."
if [ -f "$SCRIPT_DIR/backup-macos.sh" ]; then
    "$SCRIPT_DIR/backup-macos.sh" || echo "  [AVISO] Não foi possível fazer backup automático. Continuando..."
fi

echo ""
echo "Parando a aplicação..."
$COMPOSE_CMD down

echo "Removendo volumes de dados..."
$COMPOSE_CMD down -v

echo ""
echo "[OK] Banco de dados resetado com sucesso."
echo ""
echo "  Execute ./start-macos.sh para iniciar novamente."
echo ""
