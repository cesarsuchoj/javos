# Checklist de Usabilidade e Testes Multiplataforma

Este checklist serve para verificar se o JAVOS está funcionando corretamente em cada plataforma. Use-o antes de liberar novas versões ou para diagnosticar problemas.

---

## Como usar este checklist

- ✅ Marque os itens que funcionaram corretamente
- ❌ Marque os itens que apresentaram problemas
- ➖ Marque os itens que não se aplicam ao seu ambiente
- 📝 Anote observações nos campos indicados

---

## Plataformas

- [ ] **Windows 10** (versão: _______)
- [ ] **Windows 11**
- [ ] **Ubuntu** (versão: _______)
- [ ] **Debian** (versão: _______)
- [ ] **Fedora** (versão: _______)
- [ ] **macOS** + Intel (versão: _______)
- [ ] **macOS** + Apple Silicon M1/M2/M3 (versão: _______)

---

## 1. Instalação

### 1.1 Pré-requisitos

| Item | Windows | Linux | macOS | Observações |
|------|---------|-------|-------|-------------|
| Docker instalado com sucesso | ☐ | ☐ | ☐ | |
| Docker iniciando sem erros | ☐ | ☐ | ☐ | |
| Versão do Docker compatível (20+) | ☐ | ☐ | ☐ | |
| Docker Compose disponível | ☐ | ☐ | ☐ | |

### 1.2 Script de instalação

| Item | Windows (.bat) | Windows (PS) | Linux | macOS | Observações |
|------|---------------|--------------|-------|-------|-------------|
| Script executa sem erros | ☐ | ☐ | ☐ | ☐ | |
| Arquivo `.env` criado | ☐ | ☐ | ☐ | ☐ | |
| Imagem Docker baixada | ☐ | ☐ | ☐ | ☐ | |
| Container iniciado | ☐ | ☐ | ☐ | ☐ | |
| Mensagem de sucesso exibida | ☐ | ☐ | ☐ | ☐ | |
| Tempo de instalação: | ___min | ___min | ___min | ___min | |

---

## 2. Inicialização e Acesso

| Item | Windows | Linux | macOS | Observações |
|------|---------|-------|-------|-------------|
| Script de início executa sem erros | ☐ | ☐ | ☐ | |
| Aplicação disponível em `localhost:8080` | ☐ | ☐ | ☐ | |
| Navegador abre automaticamente (macOS) | ➖ | ➖ | ☐ | |
| Tempo até a tela de login (segundos): | ___ | ___ | ___ | |
| Página de login carrega sem erros | ☐ | ☐ | ☐ | |

---

## 3. Interface do Usuário — Tela de Login

| Item | Resultado | Observações |
|------|-----------|-------------|
| Campo "Usuário" aceita entrada | ☐ | |
| Campo "Senha" mascara o texto digitado | ☐ | |
| Foco automático no campo "Usuário" | ☐ | |
| Login com credenciais corretas (`admin`/`admin123`) funciona | ☐ | |
| Redirecionamento para dashboard após login | ☐ | |
| Mensagem de erro ao usar senha incorreta | ☐ | |
| Mensagem de erro ao deixar campos em branco | ☐ | |
| Mensagem de erro ao usar servidor indisponível | ☐ | |
| Botão desabilitado durante carregamento | ☐ | |
| Texto "Entrando..." exibido durante carregamento | ☐ | |
| Mensagens de erro são claras e compreensíveis | ☐ | |

---

## 4. Interface do Usuário — Dashboard

| Item | Resultado | Observações |
|------|-----------|-------------|
| Dashboard carrega após login | ☐ | |
| Total de usuários exibido corretamente | ☐ | |
| Nome do usuário logado exibido | ☐ | |
| Versão do sistema exibida | ☐ | |
| Sidebar/menu visível | ☐ | |
| Header com opção de logout | ☐ | |
| Mensagem amigável em caso de erro no dashboard | ☐ | |

---

## 5. Autenticação e Segurança

| Item | Resultado | Observações |
|------|-----------|-------------|
| Acesso ao dashboard sem login redireciona para login | ☐ | |
| Token JWT expira após período configurado | ☐ | |
| Logout limpa a sessão corretamente | ☐ | |
| Após logout, não é possível voltar ao dashboard sem login | ☐ | |
| Acesso a `/api/*` sem token retorna erro 401 | ☐ | |

---

## 6. Mensagens do Sistema

### 6.1 Mensagens de Erro — Login

| Cenário | Mensagem exibida | Esperado? | Observações |
|---------|-----------------|-----------|-------------|
| Usuário/senha incorretos | | "Usuário ou senha incorretos. Verifique os dados e tente novamente." | |
| Campos em branco | | (validação nativa do navegador) | |
| Servidor indisponível | | "Não foi possível conectar ao servidor. Verifique se a aplicação está rodando." | |
| Muitas tentativas | | Mensagem de bloqueio temporário | |

### 6.2 Mensagens de Erro — Dashboard

| Cenário | Mensagem exibida | Esperado? | Observações |
|---------|-----------------|-----------|-------------|
| Falha ao carregar dados | | Mensagem clara com sugestão de ação | |
| Sessão expirada | | Redirecionamento para login com aviso | |

---

