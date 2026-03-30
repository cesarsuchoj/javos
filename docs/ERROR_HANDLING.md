# Guia de Tratamento de Erros – Javos API

## Formato Padrão de Erro

Todos os erros da Javos API seguem o mesmo formato JSON:

```json
{
  "status": 400,
  "message": "Erro de validação",
  "timestamp": "2024-01-15T10:30:00",
  "details": {
    "email": "Email inválido",
    "password": "Password deve ter entre 8 e 128 caracteres"
  }
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `status` | `integer` | Código HTTP do erro |
| `message` | `string` | Mensagem descritiva do erro |
| `timestamp` | `string` (ISO 8601) | Data e hora do erro |
| `details` | `object` (opcional) | Mapa de campos com erros de validação |

---

## Tabela de Códigos de Status

| Código HTTP | Nome | Quando ocorre |
|-------------|------|---------------|
| `400 Bad Request` | Dados inválidos | Corpo JSON malformado, parâmetros de tipo incorreto ou violação de validação |
| `401 Unauthorized` | Não autenticado | Token JWT ausente, expirado ou inválido |
| `403 Forbidden` | Acesso negado | Usuário autenticado sem permissão para o recurso (`ROLE_ADMIN` necessário) |
| `404 Not Found` | Não encontrado | Recurso com o ID especificado não existe |
| `409 Conflict` | Conflito | Duplicidade (username/email já cadastrado) ou violação de integridade referencial |
| `429 Too Many Requests` | Rate limit | Limite de requisições por IP atingido |
| `500 Internal Server Error` | Erro interno | Erro inesperado no servidor |

---

## Erros por Categoria

### 400 – Validação de Campos

Ocorre quando um ou mais campos não passam nas validações de entrada.

```json
{
  "status": 400,
  "message": "Erro de validação",
  "timestamp": "2024-01-15T10:30:00",
  "details": {
    "username": "Username deve conter apenas letras, números, '.', '_' ou '-'",
    "password": "Password deve conter ao menos uma letra minúscula, uma maiúscula e um número"
  }
}
```

**Causa:** `MethodArgumentNotValidException` (Bean Validation JSR-380)

---

### 400 – Parâmetro de Tipo Incorreto

```json
{
  "status": 400,
  "message": "Valor inválido para o parâmetro 'status'",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Causa:** `MethodArgumentTypeMismatchException` — ex: passar `"invalido"` onde se espera um enum.

**Exemplo:** `PATCH /api/v1/service-orders/1/status?status=INVALIDO`

---

### 401 – Não Autenticado

```json
{
  "status": 401,
  "message": "Credenciais inválidas",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Causas:**
- Login com usuário/senha incorretos
- Token JWT ausente no header `Authorization`
- Token JWT expirado

**Solução:** Faça login novamente em `POST /api/v1/auth/login` ou renove o token em `POST /api/v1/auth/refresh`.

---

### 403 – Acesso Negado

```json
{
  "status": 403,
  "message": "Acesso negado",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Causa:** O usuário está autenticado mas não possui a role necessária (`ROLE_ADMIN`).

**Exemplo:** Um usuário com `ROLE_USER` tentando acessar `GET /api/v1/users` (exclusivo para admins).

---

### 404 – Não Encontrado

```json
{
  "status": 404,
  "message": "Cliente não encontrado com id: 999",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Causa:** `ResourceNotFoundException` — o recurso com o ID especificado não existe no banco de dados.

---

### 409 – Conflito

```json
{
  "status": 409,
  "message": "Username ou email já está em uso",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Causas:**
- `ResourceAlreadyExistsException` — duplicidade de dados únicos (username, email, código de produto)
- `DataIntegrityViolationException` — violação de chave estrangeira (ex: tentar excluir um cliente que possui ordens de serviço)

---

### 429 – Rate Limit

```json
{
  "status": 429,
  "message": "Muitas requisições. Tente novamente em alguns instantes.",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Causa:** Número de requisições por IP excedeu o limite configurado.

**Solução:** Aguarde o intervalo de reposição (60 s) e tente novamente. Veja o [Guia de Rate Limiting](RATE_LIMITING.md).

---

### 500 – Erro Interno

```json
{
  "status": 500,
  "message": "Erro interno do servidor",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Causa:** Exceção não tratada no servidor. Detalhes são logados internamente mas não expostos ao cliente por segurança.

**Ação recomendada:** Abra uma issue com o `X-Correlation-Id` do request para facilitar o diagnóstico.

---

## Tratamento em Clientes

### Padrão Recomendado (JavaScript)

```javascript
async function apiCall(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`,
      ...options.headers
    }
  });

  if (!response.ok) {
    const error = await response.json();

    switch (error.status) {
      case 400:
        // Mostrar erros de validação ao usuário
        console.error('Validação:', error.details || error.message);
        break;
      case 401:
        // Tentar renovar token ou redirecionar para login
        await refreshToken();
        break;
      case 403:
        // Mostrar mensagem de permissão insuficiente
        console.error('Permissão negada:', error.message);
        break;
      case 404:
        console.error('Não encontrado:', error.message);
        break;
      case 409:
        console.error('Conflito:', error.message);
        break;
      case 429:
        // Implementar backoff e retry
        await sleep(1000);
        return apiCall(url, options);
      default:
        console.error('Erro do servidor:', error.message);
    }

    throw new Error(error.message);
  }

  return response.json();
}
```

---

## Header X-Correlation-Id

Cada requisição recebe automaticamente um header de correlação no response:

```http
X-Correlation-Id: 550e8400-e29b-41d4-a716-446655440000
```

Use este ID ao reportar bugs ou problemas para facilitar o rastreamento nos logs do servidor.

Você também pode enviar seu próprio `X-Correlation-Id` no request e ele será preservado na resposta e nos logs.

```bash
curl -X GET http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Correlation-Id: meu-id-customizado-123"
```
