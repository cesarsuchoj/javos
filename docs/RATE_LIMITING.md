# Guia de Rate Limiting – Javos API

## Visão Geral

A Javos API implementa **rate limiting por IP** usando o algoritmo **token bucket** (biblioteca Bucket4j). Isso protege a API contra abuso e ataques de força bruta.

---

## Limites Padrão

| Endpoint | Limite | Janela |
|----------|--------|--------|
| `/api/v1/auth/**` (login, register, refresh) | **10 requisições** | 60 segundos |
| Todos os outros endpoints (`/api/v1/**`) | **100 requisições** | 60 segundos |

> Os limites são aplicados **por endereço IP de origem** de forma independente por bucket.

---

## Resposta quando o Limite é Atingido

Quando o limite é excedido, a API retorna:

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "status": 429,
  "message": "Muitas requisições. Tente novamente em alguns instantes.",
  "timestamp": "2024-01-15T10:30:00",
  "details": null
}
```

---

## Configuração

Os limites podem ser ajustados via variáveis de ambiente:

| Variável de Ambiente | Padrão | Descrição |
|---------------------|--------|-----------|
| `RATE_LIMIT_ENABLED` | `true` | Habilita/desabilita o rate limiting |
| `RATE_LIMIT_AUTH_CAPACITY` | `10` | Capacidade máxima do bucket de autenticação |
| `RATE_LIMIT_AUTH_REFILL_TOKENS` | `10` | Tokens repostos por intervalo (auth) |
| `RATE_LIMIT_AUTH_REFILL_DURATION_SECONDS` | `60` | Intervalo de reposição em segundos (auth) |
| `RATE_LIMIT_API_CAPACITY` | `100` | Capacidade máxima do bucket geral |
| `RATE_LIMIT_API_REFILL_TOKENS` | `100` | Tokens repostos por intervalo (API) |
| `RATE_LIMIT_API_REFILL_DURATION_SECONDS` | `60` | Intervalo de reposição em segundos (API) |

### Exemplo no arquivo `.env`

```env
RATE_LIMIT_ENABLED=true
RATE_LIMIT_AUTH_CAPACITY=10
RATE_LIMIT_API_CAPACITY=200
```

---

## Comportamento do Token Bucket

O algoritmo funciona da seguinte forma:

1. Cada IP possui um **bucket** com capacidade configurada (ex: 100 tokens).
2. Cada requisição **consome 1 token** do bucket.
3. Tokens são **repostos automaticamente** no intervalo configurado (ex: 100 tokens a cada 60 s).
4. Se o bucket está vazio (0 tokens), a requisição é rejeitada com `429`.
5. Buckets inativos são removidos automaticamente da memória.

---

## Boas Práticas para Clientes

1. **Implemente retry com backoff exponencial** para lidar com erros 429:

```javascript
async function fetchWithRetry(url, options, maxRetries = 3) {
  for (let attempt = 0; attempt < maxRetries; attempt++) {
    const response = await fetch(url, options);
    if (response.status !== 429) return response;

    const delay = Math.pow(2, attempt) * 1000; // 1s, 2s, 4s
    await new Promise(resolve => setTimeout(resolve, delay));
  }
  throw new Error('Rate limit exceeded after retries');
}
```

2. **Faça cache de respostas** quando possível para evitar requisições desnecessárias.

3. **Agrupe requisições** em batch quando a API oferecer suporte.

4. **Monitore o consumo** – em ambientes de produção, considere configurar alertas para IPs que frequentemente atingem o limite.

---

## Ambiente de Testes

O rate limiting é **desabilitado automaticamente** no perfil de testes (`spring.profiles.active=test`) para não interferir nos testes automatizados:

```yaml
# application-test.yml
javos:
  rate-limit:
    enabled: false
```

---

## Implementação Técnica

- **Biblioteca:** [Bucket4j](https://github.com/bucket4j/bucket4j) 8.10.1
- **Armazenamento:** In-memory (por instância)
- **Estratégia:** Token bucket separado por IP e por tipo de endpoint (auth vs. API geral)
- **Classe:** `com.javos.config.RateLimitFilter`
