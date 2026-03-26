#!/usr/bin/env bash
# ============================================================
# Javos - Script de Instalação (macOS)
# ============================================================
# Execute este script UMA VEZ na primeira instalação.
# Uso: ./install-macos.sh
#
# Compatível com:
#   - macOS Intel (x86_64)
#   - macOS Apple Silicon / M1 / M2 / M3 (arm64)
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Detecta arquitetura ----
ARCH="$(uname -m)"

echo ""
echo "=========================================="
echo "   Instalação do JAVOS para macOS"
echo "=========================================="
echo ""

if [ "$ARCH" = "arm64" ]; then
    echo "  Detectado: Apple Silicon (M1/M2/M3 - arm64)"
else
    echo "  Detectado: Intel (x86_64)"
fi
echo ""

# ---- Verifica macOS ----
if [ "$(uname -s)" != "Darwin" ]; then
    echo "ERRO: Este script é exclusivo para macOS."
    echo "  Para Linux, use: ./install.sh"
    echo ""
    exit 1
fi

# ---- Remove quarentena do macOS (caso os scripts tenham sido baixados) ----
xattr -d com.apple.quarantine "$SCRIPT_DIR"/*.sh 2>/dev/null || true

# ---- Verifica Homebrew (opcional, mas recomendado) ----
echo "Verificando Homebrew..."
if command -v brew &>/dev/null; then
    echo "[OK] Homebrew encontrado: $(brew --version | head -1)"
else
    echo "[AVISO] Homebrew não encontrado."
    echo ""
    echo "  O Homebrew é recomendado para instalar o Docker no macOS."
    echo "  Para instalar o Homebrew, execute:"
    echo "    /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
    echo ""
    echo "  Ou instale o Docker Desktop manualmente em:"
    echo "  https://www.docker.com/products/docker-desktop"
    echo ""
fi

# ---- Verifica Docker ----
if ! command -v docker &>/dev/null; then
    echo ""
    echo "ERRO: Docker não encontrado!"
    echo ""
    if command -v brew &>/dev/null; then
        echo "  Instale o Docker Desktop via Homebrew com:"
        echo "    brew install --cask docker"
        echo ""
        echo "  Ou acesse: https://www.docker.com/products/docker-desktop"
    else
        echo "  Instale o Docker Desktop em:"
        echo "  https://www.docker.com/products/docker-desktop"
        if [ "$ARCH" = "arm64" ]; then
            echo ""
            echo "  Atenção: Faça o download da versão para Apple Silicon (M1/M2/M3)!"
        fi
    fi
    echo ""
    exit 1
fi
echo "[OK] Docker encontrado: $(docker --version)"

# ---- Verifica Docker Compose ----
if docker compose version &>/dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &>/dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo ""
    echo "ERRO: Docker Compose não encontrado!"
    echo ""
    echo "  O Docker Compose geralmente vem incluído no Docker Desktop."
    echo "  Certifique-se de ter o Docker Desktop instalado e atualizado."
    echo ""
    exit 1
fi
echo "[OK] Docker Compose encontrado"

# ---- Verifica se o Docker está rodando ----
if ! docker info &>/dev/null; then
    echo ""
    echo "ERRO: Docker não está em execução!"
    echo ""
    echo "  Inicie o Docker Desktop pelo Launchpad ou Spotlight"
    echo "  e aguarde o ícone da baleia aparecer na barra de menu."
    echo ""
    echo "  Dica: No macOS, você pode iniciar via terminal com:"
    echo "    open -a Docker"
    echo ""
    exit 1
fi
echo "[OK] Docker está em execução"

# ---- Verifica Rosetta 2 (apenas Apple Silicon) ----
if [ "$ARCH" = "arm64" ]; then
    echo ""
    echo "Verificando compatibilidade Apple Silicon..."
    if /usr/bin/pgrep -q oahd 2>/dev/null || [ -f /Library/Apple/usr/share/rosetta/rosetta ]; then
        echo "[OK] Rosetta 2 está disponível (necessário para algumas imagens Docker)"
    else
        echo "[AVISO] Rosetta 2 não encontrada."
        echo "  Se encontrar problemas com imagens Docker, instale com:"
        echo "    softwareupdate --install-rosetta --agree-to-license"
    fi
fi

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

# No Apple Silicon, garantimos que imagens multi-plataforma sejam usadas
if [ "$ARCH" = "arm64" ]; then
    export DOCKER_DEFAULT_PLATFORM=linux/amd64
fi

$COMPOSE_CMD build

# ---- Permissões dos scripts ----
chmod +x "$SCRIPT_DIR"/*.sh 2>/dev/null || true

echo ""
echo "=========================================="
echo "   Instalação concluída com sucesso!"
echo "=========================================="
echo ""
echo "  Para iniciar a aplicação, execute:"
echo "    ./start-macos.sh"
echo ""
echo "  Depois acesse:"
echo "    http://localhost:8080"
echo ""
echo "  Outros comandos úteis:"
echo "    ./stop-macos.sh    - Parar a aplicação"
echo "    ./logs-macos.sh    - Ver logs"
echo "    ./backup-macos.sh  - Fazer backup dos dados"
echo ""
