#!/usr/bin/env bash
# ============================================================
# Javos - Script de Build Completo
# ============================================================
# Este script realiza o build completo do projeto:
#   1. Instala dependências e compila o frontend
#   2. Copia o build estático para o backend
#   3. Empacota o backend como JAR executável
# ============================================================

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_DIR="$ROOT_DIR/backend"
STATIC_DIR="$BACKEND_DIR/src/main/resources/static"

echo "======================================================"
echo "  Javos - Build Script"
echo "======================================================"
echo ""

# ---- 1. Build do Frontend ----
echo "[1/4] Instalando dependências do frontend..."
cd "$FRONTEND_DIR"
npm ci

echo "[2/4] Compilando o frontend..."
npm run build
# O vite.config.ts define outDir como '../backend/src/main/resources/static'
# O build já é copiado automaticamente para o diretório estático do backend.

echo "  ✓ Frontend compilado em: $STATIC_DIR"
echo ""

# ---- 2. Build do Backend ----
echo "[3/4] Compilando e empacotando o backend (Maven)..."
cd "$BACKEND_DIR"
mvn package -DskipTests --no-transfer-progress

JAR_FILE=$(find "$BACKEND_DIR/target" -name "javos-*.jar" | head -1)

echo ""
echo "======================================================"
echo "  Build concluído com sucesso!"
echo ""
echo "  JAR gerado: $JAR_FILE"
echo ""
echo "  Para executar:"
echo "    java -jar $JAR_FILE"
echo ""
echo "  Acesse: http://localhost:8080"
echo "======================================================"