## 7. Scripts de Operação

### 7.1 Script de Parada

| Item | Windows | Linux | macOS | Observações |
|------|---------|-------|-------|-------------|
| Para a aplicação sem erros | ☐ | ☐ | ☐ | |
| Mensagem de confirmação exibida | ☐ | ☐ | ☐ | |

### 7.2 Script de Logs

| Item | Windows | Linux | macOS | Observações |
|------|---------|-------|-------|-------------|
| Exibe logs em tempo real | ☐ | ☐ | ☐ | |
| `Ctrl+C` encerra corretamente | ☐ | ☐ | ☐ | |
| Logs são legíveis e compreensíveis | ☐ | ☐ | ☐ | |

### 7.3 Script de Backup

| Item | Windows | Linux | macOS | Observações |
|------|---------|-------|-------|-------------|
| Cria arquivo de backup sem erros | ☐ | ☐ | ☐ | |
| Arquivo `.tar.gz` criado em `backups/` | ☐ | ☐ | ☐ | |
| Nome do arquivo inclui data/hora | ☐ | ☐ | ☐ | |
| Finder abre após backup (macOS) | ➖ | ➖ | ☐ | |
| Backup com pasta personalizada funciona | ☐ | ☐ | ☐ | |

### 7.4 Script de Restauração

| Item | Windows | Linux | macOS | Observações |
|------|---------|-------|-------|-------------|
| Restaura backup sem erros | ☐ | ☐ | ☐ | |
| Dados restaurados corretamente | ☐ | ☐ | ☐ | |
| Confirmação solicitada antes de restaurar (macOS) | ➖ | ➖ | ☐ | |

### 7.5 Script de Reset

| Item | Windows | Linux | macOS | Observações |
|------|---------|-------|-------|-------------|
| Solicita confirmação antes de resetar | ☐ | ☐ | ☐ | |
| Reseta banco de dados corretamente | ☐ | ☐ | ☐ | |
| Sistema acessível após reset | ☐ | ☐ | ☐ | |
| Usuário `admin`/`admin123` funciona após reset | ☐ | ☐ | ☐ | |

---

## 8. Compatibilidade com Navegadores

| Navegador | Versão | Login | Dashboard | Observações |
|-----------|--------|-------|-----------|-------------|
| Google Chrome | ___ | ☐ | ☐ | |
| Mozilla Firefox | ___ | ☐ | ☐ | |
| Microsoft Edge | ___ | ☐ | ☐ | |
| Safari | ___ | ☐ | ☐ | |
| Brave | ___ | ☐ | ☐ | |

---

## 9. Testes de Configuração

### 9.1 Arquivo `.env`

| Item | Resultado | Observações |
|------|-----------|-------------|
| Mudança de porta (`APP_PORT`) funciona | ☐ | |
| JWT_SECRET personalizado aceito | ☐ | |
| Configuração MySQL funciona | ☐ | |

### 9.2 Banco de Dados

| Item | Resultado | Observações |
|------|-----------|-------------|
| SQLite (padrão) funciona | ☐ | |
| MySQL funciona (se configurado) | ☐ | |
| Dados persistem após reinicialização do container | ☐ | |

---

## 10. Testes de Resiliência

| Cenário | Resultado esperado | Resultado obtido | Observações |
|---------|-------------------|-----------------|-------------|
| Reiniciar container | Aplicação volta automaticamente | ☐ | |
| Reiniciar o computador | Container reinicia com o Docker | ☐ | |
| Desconectar da internet durante uso | Dados locais preservados | ☐ | |
| Acesso simultâneo (múltiplos usuários) | Funciona sem erros | ☐ | |

---

## 11. Documentação

| Item | Resultado | Observações |
|------|-----------|-------------|
| Guia Linux (`docs/README-linux.md`) é claro e completo | ☐ | |
| Guia Windows (`docs/README-windows.md`) é claro e completo | ☐ | |
| Guia macOS (`docs/README-macos.md`) é claro e completo | ☐ | |
| FAQ (`docs/FAQ.md`) responde as dúvidas comuns | ☐ | |
| `deploy/README.md` cobre todos os sistemas | ☐ | |
| Links entre documentos funcionam corretamente | ☐ | |

---

## Resultado Final

| Plataforma | Total de itens testados | Passou | Falhou | % Sucesso |
|------------|------------------------|--------|--------|-----------|
| Windows 10/11 | | | | |
| Ubuntu/Debian | | | | |
| Fedora | | | | |
| macOS Intel | | | | |
| macOS Apple Silicon | | | | |

---

## Problemas Encontrados

Use este espaço para documentar problemas não cobertos pelo checklist:

| # | Descrição do problema | Plataforma | Severidade | Status |
|---|----------------------|------------|------------|--------|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

---

## Responsável pelos testes

- **Nome:** _______________________________
- **Data:** _______________________________
- **Versão do JAVOS:** _______________________________
- **Observações gerais:** _______________________________

---

## Referências

- [Documentação técnica](README.md)
- [Guia Windows](README-windows.md)
- [Guia macOS](README-macos.md)
- [Guia Linux](README-linux.md)
- [FAQ](FAQ.md)
- [Reportar problema](https://github.com/cesarsuchoj/javos/issues)
