# Guia de Instalação para macOS (incluindo Apple Silicon M1/M2/M3)

Bem-vindo! Este guia foi criado especialmente para usuários de Mac, incluindo as novas máquinas com chip Apple Silicon (M1, M2 e M3).

---

## Índice

1. [Qual Mac eu tenho?](#qual-mac-eu-tenho)
2. [Pré-requisitos](#pré-requisitos)
3. [Instalando o Docker Desktop](#instalando-o-docker-desktop)
4. [Instalação do JAVOS](#instalação-do-javos)
5. [Usando o JAVOS no dia a dia](#usando-o-javos-no-dia-a-dia)
6. [Backup e Restauração dos Dados](#backup-e-restauração-dos-dados)
7. [Solução de Problemas (Troubleshooting)](#solução-de-problemas-troubleshooting)
8. [Perguntas Frequentes](#perguntas-frequentes)

---

## Qual Mac eu tenho?

Para saber qual chip seu Mac possui:

1. Clique no menu  (Apple) no canto superior esquerdo
2. Selecione **"Sobre este Mac"**
3. Verifique o campo **"Chip"** ou **"Processador"**:
   - Se aparecer **"Apple M1"**, **"Apple M2"** ou **"Apple M3"** → você tem um **Apple Silicon**
   - Se aparecer **"Intel"** → você tem um **Mac Intel**

> **Ambos os tipos são totalmente compatíveis com o JAVOS.** A instalação é praticamente idêntica.

---

## Pré-requisitos

### Versão do macOS recomendada

| Chip | macOS mínimo recomendado |
|------|--------------------------|
| Apple Silicon (M1/M2/M3) | macOS 12 Monterey ou superior |
| Intel | macOS 11 Big Sur ou superior |

### Espaço em disco necessário

- **Docker Desktop:** ~500 MB
- **JAVOS (imagem Docker):** ~400 MB
- **Total recomendado:** pelo menos 2 GB livres

---

## Instalando o Docker Desktop

O Docker é o único programa que você precisa instalar antes do JAVOS. Ele permite rodar a aplicação de forma simples, sem precisar instalar Java, banco de dados ou outras ferramentas.

### Opção 1: Via Homebrew (Recomendado para usuários técnicos)

O [Homebrew](https://brew.sh) é o gerenciador de pacotes mais popular para macOS. Se você já o tem instalado:

```bash
brew install --cask docker
```

Se ainda não tem o Homebrew, instale com:

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

> **Atenção para Apple Silicon:** Após instalar o Homebrew em Macs M1/M2/M3, o terminal pode pedir para você adicionar o Homebrew ao PATH. Siga as instruções exibidas no terminal (geralmente algo como `eval "$(/opt/homebrew/bin/brew shellenv)"`).

### Opção 2: Download Direto

1. Acesse: [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Clique em **"Download for Mac"**
3. **Atenção:** Escolha a versão correta:
   - **"Mac with Apple Silicon"** → para Macs M1/M2/M3
   - **"Mac with Intel chip"** → para Macs Intel
4. Abra o arquivo `.dmg` baixado
5. Arraste o **Docker** para a pasta **Aplicativos**

### Iniciando o Docker Desktop

1. Abra o **Launchpad** (ícone de foguete no Dock)
2. Procure por **"Docker"** e clique para abrir
3. Aguarde o ícone da 🐳 baleia aparecer na **barra de menu** (canto superior direito)
4. Quando o ícone parar de animar, o Docker está pronto!

---

## Instalação do JAVOS

### Passo 1: Baixe os arquivos do JAVOS

Faça o download da última versão do JAVOS em:
[https://github.com/cesarsuchoj/javos/releases](https://github.com/cesarsuchoj/javos/releases)

Extraia o arquivo `.zip` em uma pasta de sua preferência (por exemplo, `~/Documentos/javos`).

### Passo 2: Abra o Terminal

Você pode abrir o Terminal de duas formas:
- Pelo **Spotlight**: pressione `⌘ + Espaço`, digite "Terminal" e pressione Enter
- Pelo **Launchpad**: procure por "Terminal" na pasta "Utilitários"

### Passo 3: Navegue até a pasta `deploy`

No Terminal, execute:

```bash
cd ~/Documentos/javos/deploy
```

> Substitua `~/Documentos/javos` pelo caminho onde você extraiu os arquivos.

### Passo 4: Execute o script de instalação

```bash
./install-macos.sh
```

Se aparecer uma mensagem de permissão negada, execute primeiro:

```bash
chmod +x *.sh
./install-macos.sh
```

> **O que acontece durante a instalação?**
> - O script verifica se o Docker está instalado e funcionando
> - Detecta automaticamente se é Apple Silicon ou Intel
> - Cria o arquivo de configuração `.env`
> - Baixa e compila a aplicação (pode levar de 5 a 15 minutos na primeira vez)

### Passo 5: Aguarde a conclusão

Você verá mensagens de progresso. Quando aparecer:

```
==========================================
   Instalação concluída com sucesso!
==========================================
```

A instalação foi concluída! 🎉

---

## Usando o JAVOS no dia a dia

### Iniciar a aplicação

```bash
./start-macos.sh
```

O navegador abrirá automaticamente com o JAVOS. Caso não abra, acesse manualmente:
[http://localhost:8080](http://localhost:8080)

**Usuário padrão:** `admin` | **Senha:** `admin123`

> ⚠️ **Altere a senha padrão imediatamente após o primeiro acesso!**

### Parar a aplicação

```bash
./stop-macos.sh
```

### Ver logs (mensagens do sistema)

```bash
./logs-macos.sh
```

Pressione `Ctrl+C` para sair dos logs.

### Resetar o banco de dados

> ⚠️ **Cuidado!** Esta operação apaga **todos os dados**. Faça um backup antes!

```bash
./reset-macos.sh
```

---

## Backup e Restauração dos Dados

### Criar um backup

```bash
# Salva em ./backups/ (padrão)
./backup-macos.sh

# Salva em pasta específica
./backup-macos.sh ~/Desktop/backups-javos

# Salva em disco externo
./backup-macos.sh /Volumes/MeuDisco/backups
```

O backup será aberto automaticamente no Finder após ser criado.

### Restaurar um backup

```bash
./restore-macos.sh backups/javos-backup-20240101_120000.tar.gz
```

> O macOS exibirá uma janela de confirmação antes de restaurar.

### Onde ficam os backups?

Por padrão, os backups são salvos em `deploy/backups/`. Recomendamos copiar os backups para um local externo (Time Machine, iCloud, disco externo) regularmente.

---

## Solução de Problemas (Troubleshooting)

### "Permissão negada" ao executar os scripts

**Solução:** Adicione permissão de execução aos scripts:

```bash
chmod +x ~/Documentos/javos/deploy/*.sh
```

### "Docker não encontrado" mesmo após instalar

**Possível causa:** O Docker Desktop não foi iniciado ainda.

**Solução:** Abra o Docker Desktop pelo Launchpad e aguarde o ícone da baleia aparecer na barra de menu.

### "Docker não está em execução"

**Solução:** Inicie o Docker Desktop ou use o comando:

```bash
open -a Docker
```

Aguarde até 30 segundos para o Docker iniciar completamente.

### Aplicação lenta no Apple Silicon (M1/M2/M3)

**Explicação:** Algumas imagens Docker são compiladas para arquitetura x86 (Intel). No Apple Silicon, o Docker as executa via emulação (Rosetta), o que pode ser um pouco mais lento.

**Solução:** Instale o Rosetta 2 se ainda não tiver:

```bash
softwareupdate --install-rosetta --agree-to-license
```

### "Cannot connect to the Docker daemon"

**Solução passo a passo:**
1. Abra o **Monitor de Atividade** (Activity Monitor)
2. Procure por "Docker" e verifique se está rodando
3. Se não estiver, abra o Docker Desktop novamente
4. Se o problema persistir, reinicie o Docker Desktop: clique no ícone da baleia > **Restart**

### A aplicação não abre no navegador

**Verifique:**
1. Se o Docker Desktop está rodando (ícone da baleia na barra de menu)
2. Se a aplicação foi iniciada: `./start-macos.sh`
3. Os logs para erros: `./logs-macos.sh`
4. Se a porta 8080 não está em uso por outro programa

**Para verificar se a porta 8080 está em uso:**

```bash
lsof -i :8080
```

Se estiver em uso, você pode mudar a porta no arquivo `.env`:
```
APP_PORT=8090
```

### macOS bloqueou o script ("não pode ser aberto porque é de desenvolvedor não identificado")

**Solução:**
1. Vá em **Preferências do Sistema** > **Segurança e Privacidade** > **Geral**
2. Clique em **"Abrir mesmo assim"**

Ou execute no Terminal:
```bash
xattr -d com.apple.quarantine ~/Documentos/javos/deploy/*.sh
```

### Homebrew não funciona no Apple Silicon após instalação

No Apple Silicon, o Homebrew é instalado em `/opt/homebrew` (diferente de `/usr/local` no Intel). Adicione ao seu perfil do terminal:

```bash
# Para zsh (padrão no macOS moderno):
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"
```

---

## Perguntas Frequentes

**Posso usar o JAVOS sem o Docker?**

Sim! Veja a documentação em [`/docs/README.md`](README.md) para rodar sem Docker (requer Java 17+ e Maven).

**Preciso deixar o computador ligado para outros acessarem?**

Sim. O JAVOS roda localmente no seu Mac. Para acesso pela rede, o Mac deve estar ligado com a aplicação iniciada.

**Como outros computadores da rede acessam o JAVOS?**

1. Descubra o IP local do seu Mac:
   ```bash
   ipconfig getifaddr en0
   ```
2. Outros computadores na mesma rede podem acessar em: `http://IP-DO-SEU-MAC:8080`

**O JAVOS funciona com o Mac em modo de economia de energia?**

O modo de suspensão interrompe os containers Docker. Para uso contínuo, configure nas **Preferências do Sistema** > **Bateria** para impedir que o Mac durma quando conectado à fonte.

**Como atualizar para uma nova versão?**

1. Faça um backup: `./backup-macos.sh`
2. Pare a aplicação: `./stop-macos.sh`
3. Substitua os arquivos da pasta `deploy` pelos da nova versão
4. Execute a instalação novamente: `./install-macos.sh`
5. Inicie: `./start-macos.sh`

Seus dados serão mantidos (desde que você não execute `./reset-macos.sh`).

**O JAVOS funciona no macOS Ventura/Sonoma/Sequoia?**

Sim! O JAVOS é compatível com todas as versões recentes do macOS. Certifique-se de usar a versão mais recente do Docker Desktop para melhor compatibilidade.

---

## Comandos de Referência Rápida

| O que fazer | Comando |
|-------------|---------|
| Instalar (primeira vez) | `./install-macos.sh` |
| Iniciar | `./start-macos.sh` |
| Parar | `./stop-macos.sh` |
| Ver logs | `./logs-macos.sh` |
| Fazer backup | `./backup-macos.sh` |
| Restaurar backup | `./restore-macos.sh <arquivo>` |
| Resetar tudo | `./reset-macos.sh` |

---

## Precisa de Ajuda?

- 📋 Veja os logs da aplicação: `./logs-macos.sh`
- 🐛 Reporte problemas em: [https://github.com/cesarsuchoj/javos/issues](https://github.com/cesarsuchoj/javos/issues)
- 📖 Documentação completa: [`/docs/README.md`](README.md)
