#!/usr/bin/env bash
# ============================================================
# Javos - Script de Instalação (Linux / macOS)
# ============================================================
# Execute este script UMA VEZ na primeira instalação.
# Uso: ./install.sh
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo "=========================================="
echo "   Instalação do JAVOS"
echo "=========================================="
echo ""

# ---- Verifica Docker ----
if ! command -v docker &>/dev/null; then
    echo "ERRO: Docker não encontrado!"
    echo ""
    echo "  Instale o Docker Desktop em:"
    echo "  https://www.docker.com/products/docker-desktop"
    echo ""
    exit 1
fi
echo "[OK] Docker encontrado: $(docker --version)"

# ---- Verifica Docker Compose ----
if docker compose version &>/dev/null 2>&1>/dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &>/dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo ""
    echo "ERRO: Docker Compose não encontrado!"
    echo ""
    echo "  Instale o Docker Desktop (inclui Docker Compose) em:"
    echo "  https://www.docker.com/products/docker-desktop"
    echo ""
    exit 1
fi
echo "[OK] Docker Compose encontrado"

# ---- Verifica se o Docker está rodando ----
if ! docker info &>/dev/null; then
    echo ""
    echo "ERRO: Docker não está em execução!"
    echo ""
    echo "  Inicie o Docker Desktop e tente novamente."
    echo ""
    exit 1
fi
echo "[OK] Docker está em execução"

# ---- Cria arquivo .env ----
echo ""
if [ ! -f .env ]; then
    echo "Criando arquivo de configuração (.env)..."
    cp .env.example .env
    echo "[OK] Arquivo .env criado"
    echo ""
    echo "  Dica: Edite o arquivo .env para personalizar a"
    echo "  configuração (porta, banco de dados, etc.)."
else
    echo "[OK] Arquivo .env já existe, mantendo configurações atuais"
fi

# ---- Build das imagens Docker ----
echo ""
echo "Compilando a aplicação (isso pode levar alguns minutos)..."
echo "(Aguarde, não feche esta janela)"
echo ""
$COMPOSE_CMD build

# ---- Permissões dos scripts ----
chmod +x install.sh start.sh stop.sh logs.sh reset.sh 2>/dev/null || true

echo ""
echo "=========================================="
echo "   Instalacao concluida com sucesso!"
echo "=========================================="
echo ""
echo "  Para iniciar a aplicacao, execute:"
echo "    ./start.sh"
echo ""
echo "  Depois acesse:"
echo "    http://localhost:8080"
echo ""
