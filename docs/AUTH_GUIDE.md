# Guia de Autenticação – Javos API

## Visão Geral

A Javos API usa **JWT (JSON Web Tokens)** para autenticação stateless. O fluxo completo inclui:

1. **Login** → obtém `accessToken` (24 h) e `refreshToken` (7 dias)
2. **Chamadas autenticadas** → inclui `accessToken` no header `Authorization`
3. **Renovação** → troca o `refreshToken` por um novo par de tokens (rotação)
4. **Logout** → revoga tokens no servidor

---

## Endpoints de Autenticação

Base path: `/api/v1/auth`

| Método | Endpoint   | Descrição                           | Autenticação |
|--------|-----------|--------------------------------------|--------------|
| POST   | /login    | Obtém tokens de acesso               | Não          |
| POST   | /register | Registra novo usuário (role USER)    | Não          |
| POST   | /refresh  | Renova tokens (rotação)              | Não          |
| POST   | /logout   | Encerra sessão e revoga tokens       | Opcional     |

---

## Fluxo de Login

### 1. Realizar Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "SenhaSegura123"
  }'
```

**Resposta (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "username": "admin",
  "name": "Administrador",
  "role": "ROLE_ADMIN",
  "expiresIn": 86400000
}
```

> **Campos:**
> - `token` – access token JWT. Válido por 24 horas.
> - `refreshToken` – UUID opaco para renovação. Válido por 7 dias.
> - `expiresIn` – tempo de expiração do access token em milissegundos.

### 2. Usar o Token nas Requisições

Inclua o `token` no header `Authorization` de todas as chamadas autenticadas:

```bash
curl -X GET http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## Renovação de Token (Refresh)

O access token expira em 24 h. Use o `refreshToken` para obter um novo par sem precisar fazer login novamente.

> ⚠️ O sistema usa **rotação de tokens**: ao fazer refresh, o `refreshToken` anterior é invalidado e um novo é emitido.

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Resposta (200 OK):** Mesmo formato do login, com novos tokens.

**Erros possíveis:**

| Status | Mensagem | Causa |
|--------|----------|-------|
| 400 | Refresh token inválido, expirado ou revogado | Token não existe, já foi usado ou expirou |

---

## Registro de Usuário

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao.silva",
    "email": "joao@exemplo.com",
    "name": "João Silva",
    "password": "MinhaS3nha!"
  }'
```

**Regras de senha:** mínimo 8 caracteres, ao menos uma letra minúscula, uma maiúscula e um número.

**Resposta (201 Created):**

```json
{
  "id": 2,
  "username": "joao.silva",
  "email": "joao@exemplo.com",
  "name": "João Silva",
  "role": "ROLE_USER"
}
```

---

## Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Resposta (204 No Content):** sem corpo.

> O header `Authorization` é opcional no logout, mas recomendado para revogação imediata do access token (blocklist interna).

---

## Roles e Permissões

| Role        | Acesso                                         |
|-------------|------------------------------------------------|
| `ROLE_USER` | Todos os endpoints de negócio (leitura/escrita)|
| `ROLE_ADMIN`| Todos os endpoints + gerenciamento de usuários + actuator |

---

## Headers Comuns

| Header | Obrigatório | Descrição |
|--------|-------------|-----------|
| `Authorization: Bearer <token>` | Sim (exceto auth) | Token JWT de acesso |
| `Content-Type: application/json` | Sim (POST/PUT) | Tipo do corpo da requisição |
| `X-Correlation-Id` | Não | ID de correlação (gerado automaticamente pelo servidor se ausente) |

---

## Exemplos de Erros de Autenticação

```json
// 401 Unauthorized – token ausente ou expirado
{
  "status": 401,
  "message": "Credenciais inválidas",
  "timestamp": "2024-01-15T10:30:00",
  "details": null
}
```

```json
// 403 Forbidden – permissão insuficiente
{
  "status": 403,
  "message": "Acesso negado",
  "timestamp": "2024-01-15T10:30:00",
  "details": null
}
```

---

## Implementação em Clientes

### JavaScript / Fetch

```javascript
// Armazene os tokens após o login
const { token, refreshToken } = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
}).then(r => r.json());

// Use o token nas requisições
const clients = await fetch('/api/v1/clients', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());
```

### Interceptor de renovação automática

```javascript
async function apiFetch(url, options = {}) {
  let response = await fetch(url, {
    ...options,
    headers: { ...options.headers, 'Authorization': `Bearer ${accessToken}` }
  });

  if (response.status === 401 && refreshToken) {
    // Tenta renovar o token
    const renewed = await fetch('/api/v1/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    }).then(r => r.json());

    accessToken = renewed.token;
    refreshToken = renewed.refreshToken;

    // Repete a requisição original com o novo token
    response = await fetch(url, {
      ...options,
      headers: { ...options.headers, 'Authorization': `Bearer ${accessToken}` }
    });
  }

  return response;
}
```
