# JAVOS - Guia de Instalação

Bem-vindo! Este guia vai te ajudar a instalar e usar o **Javos** no seu computador, mesmo sem conhecimento técnico.

> 🍎 **Usuário de Mac (especialmente M1/M2/M3)?** Veja o guia dedicado: [`../docs/README-macos.md`](../docs/README-macos.md)

---

## Índice

1. [O que você precisa](#o-que-você-precisa)
2. [Instalação por Sistema Operacional](#instalação-por-sistema-operacional)
3. [Usando o JAVOS](#usando-o-javos)
4. [Backup e Restauração](#backup-e-restauração)
5. [Configuração](#configuração-avançado)
6. [Scripts disponíveis](#scripts-disponíveis)
7. [Perguntas Frequentes](#perguntas-frequentes)

---

## O que você precisa

Antes de começar, instale o **Docker Desktop**:

- **Windows / Mac:** Acesse https://www.docker.com/products/docker-desktop e clique em "Download"
  - Para Mac M1/M2/M3: escolha a versão **"Apple Silicon"**
- **Linux:** Siga as instruções em https://docs.docker.com/engine/install/

> O Docker é um programa gratuito que permite rodar aplicações de forma isolada, sem precisar instalar Java, banco de dados ou outras ferramentas.

---

## Instalação por Sistema Operacional

### Windows

**Opção A — Arquivo .bat (recomendado para iniciantes):**

1. Abra a pasta `deploy` no Explorador de Arquivos
2. Dê **duplo clique** em `install.bat`
3. Aguarde a instalação terminar (pode levar alguns minutos)

**Opção B — PowerShell:**

```powershell
# Se necessário, habilite scripts PowerShell:
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser

# Instalar:
.\install.ps1
```

### Linux

Abra o Terminal, navegue até a pasta `deploy` e execute:

```bash
chmod +x *.sh
./install.sh
```

### macOS (incluindo Apple Silicon M1/M2/M3)

> 📖 Para um guia completo com troubleshooting específico para Mac, veja: [`../docs/README-macos.md`](../docs/README-macos.md)

```bash
chmod +x *.sh
./install-macos.sh
```

O script detecta automaticamente se você tem um Mac Intel ou Apple Silicon.

---

## Usando o JAVOS

### Iniciar a aplicação

| Sistema | .bat / .sh | PowerShell |
|---------|-----------|------------|
| Windows | Duplo clique em `start.bat` | `.\start.ps1` |
| Linux | `./start.sh` | — |
| macOS | `./start-macos.sh` | — |

Depois acesse: **http://localhost:8080**

> No macOS, o navegador abre automaticamente!

---

### Parar a aplicação

| Sistema | .bat / .sh | PowerShell |
|---------|-----------|------------|
| Windows | Duplo clique em `stop.bat` | `.\stop.ps1` |
| Linux | `./stop.sh` | — |
| macOS | `./stop-macos.sh` | — |

---

### Ver os logs (mensagens do sistema)

| Sistema | .bat / .sh | PowerShell |
|---------|-----------|------------|
| Windows | Duplo clique em `logs.bat` | `.\logs.ps1` |
| Linux | `./logs.sh` | — |
| macOS | `./logs-macos.sh` | — |

Pressione `Ctrl+C` para sair dos logs.

---

### Resetar o banco de dados

> ⚠️ **Atenção:** Esta operação apaga **todos os dados**! Faça um backup antes.

| Sistema | .bat / .sh | PowerShell |
|---------|-----------|------------|
| Windows | Duplo clique em `reset.bat` | `.\reset.ps1` |
| Linux | `./reset.sh` | — |
| macOS | `./reset-macos.sh` | — |

---

## Backup e Restauração

### Criar um backup

| Sistema | .bat / .sh | PowerShell |
|---------|-----------|------------|
| Windows | Duplo clique em `backup.bat` | `.\backup.ps1` |
| Linux | `./backup.sh` | — |
| macOS | `./backup-macos.sh` | — |

O backup é salvo em `./backups/javos-backup-YYYYMMDD_HHMMSS.tar.gz`.

Para salvar em outro local:
- Linux/macOS: `./backup.sh /caminho/para/pasta`
- Windows: `backup.bat C:\caminho\para\pasta`
- PowerShell: `.\backup.ps1 -BackupDir C:\caminho\para\pasta`

### Restaurar um backup

| Sistema | .bat / .sh | PowerShell |
|---------|-----------|------------|
| Windows | `restore.bat backups\arquivo.tar.gz` | `.\restore.ps1 backups\arquivo.tar.gz` |
| Linux | `./restore.sh backups/arquivo.tar.gz` | — |
| macOS | `./restore-macos.sh backups/arquivo.tar.gz` | — |

> No Windows, você também pode **arrastar o arquivo .tar.gz** para cima do `restore.bat`.

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
4. Para a aplicação: `./stop.sh` (ou `stop.bat` / `.\stop.ps1`)
5. Inicie novamente: `./start.sh` (ou `start.bat` / `.\start.ps1`)

---

## Scripts disponíveis

### Linux

| Script | Descrição |
|--------|-----------|
| `install.sh` | Instalação inicial |
| `start.sh` | Iniciar a aplicação |
| `stop.sh` | Parar a aplicação |
| `logs.sh` | Ver logs em tempo real |
| `reset.sh` | Resetar banco de dados |
| `backup.sh` | Criar backup dos dados |
| `restore.sh` | Restaurar backup |
| `build.sh` | Build manual da aplicação |

### macOS (incluindo M1/M2/M3)

| Script | Descrição |
|--------|-----------|
| `install-macos.sh` | Instalação com suporte a Homebrew e Apple Silicon |
| `start-macos.sh` | Iniciar (abre o navegador automaticamente) |
| `stop-macos.sh` | Parar a aplicação |
| `logs-macos.sh` | Ver logs em tempo real |
| `reset-macos.sh` | Resetar (com confirmação nativa macOS) |
| `backup-macos.sh` | Criar backup (abre o Finder após criar) |
| `restore-macos.sh` | Restaurar backup |

### Windows — Arquivos .bat

| Script | Descrição |
|--------|-----------|
| `install.bat` | Instalação inicial |
| `start.bat` | Iniciar a aplicação |
| `stop.bat` | Parar a aplicação |
| `logs.bat` | Ver logs em tempo real |
| `reset.bat` | Resetar banco de dados |
| `backup.bat` | Criar backup dos dados |
| `restore.bat` | Restaurar backup |

### Windows — PowerShell

| Script | Descrição |
|--------|-----------|
| `install.ps1` | Instalação inicial |
| `start.ps1` | Iniciar (abre o navegador automaticamente) |
| `stop.ps1` | Parar a aplicação |
| `logs.ps1` | Ver logs em tempo real |
| `reset.ps1` | Resetar banco de dados |
| `backup.ps1` | Criar backup dos dados |
| `restore.ps1` | Restaurar backup |

> **Para usar os scripts PowerShell**, pode ser necessário habilitar a execução de scripts:
> ```powershell
> Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
> ```

---

## Perguntas Frequentes

**A aplicação não abre no navegador. O que faço?**
- Verifique se o Docker Desktop está aberto e rodando
- Execute `./logs.sh` (Linux), `./logs-macos.sh` (macOS) ou `logs.bat` (Windows) para ver mensagens de erro
- Aguarde mais alguns segundos após iniciar (o primeiro carregamento é mais lento)

**Esqueci minha senha. Como recupero?**
- Faça um backup dos dados antes: `./backup.sh` ou `backup.bat`
- Execute o reset: `./reset.sh` (Linux), `./reset-macos.sh` (macOS) ou `reset.bat` (Windows)
- ⚠️ Isso apaga todos os dados!

**Posso instalar em outro computador da rede?**
- Sim! Copie a pasta `deploy` para o outro computador e repita os passos de instalação
- Os dados de cada instalação são independentes

**Como atualizar para uma nova versão?**
1. Faça um backup: `./backup.sh` (Linux), `./backup-macos.sh` (macOS) ou `backup.bat` (Windows)
2. Pare a aplicação: `./stop.sh` / `stop.bat`
3. Substitua os arquivos da pasta `deploy` pelos da nova versão
4. Execute a instalação novamente (seus dados serão mantidos)
5. Inicie: `./start.sh` / `start.bat`

**Tenho um Mac com M1/M2/M3. Tem algo especial que preciso fazer?**
- Leia o guia dedicado: [`../docs/README-macos.md`](../docs/README-macos.md)
- Use os scripts `*-macos.sh` que detectam automaticamente o Apple Silicon
- Se encontrar problemas de compatibilidade, instale o Rosetta 2: `softwareupdate --install-rosetta --agree-to-license`
