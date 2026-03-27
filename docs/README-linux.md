# Guia de Instalação para Linux (Ubuntu, Debian, Fedora e outros)

Bem-vindo! Este guia foi criado para facilitar a instalação e uso do **Javos** em sistemas Linux, mesmo para quem não tem experiência técnica avançada.

---

## Índice

1. [Pré-requisitos](#pré-requisitos)
2. [Instalando o Docker](#instalando-o-docker)
3. [Instalação do JAVOS](#instalação-do-javos)
4. [Usando o JAVOS no dia a dia](#usando-o-javos-no-dia-a-dia)
5. [Backup e Restauração dos Dados](#backup-e-restauração-dos-dados)
6. [Solução de Problemas (Troubleshooting)](#solução-de-problemas-troubleshooting)
7. [Perguntas Frequentes](#perguntas-frequentes)

---

## Pré-requisitos

### Distribuições compatíveis

O JAVOS foi testado nas seguintes distribuições Linux:

| Distribuição        | Versões recomendadas        |
|---------------------|-----------------------------|
| Ubuntu              | 20.04 LTS, 22.04 LTS, 24.04 LTS |
| Debian              | 11 (Bullseye), 12 (Bookworm) |
| Fedora              | 38, 39, 40                  |
| Linux Mint          | 21.x                        |
| openSUSE            | Leap 15.x, Tumbleweed       |
| Arch Linux          | Rolling                     |

> **Outros sistemas:** O JAVOS provavelmente funciona em qualquer distribuição Linux que suporte Docker. Se sua distribuição não está listada, consulte as instruções de instalação do Docker para o seu sistema em [docs.docker.com](https://docs.docker.com/engine/install/).

### Espaço em disco necessário

- **Docker Engine:** ~200 MB
- **JAVOS (imagem Docker):** ~400 MB
- **Total recomendado:** pelo menos 2 GB livres

---

## Instalando o Docker

O Docker é o único programa que você precisa instalar antes do JAVOS. Ele permite rodar a aplicação de forma simples, sem precisar instalar Java, banco de dados ou outras ferramentas separadamente.

### Ubuntu / Debian

Abra o Terminal (atalho: `Ctrl+Alt+T`) e execute os seguintes comandos um de cada vez:

```bash
# 1. Atualize os pacotes
sudo apt update

# 2. Instale dependências necessárias
sudo apt install -y ca-certificates curl gnupg

# 3. Adicione a chave GPG oficial do Docker
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 4. Adicione o repositório do Docker
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. Instale o Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

> **Para Debian:** Substitua `ubuntu` por `debian` nos comandos acima.

### Fedora

```bash
# 1. Instale o plugin do repositório DNF
sudo dnf install -y dnf-plugins-core

# 2. Adicione o repositório do Docker
sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo

# 3. Instale o Docker
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. Inicie e habilite o serviço Docker
sudo systemctl start docker
sudo systemctl enable docker
```

### Configurar Docker sem `sudo` (recomendado)

Por padrão, o Docker requer `sudo` para ser executado. Para usar sem `sudo`:

```bash
# Adicione seu usuário ao grupo docker
sudo usermod -aG docker $USER

# Aplique a mudança (faça logout e login novamente, ou execute:)
newgrp docker
```

> **Importante:** Você precisará sair e entrar novamente na sua sessão para que essa mudança tenha efeito permanente.

### Verificar instalação

```bash
docker --version
docker compose version
```

Se ambos os comandos mostrarem versões, o Docker está instalado corretamente!

---

## Instalação do JAVOS

### Passo 1: Baixe os arquivos do JAVOS

Acesse a página de releases do JAVOS em:
[https://github.com/cesarsuchoj/javos/releases](https://github.com/cesarsuchoj/javos/releases)

Baixe o arquivo `.zip` da versão mais recente e extraia em uma pasta de sua preferência, por exemplo:

```bash
# Crie uma pasta para o JAVOS
mkdir -p ~/javos

# Extraia o arquivo baixado (substitua pelo nome do arquivo correto)
unzip ~/Downloads/javos-*.zip -d ~/javos

# Acesse a pasta deploy
cd ~/javos/deploy
```

Ou, se preferir clonar o repositório com Git:

```bash
git clone https://github.com/cesarsuchoj/javos.git ~/javos
cd ~/javos/deploy
```

### Passo 2: Execute o script de instalação

```bash
# Dê permissão de execução aos scripts (necessário apenas na primeira vez)
chmod +x *.sh

# Execute a instalação
./install.sh
```

> **O que acontece durante a instalação?**
> - O script verifica se o Docker está instalado e funcionando
> - Cria o arquivo de configuração `.env`
> - Baixa e compila a aplicação (pode levar de 5 a 15 minutos na primeira vez)

### Passo 3: Aguarde a conclusão

Você verá mensagens de progresso no terminal. Quando aparecer:

```
==========================================
   Instalação concluída com sucesso!
==========================================
```

A instalação foi concluída com sucesso! 🎉

---

## Usando o JAVOS no dia a dia

### Iniciar a aplicação

```bash
cd ~/javos/deploy
./start.sh
```

Depois acesse: **http://localhost:8080**

**Usuário padrão:** `admin` | **Senha:** `admin123`

> ⚠️ **Altere a senha padrão imediatamente após o primeiro acesso!**

### Parar a aplicação

```bash
./stop.sh
```

### Ver logs (mensagens do sistema)

Os logs são úteis para diagnosticar problemas:

```bash
./logs.sh
```

Pressione `Ctrl+C` para sair dos logs.

### Resetar o banco de dados

> ⚠️ **Cuidado!** Esta operação apaga **todos os dados**. Faça um backup antes!

```bash
./reset.sh
```

---

## Backup e Restauração dos Dados

### Criar um backup

```bash
# Salva em ./backups/ (padrão)
./backup.sh

# Salva em pasta específica
./backup.sh ~/meus-backups

# Salva em disco externo montado
./backup.sh /media/seu-usuario/disco-externo/backups
```

### Restaurar um backup

```bash
./restore.sh backups/javos-backup-20240101_120000.tar.gz
```

### Onde ficam os backups?

Por padrão, os backups são salvos em `deploy/backups/`. Recomendamos copiar os backups para um local externo regularmente:

```bash
# Copiar para outra pasta
cp -r ~/javos/deploy/backups ~/Documentos/backups-javos

# Copiar para disco externo
cp -r ~/javos/deploy/backups /media/seu-usuario/disco-externo/
```

---

## Solução de Problemas (Troubleshooting)

### "Permissão negada" ao executar os scripts

**Sintoma:** Mensagem `bash: ./install.sh: Permission denied`

**Solução:** Adicione permissão de execução:

```bash
chmod +x ~/javos/deploy/*.sh
```

### "Docker: command not found"

**Causa:** O Docker não está instalado ou não está no PATH.

**Solução:** Siga as instruções de [Instalando o Docker](#instalando-o-docker) e depois verifique:

```bash
which docker
docker --version
```

### "Got permission denied while trying to connect to the Docker daemon"

**Causa:** Seu usuário não tem permissão para acessar o Docker sem `sudo`.

**Solução:**

```bash
# Adicione seu usuário ao grupo docker
sudo usermod -aG docker $USER

# Reinicie a sessão (ou execute):
newgrp docker
```

### "Cannot connect to the Docker daemon. Is the docker daemon running?"

**Causa:** O serviço Docker não está em execução.

**Solução:**

```bash
# Verificar status do Docker
sudo systemctl status docker

# Iniciar o Docker se não estiver rodando
sudo systemctl start docker

# Habilitar inicialização automática
sudo systemctl enable docker
```

### A aplicação não abre no navegador

**Verifique:**

1. Se o Docker está rodando:
   ```bash
   docker ps
   ```
   
2. Se os containers do JAVOS estão em execução:
   ```bash
   docker ps | grep javos
   ```
   
3. Se a porta 8080 não está em uso por outro programa:
   ```bash
   sudo lsof -i :8080
   # ou
   sudo ss -tlnp | grep 8080
   ```

4. Se a porta está em uso, mude no arquivo `.env`:
   ```
   APP_PORT=8090
   ```
   
   Depois reinicie: `./stop.sh && ./start.sh`

### Erro "No space left on device"

**Causa:** O disco está cheio.

**Solução:**

```bash
# Verificar espaço em disco
df -h

# Limpar imagens Docker não utilizadas
docker system prune -a

# Verificar quanto espaço o Docker usa
docker system df
```

### Container reiniciando em loop

**Sintoma:** O container JAVOS aparece como `Restarting` no `docker ps`.

**Solução:**

```bash
# Ver os logs de erro
./logs.sh

# Ou verificar diretamente
docker logs javos-app --tail 50
```

Os logs mostrarão a causa do problema. Causas comuns:
- Porta 8080 já em uso por outro programa
- Arquivo de banco de dados corrompido (tente o reset: `./reset.sh`)

### Firewall bloqueando acesso à aplicação

Se outros computadores na rede não conseguem acessar o JAVOS:

**Ubuntu/Debian (UFW):**
```bash
sudo ufw allow 8080/tcp
sudo ufw status
```

**Fedora/RHEL (firewalld):**
```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### Erro ao instalar: "docker: 'compose' is not a docker command"

**Causa:** Versão antiga do Docker instalada (antes do Docker Compose V2).

**Solução:**

```bash
# Verificar versão do Docker Compose
docker compose version

# Se não funcionar, tente a versão antiga
docker-compose version

# Para atualizar, desinstale e reinstale seguindo as instruções acima
```

### SELinux bloqueando o Docker (Fedora/RHEL)

**Sintoma:** Erros de permissão mesmo com Docker rodando corretamente.

**Solução:**

```bash
# Verificar se SELinux está bloqueando
sudo audit2why -a

# Configurar o contexto correto para o volume de dados
sudo chcon -Rt svirt_sandbox_file_t ~/javos/deploy/
```

---

## Perguntas Frequentes

**Preciso usar `sudo` nos scripts?**

Não, se você configurou o Docker para rodar sem `sudo` (seguindo as instruções em [Configurar Docker sem sudo](#configurar-docker-sem-sudo)). Caso contrário, sim.

**Como o JAVOS é acessado por outros computadores na rede?**

1. Descubra o IP local do seu computador:
   ```bash
   ip addr show | grep "inet " | grep -v 127.0.0.1
   # ou
   hostname -I
   ```
2. Outros computadores na mesma rede podem acessar em: `http://SEU-IP:8080`

**Posso usar o JAVOS sem o Docker?**

Sim! Veja a documentação em [`/docs/README.md`](README.md) para rodar sem Docker (requer Java 17+ e Maven).

**O JAVOS inicia automaticamente quando ligo o computador?**

Não por padrão. Para configurar a inicialização automática:

```bash
# Habilitar o Docker para iniciar com o sistema (já deve estar ativo)
sudo systemctl enable docker

# Editar o docker-compose para reinicialização automática
# O arquivo já contém `restart: unless-stopped`, então após o primeiro start,
# o container reiniciará automaticamente.
# Para iniciar o JAVOS automaticamente na primeira vez, use:
./start.sh
```

O container JAVOS já está configurado com `restart: unless-stopped`, o que significa que ele reiniciará automaticamente após reinicializações do sistema, desde que tenha sido iniciado pelo menos uma vez.

**Como atualizar para uma nova versão?**

1. Faça um backup: `./backup.sh`
2. Pare a aplicação: `./stop.sh`
3. Baixe a nova versão e substitua os arquivos da pasta `deploy`
4. Execute a instalação novamente: `./install.sh`
5. Inicie: `./start.sh`

Seus dados serão mantidos (desde que você não execute `./reset.sh`).

**Esqueci minha senha. Como recupero?**

Atualmente, a única forma de recuperar o acesso é:

1. Faça um backup dos dados: `./backup.sh`
2. Execute o reset: `./reset.sh`  
   ⚠️ Isso apaga **todos os dados**!
3. Acesse novamente com usuário `admin` / senha `admin123`

---

## Comandos de Referência Rápida

| O que fazer | Comando |
|-------------|---------|
| Instalar (primeira vez) | `./install.sh` |
| Iniciar | `./start.sh` |
| Parar | `./stop.sh` |
| Ver logs | `./logs.sh` |
| Fazer backup | `./backup.sh` |
| Restaurar backup | `./restore.sh <arquivo>` |
| Resetar tudo | `./reset.sh` |
| Verificar status | `docker ps` |

---

## Precisa de Ajuda?

- 📋 Veja os logs da aplicação: `./logs.sh`
- 🐛 Reporte problemas em: [https://github.com/cesarsuchoj/javos/issues](https://github.com/cesarsuchoj/javos/issues)
- 📖 Documentação completa: [`/docs/README.md`](README.md)
- 🍎 Guia para macOS: [`/docs/README-macos.md`](README-macos.md)
- 🪟 Guia para Windows: [`/docs/README-windows.md`](README-windows.md)
