# Changelog – Javos API

Todas as mudanças notáveis da API REST são documentadas aqui.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e o projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

---

## [Não lançado]

### Adicionado
- Documentação Swagger/OpenAPI completa em todos os endpoints
- Guias de autenticação, rate limiting e tratamento de erros em `docs/`
- Coleção Postman exportada em `docs/POSTMAN_COLLECTION.json`
- Configuração do Swagger UI com ordenação alfabética e Try-it-out habilitado

---

## [0.0.1-SNAPSHOT] – Em desenvolvimento

### Adicionado

#### Autenticação (`/api/v1/auth`)
- `POST /login` – Autenticação com JWT (access token 24h + refresh token 7d)
- `POST /register` – Registro de novo usuário (role `ROLE_USER`)
- `POST /refresh` – Renovação de tokens com rotação
- `POST /logout` – Encerramento de sessão com revogação de tokens

#### Usuários (`/api/v1/users`)
- `GET /` – Listar usuários (requer `ROLE_ADMIN`)
- `GET /{id}` – Buscar usuário por ID (próprio usuário ou admin)
- `PUT /{id}` – Atualizar usuário (requer `ROLE_ADMIN`)
- `DELETE /{id}` – Excluir usuário (requer `ROLE_ADMIN`)

#### Clientes (`/api/v1/clients`)
- `GET /` – Listar clientes (filtros: `name`, `document`)
- `GET /{id}` – Buscar cliente por ID
- `POST /` – Criar cliente
- `PUT /{id}` – Atualizar cliente
- `DELETE /{id}` – Excluir cliente

#### Produtos (`/api/v1/products`)
- `GET /` – Listar produtos (filtro: `name`)
- `GET /{id}` – Buscar produto por ID
- `GET /code/{code}` – Buscar produto por código
- `POST /` – Criar produto
- `PUT /{id}` – Atualizar produto
- `DELETE /{id}` – Excluir produto

#### Ordens de Serviço (`/api/v1/service-orders`)
- `GET /` – Listar ordens de serviço
- `GET /{id}` – Buscar OS por ID
- `POST /` – Criar OS
- `PUT /{id}` – Atualizar OS
- `PATCH /{id}/status` – Alterar status (query param `status`)
- `DELETE /{id}` – Excluir OS
- `GET /{id}/notes` – Listar notas da OS
- `POST /{id}/notes` – Adicionar nota à OS

#### Vendas (`/api/v1/sales`)
- `GET /` – Listar vendas
- `GET /{id}` – Buscar venda por ID
- `POST /` – Criar venda
- `PUT /{id}` – Atualizar venda
- `PATCH /{id}/status` – Alterar status (query param `status`)
- `POST /{id}/items` – Adicionar item
- `DELETE /{id}/items/{itemId}` – Remover item
- `DELETE /{id}` – Excluir venda

#### Cobranças (`/api/v1/charges`)
- `GET /` – Listar cobranças
- `GET /{id}` – Buscar cobrança por ID
- `POST /` – Criar cobrança
- `PUT /{id}` – Atualizar cobrança
- `PATCH /{id}/status` – Alterar status (query param `status`)
- `DELETE /{id}` – Excluir cobrança

#### Financeiro (`/api/v1/financial`)
- `GET /categories` – Listar categorias
- `GET /categories/{id}` – Buscar categoria
- `POST /categories` – Criar categoria
- `PUT /categories/{id}` – Atualizar categoria
- `DELETE /categories/{id}` – Excluir categoria
- `GET /accounts` – Listar contas
- `GET /accounts/{id}` – Buscar conta
- `POST /accounts` – Criar conta
- `PUT /accounts/{id}` – Atualizar conta
- `DELETE /accounts/{id}` – Excluir conta
- `GET /entries` – Listar lançamentos
- `GET /entries/{id}` – Buscar lançamento
- `POST /entries` – Criar lançamento
- `PUT /entries/{id}` – Atualizar lançamento
- `DELETE /entries/{id}` – Excluir lançamento

#### Dashboard (`/api/v1/dashboard`)
- `GET /summary` – Resumo do sistema

#### Configurações (`/api/v1/system-config`)
- `GET /` – Listar configurações
- `GET /{key}` – Buscar configuração por chave
- `PUT /{key}` – Atualizar configuração

#### Auditoria (`/api/v1/audit`)
- `GET /` – Listar todos os logs
- `GET /user/{username}` – Logs por usuário
- `GET /entity/{entityType}/{entityId}` – Logs por entidade

### Infraestrutura
- Autenticação JWT stateless com access token + refresh token
- Rate limiting por IP (Bucket4j): 10 req/min em auth, 100 req/min em API
- Banco de dados SQLite por padrão (configurável para MySQL/PostgreSQL via env vars)
- Migrations automáticas com Flyway
- Logging estruturado em JSON (produção) via Logstash Encoder
- Header `X-Correlation-Id` em todas as requisições
- Swagger UI disponível em `/swagger-ui.html`
- OpenAPI JSON disponível em `/api/docs`
- Métricas Prometheus em `/actuator/prometheus`
- Cache em memória com Caffeine
- CORS configurável via variável de ambiente `CORS_ALLOWED_ORIGINS`
