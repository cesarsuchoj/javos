#!/usr/bin/env bash
# ============================================================
# Javos - Inicialização de certificados SSL
# ============================================================
# Execute antes de iniciar o docker-compose.prod.yml.
#
# Modos:
#   ./init-ssl.sh              # Gera certificado autoassinado (padrão)
#   ./init-ssl.sh -l           # Usa Let's Encrypt (requer DOMAIN e CERTBOT_EMAIL no .env)
#
# Uso típico:
#   cp .env.example .env
#   # edite o .env com DOMAIN, JWT_SECRET etc.
#   ./init-ssl.sh
#   docker compose -f docker-compose.prod.yml up -d
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

SSL_DIR="$SCRIPT_DIR/ssl"
MODE="selfsigned"

# ---- Parse de argumentos ----
while getopts "lh" opt; do
    case $opt in
        l) MODE="letsencrypt" ;;
        h)
            echo "Uso: $0 [-l]"
            echo "  -l  Usar Let's Encrypt (requer DOMAIN e CERTBOT_EMAIL no .env)"
            exit 0
            ;;
        *) echo "Opção inválida: -$OPTARG"; exit 1 ;;
    esac
done

# ---- Carrega variáveis do .env ----
if [ -f "$SCRIPT_DIR/.env" ]; then
    # shellcheck disable=SC1090
    set -a
    source <(grep -E '^[A-Z_]+=.' "$SCRIPT_DIR/.env" | grep -v '^#')
    set +a
fi

DOMAIN="${DOMAIN:-localhost}"

# ---- Verifica se os certs já existem ----
if [ -f "$SSL_DIR/fullchain.pem" ] && [ -f "$SSL_DIR/privkey.pem" ]; then
    echo "[OK] Certificados SSL já existem em $SSL_DIR/"
    echo "     Para regenerar, remova os arquivos e execute novamente."
    exit 0
fi

mkdir -p "$SSL_DIR"

# ============================================================
# Modo: Let's Encrypt
# ============================================================
if [ "$MODE" = "letsencrypt" ]; then
    CERTBOT_EMAIL="${CERTBOT_EMAIL:-}"

    if [ -z "$DOMAIN" ] || [ "$DOMAIN" = "localhost" ]; then
        echo "ERRO: DOMAIN deve ser configurado no .env para usar Let's Encrypt."
        exit 1
    fi

    if [ -z "$CERTBOT_EMAIL" ]; then
        echo "ERRO: CERTBOT_EMAIL deve ser configurado no .env para usar Let's Encrypt."
        exit 1
    fi

    echo ""
    echo "=========================================="
    echo "  Obtendo certificado Let's Encrypt"
    echo "  Domínio: $DOMAIN"
    echo "  Email:   $CERTBOT_EMAIL"
    echo "=========================================="
    echo ""
    echo "NOTA: A porta 80 deve estar acessível da internet."
    echo ""

    mkdir -p "$SCRIPT_DIR/certbot/conf" "$SCRIPT_DIR/certbot/www"

    docker run --rm \
        -v "$SCRIPT_DIR/certbot/conf:/etc/letsencrypt" \
        -v "$SCRIPT_DIR/certbot/www:/var/www/certbot" \
        -p 80:80 \
        certbot/certbot certonly \
        --standalone \
        --non-interactive \
        --agree-tos \
        --email "$CERTBOT_EMAIL" \
        -d "$DOMAIN"

    CERT_DIR="$SCRIPT_DIR/certbot/conf/live/$DOMAIN"

    if [ ! -f "$CERT_DIR/fullchain.pem" ]; then
        echo "ERRO: Falha ao obter certificados do Let's Encrypt."
        exit 1
    fi

    # Cria symlinks para o diretório ssl/
    ln -sf "$CERT_DIR/fullchain.pem" "$SSL_DIR/fullchain.pem"
    ln -sf "$CERT_DIR/privkey.pem" "$SSL_DIR/privkey.pem"

    echo ""
    echo "[OK] Certificados Let's Encrypt configurados!"

# ============================================================
# Modo: Autoassinado (padrão)
# ============================================================
else
    echo ""
    echo "=========================================="
    echo "  Gerando certificado SSL autoassinado"
    echo "  Domínio: $DOMAIN"
    echo "=========================================="
    echo ""
    echo "AVISO: Certificados autoassinados são adequados para"
    echo "  testes. Para produção real, use Let's Encrypt (-l)."
    echo ""

    if ! command -v openssl &>/dev/null; then
        echo "ERRO: openssl não encontrado. Instale o openssl e tente novamente."
        exit 1
    fi

    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout "$SSL_DIR/privkey.pem" \
        -out "$SSL_DIR/fullchain.pem" \
        -subj "/C=BR/ST=SP/L=Sao Paulo/O=Javos/CN=$DOMAIN" \
        -addext "subjectAltName=DNS:$DOMAIN,DNS:www.$DOMAIN,IP:127.0.0.1"

    echo ""
    echo "[OK] Certificado autoassinado gerado em $SSL_DIR/"
fi

# ---- Permissões seguras para a chave privada ----
chmod 600 "$SSL_DIR/privkey.pem"
chmod 644 "$SSL_DIR/fullchain.pem"

echo ""
echo "=========================================="
echo "  Certificados SSL prontos!"
echo ""
echo "  Para iniciar em produção:"
echo "    docker compose -f docker-compose.prod.yml up -d"
echo "=========================================="
echo ""
