# JAVOS - Guia de Instalação

Bem-vindo! Este guia vai te ajudar a instalar e usar o **Javos** no seu computador, mesmo sem conhecimento técnico.

---

## O que você precisa

Antes de começar, instale o **Docker Desktop**:

- **Windows / Mac:** Acesse https://www.docker.com/products/docker-desktop e clique em "Download"
- **Linux:** Siga as instruções em https://docs.docker.com/engine/install/

> O Docker é um programa gratuito que permite rodar aplicações de forma isolada, sem precisar instalar Java, banco de dados ou outras ferramentas.

---

## Instalação (Primeira Vez)

### Windows

1. Abra a pasta `deploy` no Explorador de Arquivos
2. Dê **duplo clique** em `install.bat`
3. Aguarde a instalação terminar (pode levar alguns minutos)

### Linux / macOS

Abra o Terminal, navegue até a pasta `deploy` e execute:

```bash
chmod +x *.sh
./install.sh
```

---

## Usando o JAVOS

### Iniciar a aplicação

| Sistema | Comando |
|---------|---------|
| Windows | Duplo clique em `start.bat` |
| Linux/Mac | `./start.sh` |

Depois acesse: **http://localhost:8080**

---

### Parar a aplicação

| Sistema | Comando |
|---------|---------|
| Windows | Duplo clique em `stop.bat` |
| Linux/Mac | `./stop.sh` |

---

### Ver os logs (mensagens do sistema)

| Sistema | Comando |
|---------|---------|
| Windows | Duplo clique em `logs.bat` |
| Linux/Mac | `./logs.sh` |

Pressione `Ctrl+C` para sair dos logs.

---

### Resetar o banco de dados

> ⚠️ **Atenção:** Esta operação apaga **todos os dados**! Use apenas se quiser começar do zero.

| Sistema | Comando |
|---------|---------|
| Windows | Duplo clique em `reset.bat` |
| Linux/Mac | `./reset.sh` |

---

## Configuração (Avançado)

As configurações ficam no arquivo `.env` dentro da pasta `deploy`.

Se o arquivo `.env` não existir, copie o `.env.example`:
- Windows: `copy .env.example .env`
- Linux/Mac: `cp .env.example .env`

### Configurações disponíveis

| Configuração | Descrição | Padrão |
|---|---|---|
| `APP_PORT` | Porta de acesso à aplicação | `8080` |
| `JWT_SECRET` | Chave de segurança (mude em produção!) | (gerada) |

### Trocar o banco de dados

Por padrão, o Javos usa **SQLite** (sem configuração extra).

Para usar **MySQL** (recomendado para múltiplos usuários ou uso em rede):

1. Abra o arquivo `.env` em qualquer editor de texto
2. Adicione `#` no início das linhas da **OPÇÃO 1** (SQLite)
3. Remova o `#` do início das linhas da **OPÇÃO 2** (MySQL)
4. Para a aplicação: `./stop.sh` (ou `stop.bat`)
5. Inicie novamente: `./start.sh` (ou `start.bat`)

---

## Perguntas Frequentes

**A aplicação não abre no navegador. O que faço?**
- Verifique se o Docker Desktop está aberto e rodando
- Execute `./logs.sh` para ver se há mensagens de erro
- Aguarde mais alguns segundos após iniciar (o primeiro carregamento é mais lento)

**Esqueci minha senha. Como recupero?**
- Execute `./reset.sh` para resetar o banco de dados
- ⚠️ Isso apaga todos os dados!

**Posso instalar em outro computador da rede?**
- Sim! Copie a pasta `deploy` para o outro computador e repita os passos de instalação
- Os dados de cada instalação são independentes

**Como atualizar para uma nova versão?**
1. Pare a aplicação: `./stop.sh`
2. Substitua os arquivos da pasta `deploy` pelos da nova versão
3. Execute `./install.sh` novamente (seus dados serão mantidos)
4. Inicie: `./start.sh`
