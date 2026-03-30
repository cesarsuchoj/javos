# Javos - Runbook de Operações (On-Call)

Este documento é o guia de referência rápida para operadores on-call. Cobre diagnóstico, resolução de incidentes, backup, restore e procedimentos de emergência.

---

## Índice

1. [Informações de Contato](#informações-de-contato)
2. [Acesso ao Servidor](#acesso-ao-servidor)
3. [Comandos Rápidos](#comandos-rápidos)
4. [Alertas e Diagnóstico](#alertas-e-diagnóstico)
5. [Procedimentos de Incidente](#procedimentos-de-incidente)
6. [Backup e Restore](#backup-e-restore)
7. [Rollback](#rollback)
8. [Checklist Pós-Deploy](#checklist-pós-deploy)

---

## Informações de Contato

> Substitua com as informações reais da equipe.

| Papel              | Nome         | Contato                  |
|--------------------|--------------|--------------------------|
| On-call primário   | —            | Slack: `#javos-oncall`   |
| On-call secundário | —            | Slack: `#javos-oncall`   |
| Responsável técnico| —            | —                        |

---

## Acesso ao Servidor

```bash
# Produção
ssh -i ~/.ssh/javos_prod usuario@IP_PRODUCAO

# Staging
ssh -i ~/.ssh/javos_staging usuario@IP_STAGING

# Diretório da aplicação
cd /home/usuario/javos/deploy
```

---

## Comandos Rápidos

### Status geral

```bash
# Ver status de todos os containers
docker compose -f docker-compose.prod.yml ps

# Ver uso de recursos (CPU, RAM, I/O)
docker stats --no-stream

# Ver logs dos últimos 100 linhas do backend
docker compose -f docker-compose.prod.yml logs --tail=100 backend

# Ver logs do nginx
docker compose -f docker-compose.prod.yml logs --tail=50 nginx

# Seguir logs em tempo real
docker compose -f docker-compose.prod.yml logs -f backend
```

### Health checks

```bash
# Backend health (deve retornar {"status":"UP"})
curl -sf https://app.meusite.com.br/actuator/health | python3 -m json.tool

# Nginx health
curl -sf https://app.meusite.com.br/health

# Backend direto (sem passar pelo nginx)
docker compose -f docker-compose.prod.yml exec backend \
  wget -qO- http://localhost:8080/actuator/health
```

### Reiniciar serviços

```bash
# Reiniciar apenas o backend
docker compose -f docker-compose.prod.yml restart backend

# Reiniciar apenas o nginx
docker compose -f docker-compose.prod.yml restart nginx

# Reiniciar tudo
docker compose -f docker-compose.prod.yml restart

# Parar tudo
docker compose -f docker-compose.prod.yml down

# Iniciar tudo
docker compose -f docker-compose.prod.yml up -d
```

---

## Alertas e Diagnóstico

### Aplicação não responde (HTTP 5xx ou timeout)

```bash
# 1. Verifique se os containers estão rodando
docker compose -f docker-compose.prod.yml ps

# 2. Verifique logs de erros do backend
docker compose -f docker-compose.prod.yml logs --tail=200 backend | grep -E "ERROR|WARN|Exception"

# 3. Verifique memória da JVM
docker compose -f docker-compose.prod.yml exec backend \
  wget -qO- http://localhost:8080/actuator/metrics/jvm.memory.used

# 4. Se o container travou, force restart
docker compose -f docker-compose.prod.yml restart backend

# 5. Aguarde o health check passar (até 60s)
sleep 60
curl -sf https://app.meusite.com.br/actuator/health
```

### Erros de banco de dados

```bash
# 1. Verifique logs do backend em busca de erros de banco
docker compose -f docker-compose.prod.yml logs --tail=500 backend | grep -iE "sqlite|database|connection|sql"

# 2. Verifique integridade do banco SQLite
docker compose -f docker-compose.prod.yml exec backend \
  sh -c "sqlite3 /app/data/javos.db 'PRAGMA integrity_check;'"
# Saída esperada: "ok"

# 3. Verifique espaço em disco
df -h
docker system df

# 4. Se o banco estiver corrompido, restore o backup
# (veja seção Backup e Restore)
```

### Alto uso de CPU / Memória

```bash
# 1. Identifique o container com maior uso
docker stats --no-stream

# 2. Ver threads do backend Java
docker compose -f docker-compose.prod.yml exec backend \
  wget -qO- http://localhost:8080/actuator/metrics/executor.active

# 3. Ver métricas detalhadas
curl -sf https://app.meusite.com.br/actuator/metrics | python3 -m json.tool

# 4. Se necessário, reinicie o backend para liberar memória
docker compose -f docker-compose.prod.yml restart backend
```

### Certificado SSL expirado ou inválido

```bash
# 1. Verifique a validade do certificado
openssl s_client -connect app.meusite.com.br:443 -servername app.meusite.com.br 2>/dev/null \
  | openssl x509 -noout -dates

# 2. Force renovação manual do Let's Encrypt
docker compose -f docker-compose.prod.yml exec certbot \
  certbot renew --force-renewal

# 3. Recarregue o nginx para usar o novo certificado
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

### Nginx retorna 502 Bad Gateway

Significa que o nginx não consegue alcançar o backend.

```bash
# 1. Verifique se o backend está rodando
docker compose -f docker-compose.prod.yml ps backend

# 2. Verifique os logs do nginx
docker compose -f docker-compose.prod.yml logs --tail=50 nginx | grep -i error

# 3. Verifique se o backend está acessível na rede interna
docker compose -f docker-compose.prod.yml exec nginx \
  wget -qO- http://backend:8080/actuator/health

# 4. Reinicie o backend e aguarde o health check
docker compose -f docker-compose.prod.yml restart backend
```

### Rate limiting bloqueando usuários legítimos

```bash
# 1. Verifique se o rate limit está ativo
docker compose -f docker-compose.prod.yml exec backend \
  wget -qO- http://localhost:8080/actuator/metrics/rate.limit.blocked 2>/dev/null || echo "métrica não disponível"

# 2. Verifique logs de rate limit
docker compose -f docker-compose.prod.yml logs --tail=200 backend | grep -i "rate.limit\|429"

# 3. Para desabilitar temporariamente (emergência)
# Adicione ao .env: RATE_LIMIT_ENABLED=false
# Reinicie o backend
docker compose -f docker-compose.prod.yml up -d --no-deps backend
```

---

## Procedimentos de Incidente

### Severidade de incidentes

| Nível | Descrição                                     | Tempo de resposta |
|-------|-----------------------------------------------|-------------------|
| P1    | Aplicação completamente inacessível           | Imediato          |
| P2    | Funcionalidade crítica degradada              | 30 minutos        |
| P3    | Erro não crítico, workaround disponível       | 4 horas           |
| P4    | Melhoria ou bug menor                         | Próximo sprint    |

### Fluxo de resposta (P1/P2)

1. **Detectar** — alerta via Grafana/Prometheus ou reporte de usuário
2. **Triagem** — identificar o componente afetado (backend, nginx, banco)
3. **Mitigar** — aplicar solução imediata (restart, rollback)
4. **Comunicar** — notificar a equipe no Slack `#javos-oncall`
5. **Resolver** — corrigir a causa raiz
6. **Post-mortem** — documentar o incidente e ações preventivas

---

## Backup e Restore

### Backup manual imediato

```bash
cd /home/usuario/javos/deploy

# Cria backup com timestamp
./backup.sh /home/usuario/javos/backups

# Verifica o arquivo gerado
ls -lh /home/usuario/javos/backups/javos-backup-*.tar.gz | tail -5
```

### Restore de backup

> ⚠️ O restore **substitui todos os dados atuais**. Faça um backup do estado atual antes de restaurar.

```bash
cd /home/usuario/javos/deploy

# 1. Faça backup do estado atual primeiro
./backup.sh /home/usuario/javos/backups/pre-restore

# 2. Pare apenas o backend (mantém nginx no ar mostrando página de manutenção)
docker compose -f docker-compose.prod.yml stop backend

# 3. Restaure
./restore.sh /home/usuario/javos/backups/javos-backup-YYYYMMDD_HHMMSS.tar.gz

# 4. Inicie o backend
docker compose -f docker-compose.prod.yml up -d backend

# 5. Verifique o health
sleep 30
curl -sf https://app.meusite.com.br/actuator/health
```

### Listar backups disponíveis

```bash
ls -lht /home/usuario/javos/backups/javos-backup-*.tar.gz
```

---

## Rollback

### Rollback rápido (via imagem Docker anterior)

```bash
cd /home/usuario/javos/deploy

# 1. Identifique a tag da imagem anterior no GitHub Actions
# (tab "Packages" do repositório ou histórico do CI)
PREVIOUS_IMAGE="ghcr.io/cesarsuchoj/javos:sha-abc1234"

# 2. Atualize o .env
sed -i "s|DOCKER_IMAGE=.*|DOCKER_IMAGE=${PREVIOUS_IMAGE}|" .env

# 3. Pull da imagem anterior e restart do backend
docker pull "${PREVIOUS_IMAGE}"
docker compose -f docker-compose.prod.yml up -d --no-deps backend

# 4. Verifique
sleep 30
curl -sf https://app.meusite.com.br/actuator/health
```

### Rollback via Git (rebuild local)

```bash
cd /home/usuario/javos

# 1. Identifique o commit anterior
git log --oneline -10

# 2. Checkout da versão anterior
git checkout <commit-sha>

# 3. Rebuild e deploy
cd deploy
docker compose -f docker-compose.prod.yml up -d --build backend

# 4. Verifique
sleep 60
curl -sf https://app.meusite.com.br/actuator/health
```

---

## Checklist Pós-Deploy

Execute este checklist após cada deploy em produção:

```
[ ] Todos os containers estão com status "healthy":
      docker compose -f docker-compose.prod.yml ps

[ ] Backend responde com status UP:
      curl -sf https://app.meusite.com.br/actuator/health

[ ] Frontend carrega corretamente:
      curl -sf https://app.meusite.com.br/ | grep -i "javos"

[ ] Login funciona (teste manual ou smoke test)

[ ] Não há erros críticos nos logs (últimos 100 linhas):
      docker compose -f docker-compose.prod.yml logs --tail=100 backend | grep -c ERROR

[ ] Métricas aparecem no Grafana (dashboard principal)

[ ] Certificado SSL válido:
      curl -vI https://app.meusite.com.br 2>&1 | grep "SSL certificate verify ok"

[ ] Backup pré-deploy disponível em /home/usuario/javos/backups/
```

---

> Para procedimentos detalhados de deploy por ambiente, consulte [`docs/DEPLOYMENT.md`](DEPLOYMENT.md).
