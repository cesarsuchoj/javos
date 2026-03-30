# Javos - Guia de Deployment por Ambiente

Este documento detalha como fazer deploy do Javos em cada ambiente: desenvolvimento local, staging e produção. Inclui também as práticas de segurança, backup/restore, rollback e estratégias de escala.

---

## Índice

1. [Ambientes](#ambientes)
2. [Dev / Local](#dev--local)
3. [Staging](#staging)
4. [Produção](#produção)
5. [CI/CD Pipeline](#cicd-pipeline)
6. [Segurança em Produção](#segurança-em-produção)
7. [Backup e Restore](#backup-e-restore)
8. [Rollback](#rollback)
9. [Scaling](#scaling)
10. [Disaster Recovery](#disaster-recovery)

---

## Ambientes

| Ambiente   | Arquivo Compose                  | Perfil Spring | Banco de dados           | SSL                    |
|------------|----------------------------------|---------------|--------------------------|------------------------|
| Dev/Local  | `docker-compose.yml`             | `prod` (padrão) ou `dev` | SQLite local (padrão)    | Nenhum (HTTP)          |
| Staging    | `docker-compose.staging.yml`     | `staging`     | SQLite isolado ou MySQL  | Autoassinado           |
| Produção   | `docker-compose.prod.yml`        | `prod`        | SQLite ou MySQL          | Let's Encrypt          |

---

## Dev / Local

### Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop) (Windows / macOS)
- Docker Engine + Docker Compose (Linux)

### Setup inicial

```bash
cd deploy

# 1. Crie o arquivo de configuração
cp .env.example .env

# 2. (Opcional) Ajuste as variáveis em .env
#    - APP_PORT: porta de acesso (padrão: 8080)
#    - JWT_SECRET: troque por uma string aleatória

# 3. Instale e inicie
./install.sh       # Linux
./install-macos.sh # macOS
install.bat        # Windows
```

### Template de variáveis de ambiente

O arquivo `.env.example` contém todas as variáveis disponíveis com seus valores padrão. As principais:

| Variável                          | Descrição                              | Padrão                              |
|-----------------------------------|----------------------------------------|-------------------------------------|
| `APP_PORT`                        | Porta de acesso local                  | `8080`                              |
| `JWT_SECRET`                      | Chave JWT (mude em qualquer ambiente!) | valor gerado                        |
| `SPRING_DATASOURCE_URL`           | URL do banco de dados                  | `jdbc:sqlite:./data/javos.db`       |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | Driver JDBC                        | `org.sqlite.JDBC`                   |
| `SPRING_JPA_DATABASE_PLATFORM`    | Dialeto Hibernate                      | `SQLiteDialect`                     |
| `CORS_ALLOWED_ORIGINS`            | Origens permitidas para CORS           | `*`                                 |
| `GRAFANA_ADMIN_PASSWORD`          | Senha do Grafana                       | definir antes de usar               |

> Para uma lista completa, consulte [`deploy/.env.example`](../deploy/.env.example).

### Seed do banco de dados

Para popular o banco com dados de exemplo (clientes, ordens de serviço, cobranças):

```bash
cd deploy

# Inicie a aplicação primeiro
./start.sh

# Execute o seed
./seed.sh

# Ou use um arquivo SQL personalizado:
./seed.sh -f /caminho/meu-seed.sql
```

O seed insere dados com `INSERT OR IGNORE`, portanto é seguro re-executar sem duplicar registros.

> ⚠️ O seed é destinado **apenas** a ambientes de desenvolvimento. O script recusa execução se `SPRING_PROFILES_ACTIVE=prod`.

### Scripts de inicialização

| Script              | Descrição                          |
|---------------------|------------------------------------|
| `install.sh`        | Instalação inicial (Linux)         |
| `install-macos.sh`  | Instalação inicial (macOS)         |
| `install.bat`       | Instalação inicial (Windows)       |
| `start.sh`          | Inicia a aplicação                 |
| `stop.sh`           | Para a aplicação                   |
| `logs.sh`           | Exibe logs em tempo real           |
| `reset.sh`          | Reseta o banco de dados            |
| `seed.sh`           | Popula o banco com dados de exemplo|
| `build.sh`          | Build manual (frontend + backend)  |

---

## Staging

O ambiente de staging replica a produção com dados isolados para validar novas versões antes de liberar para os usuários finais.

### Pré-requisitos no servidor

- Docker Engine ≥ 24 e Docker Compose v2
- Porta 80 e 443 abertas no firewall
- Domínio ou IP acessível (ex: `staging.app.meusite.com.br`)

### Setup inicial

```bash
cd deploy

# 1. Crie o arquivo de configuração de staging
cp .env.staging.example .env.staging

# 2. Edite os valores marcados com CHANGE_ME
nano .env.staging
#  - DOMAIN=staging.app.meusite.com.br
#  - JWT_SECRET=<string aleatória, diferente da produção>
#  - CORS_ALLOWED_ORIGINS=https://staging.app.meusite.com.br
#  - GRAFANA_ADMIN_PASSWORD=<senha segura>

# 3. Gere o certificado SSL autoassinado (padrão para staging)
./init-ssl.sh

# 4. Inicie o ambiente de staging
docker compose -f docker-compose.staging.yml --env-file .env.staging up -d
```

### Banco de dados de staging

Por padrão, o staging usa um banco SQLite isolado (`javos-staging.db`). Para usar uma cópia do banco de produção:

```bash
# 1. Faça backup do banco de produção
docker run --rm \
  -v deploy_javos-data:/data:ro \
  -v "$(pwd)/backups:/backup" \
  alpine:3 tar czf /backup/prod-snapshot.tar.gz -C /data .

# 2. Copie o backup para o servidor de staging
scp backups/prod-snapshot.tar.gz usuario@servidor-staging:/home/usuario/

# 3. No servidor de staging, restaure o banco
ssh usuario@servidor-staging
cd /home/usuario/javos/deploy
docker compose -f docker-compose.staging.yml --env-file .env.staging down
docker volume rm deploy_javos-staging-data 2>/dev/null || true
docker run --rm \
  -v deploy_javos-staging-data:/data \
  -v "$(pwd):/backup:ro" \
  alpine:3 sh -c "cd /data && tar xzf /backup/prod-snapshot.tar.gz"
docker compose -f docker-compose.staging.yml --env-file .env.staging up -d
```

> ⚠️ Antes de restaurar dados de produção em staging, certifique-se de anonimizar dados sensíveis dos usuários (LGPD/GDPR).

### SSL de staging

O script `init-ssl.sh` gera por padrão um certificado autoassinado, adequado para staging. Para usar Let's Encrypt em staging (requer domínio público acessível na internet):

```bash
./init-ssl.sh -l
```

### Monitoramento em staging

O Grafana estará disponível na porta `3001` (configurável via `GRAFANA_PORT`):

```
http://staging.app.meusite.com.br:3001
```

Para verificar o status dos serviços:

```bash
docker compose -f docker-compose.staging.yml --env-file .env.staging ps
docker compose -f docker-compose.staging.yml --env-file .env.staging logs --tail=50 backend
```

---

## Produção

### Pré-requisitos no servidor

- VPS ou servidor dedicado (2+ vCPU, 2+ GB RAM recomendado)
- Docker Engine ≥ 24 e Docker Compose v2
- Portas 80 e 443 abertas
- Domínio público apontando para o IP do servidor
- Email válido para notificações do Let's Encrypt

### Setup inicial

```bash
cd deploy

# 1. Crie o arquivo de configuração de produção
cp .env.example .env

# 2. Edite TODAS as variáveis marcadas como obrigatórias
nano .env
#  - DOMAIN=app.meusite.com.br
#  - JWT_SECRET=$(openssl rand -hex 64)
#  - CORS_ALLOWED_ORIGINS=https://app.meusite.com.br
#  - CERTBOT_EMAIL=admin@meusite.com.br
#  - GRAFANA_ADMIN_PASSWORD=$(openssl rand -base64 24)

# 3. Gere certificados Let's Encrypt
./init-ssl.sh -l

# 4. Inicie em produção
docker compose -f docker-compose.prod.yml up -d
```

### Verificação pós-deploy

```bash
# Verifica se todos os containers estão saudáveis
docker compose -f docker-compose.prod.yml ps

# Verifica o health do backend
curl -sf https://app.meusite.com.br/actuator/health | python3 -m json.tool

# Verifica o nginx
curl -sf https://app.meusite.com.br/health

# Acompanhe os logs em tempo real
docker compose -f docker-compose.prod.yml logs -f --tail=50
```

---

## CI/CD Pipeline

O pipeline de CI/CD está definido em `.github/workflows/ci.yml`.

### Fluxo por branch

| Evento                        | Jobs executados                                              |
|-------------------------------|--------------------------------------------------------------|
| Push em qualquer branch       | `backend-tests`, `frontend-tests`                           |
| Pull Request                  | `backend-tests`, `frontend-tests`                           |
| Push em `main`                | Todos os jobs + `docker-build-push`, `deploy-staging`       |
| Tag `v*` (release)            | Todos os jobs + `docker-build-push`, `deploy-production`    |
| `workflow_dispatch` manual    | Todos os jobs + `docker-build-push`                         |

### Ambientes e aprovações

Os jobs de deploy em staging e produção usam [GitHub Environments](https://docs.github.com/en/actions/deployment/targeting-different-environments/using-environments-for-deployment):

- **`staging`**: deploy automático após testes passarem no `main`
- **`production`**: requer aprovação manual de um revisor antes do deploy

Para configurar os environments no GitHub:
1. Acesse **Settings → Environments** no repositório
2. Crie os environments `staging` e `production`
3. Em `production`, adicione **Required reviewers** (pelo menos 1)
4. Configure os secrets específicos por ambiente:
   - `DEPLOY_HOST` – IP ou hostname do servidor
   - `DEPLOY_USER` – usuário SSH
   - `DEPLOY_SSH_KEY` – chave privada SSH

### Secrets necessários no GitHub

| Secret                        | Descrição                                        |
|-------------------------------|--------------------------------------------------|
| `STAGING_DEPLOY_HOST`         | Host do servidor de staging                      |
| `STAGING_DEPLOY_USER`         | Usuário SSH do servidor de staging               |
| `STAGING_DEPLOY_SSH_KEY`      | Chave privada SSH para staging                   |
| `PRODUCTION_DEPLOY_HOST`      | Host do servidor de produção                     |
| `PRODUCTION_DEPLOY_USER`      | Usuário SSH do servidor de produção              |
| `PRODUCTION_DEPLOY_SSH_KEY`   | Chave privada SSH para produção                  |

### Health checks pós-deploy

Cada job de deploy executa health checks automáticos após subir os containers:

1. Aguarda o backend responder em `/actuator/health`
2. Valida que o status retornado é `{"status":"UP"}`
3. Verifica o nginx em `/health`

Se qualquer check falhar, o pipeline sinaliza falha e notifica a equipe.

---

## Segurança em Produção

### Checklist obrigatório antes de ir para produção

- [ ] `JWT_SECRET` gerado com `openssl rand -hex 64` (≥ 64 caracteres)
- [ ] `GRAFANA_ADMIN_PASSWORD` alterado do valor padrão
- [ ] `CORS_ALLOWED_ORIGINS` restrito ao domínio da aplicação (sem `*`)
- [ ] Certificado SSL válido (Let's Encrypt ou comercial)
- [ ] Firewall configurado: apenas portas 22 (SSH), 80 e 443 abertas
- [ ] Acesso SSH protegido por chave (desabilitar autenticação por senha)
- [ ] Grafana **não** exposto publicamente (use VPN ou restrinja por IP no firewall)
- [ ] Atualizações automáticas de segurança do SO habilitadas
- [ ] Backups automáticos configurados (veja [Backup e Restore](#backup-e-restore))
- [ ] Usuário `admin` padrão com senha alterada no primeiro acesso

### Hardening do servidor

```bash
# Desabilitar autenticação SSH por senha (use chave privada)
sudo sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo systemctl restart ssh

# Habilitar UFW (Ubuntu/Debian)
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp comment 'SSH'
sudo ufw allow 80/tcp comment 'HTTP'
sudo ufw allow 443/tcp comment 'HTTPS'
sudo ufw enable

# Restringe Grafana (porta 3000) apenas para IPs da equipe
sudo ufw allow from SEU_IP_VPN to any port 3000
```

### Renovação automática de certificados SSL

O container `certbot` no `docker-compose.prod.yml` verifica e renova automaticamente os certificados a cada 12 horas. O nginx recarrega as configurações quando os certificados são atualizados.

Para renovar manualmente:

```bash
docker compose -f docker-compose.prod.yml exec certbot certbot renew --force-renewal
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

---

## Backup e Restore

### Backup automático (cron)

Configure um cron job no servidor de produção para backups diários:

```bash
# Edite o crontab do usuário
crontab -e

# Adicione a linha abaixo para backup diário às 3:00
0 3 * * * /home/usuario/javos/deploy/backup.sh /home/usuario/javos/backups >> /var/log/javos-backup.log 2>&1
```

### Backup manual

```bash
cd deploy

# Linux / macOS
./backup.sh                          # salva em ./backups/
./backup.sh /caminho/personalizado   # salva em outro local

# Windows
backup.bat
backup.bat C:\caminho\personalizado

# PowerShell
.\backup.ps1
.\backup.ps1 -BackupDir C:\caminho\personalizado
```

O arquivo gerado é `javos-backup-YYYYMMDD_HHMMSS.tar.gz`.

### Restore

```bash
cd deploy

# Linux / macOS
./restore.sh backups/javos-backup-20240101_030000.tar.gz

# Windows
restore.bat backups\javos-backup-20240101_030000.tar.gz

# PowerShell
.\restore.ps1 backups\javos-backup-20240101_030000.tar.gz
```

### Retenção de backups

Recomenda-se manter:
- Backups **diários** por 7 dias
- Backups **semanais** por 4 semanas
- Backups **mensais** por 12 meses

Exemplo de limpeza automática no cron:

```bash
# Limpa backups com mais de 30 dias
0 4 * * * find /home/usuario/javos/backups -name "javos-backup-*.tar.gz" -mtime +30 -delete
```

### Backup remoto (S3 / rclone)

Para armazenar backups em nuvem (AWS S3, Google Cloud Storage etc.) instale o [rclone](https://rclone.org/) e adicione ao cron:

```bash
# Após o backup diário, sincronize para S3
0 3 * * * /home/usuario/javos/deploy/backup.sh /home/usuario/javos/backups && \
          rclone copy /home/usuario/javos/backups s3:meu-bucket/javos/backups/ --max-age 30d
```

---

## Rollback

### Rollback via Docker (imagem anterior)

O CI/CD publica imagens Docker com tag `sha-<commit>` e `latest` no GitHub Container Registry. Para fazer rollback:

```bash
# 1. Identifique a imagem da versão anterior
# No CI, cada push gera uma tag sha-<commit>
# Exemplo: ghcr.io/cesarsuchoj/javos:sha-abc1234

# 2. Atualize o .env com a imagem anterior
echo "DOCKER_IMAGE=ghcr.io/cesarsuchoj/javos:sha-abc1234" >> .env

# 3. Recrie apenas o backend (sem downtime no nginx)
docker compose -f docker-compose.prod.yml pull backend
docker compose -f docker-compose.prod.yml up -d --no-deps backend

# 4. Verifique a saúde após o rollback
curl -sf https://app.meusite.com.br/actuator/health
```

### Rollback do banco de dados

Se a nova versão migrou o schema e o rollback de código for necessário:

```bash
# 1. Pare a aplicação
docker compose -f docker-compose.prod.yml stop backend

# 2. Faça backup do banco atual (estado pós-migração)
./backup.sh /home/usuario/javos/backups

# 3. Restaure o backup anterior (pré-migração)
./restore.sh backups/javos-backup-<timestamp-anterior>.tar.gz

# 4. Suba a versão anterior do backend
docker compose -f docker-compose.prod.yml up -d backend
```

> ⚠️ O rollback de schema é irreversível. Garanta que o backup pré-migração esteja disponível **antes** de qualquer deploy.

### Procedimento de rollback emergencial (< 5 min)

```bash
# No servidor de produção
cd /home/usuario/javos

# 1. Identifique a tag da versão anterior no histórico Git
git log --oneline -10

# 2. Faça checkout da versão anterior
git checkout <commit-anterior>

# 3. Re-build e restart
cd deploy
docker compose -f docker-compose.prod.yml up -d --build backend
```

---

## Scaling

### Escala vertical (scale-up)

A estratégia mais simples é aumentar os recursos do servidor:

- **CPU**: o backend Java usa múltiplas threads; mais vCPUs = melhor throughput
- **RAM**: JVM requer pelo menos 512 MB; recomendado 1–2 GB para produção
- **Disco**: monitore o crescimento do banco SQLite; migre para MySQL se > 1 GB

Configure limites de recursos no Docker Compose:

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 1G
        reservations:
          memory: 512M
```

### Escala horizontal (múltiplas instâncias)

Para suportar maior carga com múltiplas instâncias do backend:

1. **Migre de SQLite para MySQL** (SQLite não suporta múltiplos writers concorrentes):
   ```bash
   # Edite .env e configure OPÇÃO 2 (MySQL)
   nano .env
   ```

2. **Configure o nginx como load balancer**:
   ```nginx
   upstream javos_backend {
       least_conn;
       server backend1:8080;
       server backend2:8080;
       server backend3:8080;
   }
   ```

3. **Inicie múltiplas réplicas**:
   ```bash
   docker compose -f docker-compose.prod.yml up -d --scale backend=3
   ```

> Para escala horizontal, JWT é stateless (sem sessões no servidor), portanto todas as instâncias podem validar tokens sem coordenação.

### Métricas para decidir quando escalar

Monitore via Grafana/Prometheus:

| Métrica                          | Threshold para escalar |
|----------------------------------|------------------------|
| CPU usage                        | > 70% sustentado       |
| Heap JVM usage                   | > 80%                  |
| Latência média das requests      | > 500ms                |
| Taxa de erros HTTP 5xx           | > 1%                   |
| Fila de threads do Tomcat        | > 80% da capacidade    |

---

## Disaster Recovery

### Cenários e procedimentos

#### Cenário 1: Falha do container (restart automático)

Todos os containers têm `restart: unless-stopped`. Se um container falhar, o Docker reinicia automaticamente. Monitore via Grafana para identificar falhas recorrentes.

#### Cenário 2: Falha do servidor (VPS inacessível)

```bash
# 1. Provisione um novo servidor com Docker
# 2. Clone o repositório
git clone https://github.com/cesarsuchoj/javos.git
cd javos/deploy

# 3. Copie o arquivo .env (mantenha backup seguro do .env!)
# O .env NÃO deve estar no Git — armazene-o em cofre de senhas (ex: 1Password, Bitwarden)

# 4. Restaure o backup mais recente
./restore.sh /caminho/do/backup.tar.gz

# 5. Gere novos certificados SSL
./init-ssl.sh -l

# 6. Inicie a aplicação
docker compose -f docker-compose.prod.yml up -d
```

**RTO estimado**: 30–60 minutos  
**RPO**: último backup (máximo 24h se backup diário configurado)

#### Cenário 3: Corrupção do banco de dados

```bash
# 1. Pare a aplicação
docker compose -f docker-compose.prod.yml stop backend

# 2. Tente reparar o banco SQLite
docker run --rm -v deploy_javos-data:/data alpine:3 \
  sh -c "apk add sqlite && sqlite3 /data/javos.db 'PRAGMA integrity_check;'"

# 3. Se corrompido, restaure o backup
./restore.sh backups/javos-backup-<ultimo-bom>.tar.gz

# 4. Reinicie
docker compose -f docker-compose.prod.yml up -d backend
```

#### Cenário 4: Vazamento de JWT_SECRET

```bash
# 1. Gere um novo JWT_SECRET
NEW_SECRET=$(openssl rand -hex 64)

# 2. Atualize o .env
sed -i "s/JWT_SECRET=.*/JWT_SECRET=${NEW_SECRET}/" .env

# 3. Reinicie o backend (todos os tokens anteriores serão invalidados)
docker compose -f docker-compose.prod.yml up -d --no-deps backend

# NOTA: Todos os usuários precisarão fazer login novamente.
```

### Checklist de Disaster Recovery

- [ ] Backup mais recente disponível e testado (teste mensal de restore)
- [ ] Arquivo `.env` armazenado em cofre de senhas (nunca no Git)
- [ ] Chaves SSH do deploy armazenadas com segurança
- [ ] Documentação do DNS atualizada (saber para onde apontar o domínio)
- [ ] Contatos de emergência definidos (equipe on-call)
- [ ] Runbook atualizado: [`docs/RUNBOOK.md`](RUNBOOK.md)
