package com.javos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Javos API")
                        .version("v1")
                        .description("""
                                API REST para o sistema Javos de gestão de ordens de serviço, clientes, \
                                produtos, financeiro e vendas.

                                ## Versionamento

                                Esta API adota **versionamento por caminho** (`/api/v1/...`).

                                - **Versão atual:** `v1`
                                - Mudanças não-compatíveis (breaking changes) são introduzidas em uma nova \
                                versão (ex.: `/api/v2/`).
                                - Versões antigas são mantidas por um período de transição e \
                                depois depreciadas *suavemente* antes da remoção.

                                ### Ciclo de vida de uma versão

                                | Fase | Descrição |
                                |------|-----------|
                                | **Ativa** | Versão atual, sem avisos. |
                                | **Depreciada** | Versão ainda funcional, porém com headers de aviso. |
                                | **Removida** | Versão desativada após a data de sunset. |

                                Quando uma versão está depreciada, as respostas incluem:
                                - `Deprecation: true`
                                - `Sunset: <data de remoção no formato HTTP-date>`
                                - `Warning: 299 - "<mensagem de migração>"`
                                - `X-API-Version: <versão utilizada>`

                                ## Autenticação

                                A maioria dos endpoints requer autenticação via **JWT Bearer Token**.

                                1. Obtenha um token em `POST /api/v1/auth/login`
                                2. Inclua o header: `Authorization: Bearer <token>`
                                3. Tokens expiram em 24 h. Use `POST /api/v1/auth/refresh` para renová-los.

                                ## Rate Limiting

                                - Endpoints de autenticação: **10 requisições / 60 s** por IP
                                - Demais endpoints: **100 requisições / 60 s** por IP

                                Quando o limite é atingido a API retorna `429 Too Many Requests`.

                                ## Erros

                                Todos os erros seguem o formato:
                                ```json
                                {
                                  "status": 404,
                                  "message": "Recurso não encontrado",
                                  "timestamp": "2024-01-01T12:00:00",
                                  "details": {}
                                }
                                ```

                                | Código | Significado |
                                |--------|-------------|
                                | 400 | Dados inválidos ou parâmetro incorreto |
                                | 401 | Não autenticado (token ausente ou expirado) |
                                | 403 | Acesso negado (permissão insuficiente) |
                                | 404 | Recurso não encontrado |
                                | 409 | Conflito (duplicidade ou violação de integridade) |
                                | 429 | Rate limit atingido |
                                | 500 | Erro interno do servidor |
                                """)
                        .contact(new Contact()
                                .name("Javos Contributors")
                                .url("https://github.com/cesarsuchoj/javos"))
                        .license(new License()
                                .name("GNU General Public License v3.0")
                                .url("https://www.gnu.org/licenses/gpl-3.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("Guias completos (autenticação, rate limiting, erros, changelog)")
                        .url("https://github.com/cesarsuchoj/javos/tree/main/docs"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido em POST /api/v1/auth/login. Inclua como: Authorization: Bearer <token>")));
    }
}
