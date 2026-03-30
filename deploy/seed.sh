#!/usr/bin/env bash
# ============================================================
# Javos - Seed do Banco de Dados (Desenvolvimento)
# ============================================================
# Popula o banco de dados com dados de exemplo para facilitar
# o desenvolvimento e testes locais.
#
# Uso:
#   ./seed.sh                        (seed no container local)
#   ./seed.sh -f meu-seed.sql        (arquivo SQL personalizado)
#   ./seed.sh -e                     (apenas dados de exemplo extras)
#
# PRÉ-REQUISITO: A aplicação deve estar rodando.
#   ./start.sh   (ou start.bat / start.ps1)
#
# AVISO: Este script é destinado APENAS a ambientes de
#   desenvolvimento. Nunca execute em staging ou produção!
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

CONTAINER_NAME="javos-app"
VOLUME_NAME="deploy_javos-data"
DB_PATH="/app/data/javos.db"
SEED_FILE=""
EXTRA_ONLY=false

# ---- Parse de argumentos ----
while getopts "f:eh" opt; do
    case $opt in
        f) SEED_FILE="$OPTARG" ;;
        e) EXTRA_ONLY=true ;;
        h)
            echo "Uso: $0 [-f arquivo.sql] [-e]"
            echo "  -f  Arquivo SQL personalizado para seed"
            echo "  -e  Apenas dados de exemplo extras (sem reset)"
            exit 0
            ;;
        *) echo "Opção inválida: -$OPTARG"; exit 1 ;;
    esac
done

# ---- Verifica ambiente ----
ENV="${SPRING_PROFILES_ACTIVE:-dev}"
if [[ "$ENV" == "prod" || "$ENV" == "production" ]]; then
    echo "ERRO: O seed não deve ser executado em ambiente de produção!"
    echo "  SPRING_PROFILES_ACTIVE=$ENV"
    exit 1
fi

echo ""
echo "=========================================="
echo "   Javos - Seed do Banco de Dados"
echo "   Ambiente: ${ENV}"
echo "=========================================="
echo ""

# ---- Verifica se a aplicação está rodando ----
if ! docker ps --filter "name=${CONTAINER_NAME}" --filter "status=running" --format "{{.Names}}" | grep -q "${CONTAINER_NAME}"; then
    echo "AVISO: Container '${CONTAINER_NAME}' não está rodando."
    echo "  Execute './start.sh' primeiro."
    echo ""
    # Tenta aplicar seed diretamente no volume mesmo sem container
    echo "  Tentando aplicar seed diretamente no volume Docker..."
fi

# ---- SQL de seed padrão ----
DEFAULT_SEED_SQL=$(cat <<'EOF'
-- ============================================================
-- Javos - Dados de Seed (Desenvolvimento)
-- ============================================================
-- Inserção de dados de exemplo para facilitar o desenvolvimento.
-- Usa INSERT OR IGNORE para evitar duplicatas em re-execuções.
-- ============================================================

-- Clientes de exemplo
INSERT OR IGNORE INTO clients (id, name, email, phone, document, created_at, updated_at)
VALUES
  (100, 'João Silva',    'joao.silva@exemplo.com',    '(11) 99999-0001', '111.222.333-01', datetime('now'), datetime('now')),
  (101, 'Maria Souza',   'maria.souza@exemplo.com',   '(21) 99999-0002', '222.333.444-02', datetime('now'), datetime('now')),
  (102, 'Carlos Pereira','carlos.pereira@exemplo.com','(31) 99999-0003', '333.444.555-03', datetime('now'), datetime('now')),
  (103, 'Ana Lima',      'ana.lima@exemplo.com',      '(41) 99999-0004', '444.555.666-04', datetime('now'), datetime('now')),
  (104, 'Pedro Alves',   'pedro.alves@exemplo.com',   '(51) 99999-0005', '555.666.777-05', datetime('now'), datetime('now'));

-- Ordens de serviço de exemplo
INSERT OR IGNORE INTO service_orders (id, client_id, description, status, created_at, updated_at)
VALUES
  (200, 100, 'Troca de tela - Notebook Dell XPS 13',         'OPEN',       datetime('now', '-5 days'), datetime('now', '-5 days')),
  (201, 101, 'Formatação e reinstalação do sistema',          'IN_PROGRESS',datetime('now', '-3 days'), datetime('now', '-1 day')),
  (202, 102, 'Substituição de bateria - MacBook Pro 2019',    'COMPLETED',  datetime('now', '-10 days'),datetime('now', '-2 days')),
  (203, 103, 'Diagnóstico de placa-mãe',                     'OPEN',       datetime('now', '-1 day'), datetime('now', '-1 day')),
  (204, 104, 'Limpeza e troca de pasta térmica',              'IN_PROGRESS',datetime('now', '-2 days'), datetime('now'));

-- Cobranças de exemplo
INSERT OR IGNORE INTO charges (id, service_order_id, description, amount, status, created_at, updated_at)
VALUES
  (300, 200, 'Peça: tela 13" FHD',       450.00, 'PENDING', datetime('now', '-5 days'), datetime('now', '-5 days')),
  (301, 200, 'Mão de obra',               150.00, 'PENDING', datetime('now', '-5 days'), datetime('now', '-5 days')),
  (302, 201, 'Formatação + licença SO',   200.00, 'PENDING', datetime('now', '-3 days'), datetime('now', '-3 days')),
  (303, 202, 'Bateria original Apple',    350.00, 'PAID',    datetime('now', '-10 days'),datetime('now', '-2 days')),
  (304, 202, 'Mão de obra',               100.00, 'PAID',    datetime('now', '-10 days'),datetime('now', '-2 days'));
EOF
)

# ---- Executa o seed ----
if [ -n "$SEED_FILE" ]; then
    if [ ! -f "$SEED_FILE" ]; then
        echo "ERRO: Arquivo '$SEED_FILE' não encontrado."
        exit 1
    fi
    SQL_CONTENT="$(cat "$SEED_FILE")"
    echo "  Usando arquivo de seed: $SEED_FILE"
elif [ "$EXTRA_ONLY" = true ]; then
    SQL_CONTENT="$DEFAULT_SEED_SQL"
    echo "  Inserindo apenas dados de exemplo extras..."
else
    SQL_CONTENT="$DEFAULT_SEED_SQL"
    echo "  Usando seed padrão de desenvolvimento..."
fi

echo ""

# Aplica via container (sqlite3 dentro do volume)
if docker ps --filter "name=${CONTAINER_NAME}" --filter "status=running" --format "{{.Names}}" | grep -q "${CONTAINER_NAME}"; then
    echo "$SQL_CONTENT" | docker exec -i "${CONTAINER_NAME}" sh -c "sqlite3 ${DB_PATH}"
    RESULT=$?
else
    # Aplica diretamente no volume via container temporário
    echo "$SQL_CONTENT" | docker run --rm -i \
        -v "${VOLUME_NAME}:/app/data" \
        alpine:3 \
        sh -c "apk add --quiet sqlite && sqlite3 ${DB_PATH}"
    RESULT=$?
fi

if [ "$RESULT" -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "   Seed concluído com sucesso!"
    echo "=========================================="
    echo ""
    echo "  Dados inseridos:"
    echo "    - 5 clientes de exemplo"
    echo "    - 5 ordens de serviço de exemplo"
    echo "    - 5 cobranças de exemplo"
    echo ""
    echo "  Acesse a aplicação em: http://localhost:${APP_PORT:-8080}"
    echo ""
else
    echo ""
    echo "ERRO: Falha ao executar o seed. Verifique os logs acima."
    exit 1
fi
