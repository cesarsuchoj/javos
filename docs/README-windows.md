# Guia de Instalação para Windows (10 e 11)

Bem-vindo! Este guia foi criado para facilitar a instalação e uso do **Javos** no Windows, mesmo para quem não tem experiência técnica.

---

## Índice

1. [Qual versão do Windows eu tenho?](#qual-versão-do-windows-eu-tenho)
2. [Pré-requisitos](#pré-requisitos)
3. [Instalando o Docker Desktop](#instalando-o-docker-desktop)
4. [Instalação do JAVOS](#instalação-do-javos)
5. [Usando o JAVOS no dia a dia](#usando-o-javos-no-dia-a-dia)
6. [Backup e Restauração dos Dados](#backup-e-restauração-dos-dados)
7. [Solução de Problemas (Troubleshooting)](#solução-de-problemas-troubleshooting)
8. [Perguntas Frequentes](#perguntas-frequentes)

---

## Qual versão do Windows eu tenho?

Para verificar sua versão do Windows:

1. Pressione as teclas `Windows + R` simultaneamente
2. Digite `winver` e pressione `Enter`
3. Uma janela mostrará a versão do seu Windows

| Versão | Suporte |
|--------|---------|
| Windows 11 (qualquer edição) | ✅ Totalmente suportado |
| Windows 10 versão 2004 ou superior | ✅ Suportado |
| Windows 10 versão anterior a 2004 | ⚠️ Pode funcionar com limitações |
| Windows 7 / 8 / 8.1 | ❌ Não suportado |

---

## Pré-requisitos

### Virtualização habilitada

O Docker requer que a **virtualização de hardware** esteja habilitada no seu computador. A maioria dos computadores modernos já tem isso habilitado.

Para verificar:
1. Abra o **Gerenciador de Tarefas** (clique direito na barra de tarefas)
2. Vá na aba **Desempenho** → **CPU**
3. Verifique se **Virtualização** está como **Habilitada**

Se estiver desabilitada, você precisará habilitá-la na BIOS/UEFI do seu computador. Consulte o manual do fabricante ou pesquise pelo modelo do seu computador + "habilitar virtualização BIOS".

### Espaço em disco necessário

- **Docker Desktop:** ~1 GB
- **WSL 2 (subsistema Linux):** ~500 MB
- **JAVOS (imagem Docker):** ~400 MB
- **Total recomendado:** pelo menos 3 GB livres

---

## Instalando o Docker Desktop

O Docker é o único programa que você precisa instalar antes do JAVOS.

### Passo 1: Baixe o Docker Desktop

1. Acesse: [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Clique no botão **"Download for Windows"**
3. Aguarde o download do arquivo `Docker Desktop Installer.exe`

### Passo 2: Instale o Docker Desktop

1. Dê **duplo clique** no arquivo `Docker Desktop Installer.exe`
2. Se aparecer um aviso de segurança do Windows, clique em **"Sim"** (ou "Run")
3. Siga as instruções do instalador:
   - Marque a opção **"Use WSL 2 instead of Hyper-V"** (recomendado)
   - Clique em **"Ok"** e aguarde a instalação
4. Ao finalizar, clique em **"Close and restart"** para reiniciar o computador

### Passo 3: Configure o WSL 2 (se necessário)

Na primeira execução do Docker Desktop, pode aparecer uma mensagem pedindo para instalar ou atualizar o WSL 2:

1. Clique no link da mensagem ou acesse: [https://aka.ms/wsl2kernel](https://aka.ms/wsl2kernel)
2. Baixe e execute o pacote de atualização do kernel do Linux
3. Reinicie o Docker Desktop

### Passo 4: Inicie o Docker Desktop

1. Procure o ícone do **Docker** na área de trabalho ou no Menu Iniciar
2. Clique para abrir
3. Aguarde o ícone da 🐳 baleia aparecer na **barra de tarefas** (canto inferior direito)
4. Quando o ícone aparecer, o Docker está pronto!

> **Primeira execução:** O Docker Desktop pode levar alguns minutos para iniciar pela primeira vez.

---

## Instalação do JAVOS

### Passo 1: Baixe os arquivos do JAVOS

Acesse a página de releases do JAVOS em:
[https://github.com/cesarsuchoj/javos/releases](https://github.com/cesarsuchoj/javos/releases)

Baixe o arquivo `.zip` da versão mais recente e extraia em uma pasta de sua preferência, por exemplo: `C:\javos`

### Passo 2: Abra a pasta `deploy`

1. Abra o **Explorador de Arquivos** (ícone de pasta na barra de tarefas)
2. Navegue até a pasta onde extraiu o JAVOS
3. Abra a subpasta **`deploy`**

### Passo 3: Execute o instalador

**Opção A — Arquivo .bat (mais simples):**

1. Na pasta `deploy`, dê **duplo clique** em `install.bat`
2. Uma janela de terminal aparecerá com o progresso da instalação
3. Aguarde até aparecer a mensagem de conclusão

**Opção B — PowerShell:**

1. Clique com o botão direito em uma área vazia da pasta `deploy` com `Shift` pressionado
2. Selecione **"Abrir janela do PowerShell aqui"** ou **"Abrir no Terminal"**
3. Execute:
   ```powershell
   # Se necessário, habilite a execução de scripts (apenas uma vez):
   Set-ExecutionPolicy RemoteSigned -Scope CurrentUser

   # Execute a instalação:
   .\install.ps1
   ```

> **O que acontece durante a instalação?**
> - Verifica se o Docker está instalado e funcionando
> - Cria o arquivo de configuração `.env`
> - Baixa e compila a aplicação (pode levar de 5 a 15 minutos na primeira vez)

### Passo 4: Aguarde a conclusão

Quando aparecer:

```
==========================================
   Instalação concluída com sucesso!
==========================================
```

A instalação foi concluída! 🎉

---

## Usando o JAVOS no dia a dia

### Iniciar a aplicação

**Via arquivo .bat:**
- Dê **duplo clique** em `start.bat` na pasta `deploy`

**Via PowerShell:**
```powershell
.\start.ps1
```

Depois acesse: **http://localhost:8080**

**Usuário padrão:** `admin` | **Senha:** `admin123`

> ⚠️ **Altere a senha padrão imediatamente após o primeiro acesso!**

### Parar a aplicação

**Via arquivo .bat:**
- Dê **duplo clique** em `stop.bat`

**Via PowerShell:**
```powershell
.\stop.ps1
```

### Ver logs (mensagens do sistema)

**Via arquivo .bat:**
- Dê **duplo clique** em `logs.bat`

**Via PowerShell:**
```powershell
.\logs.ps1
```

Pressione `Ctrl+C` para sair dos logs.

### Resetar o banco de dados

> ⚠️ **Cuidado!** Esta operação apaga **todos os dados**. Faça um backup antes!

**Via arquivo .bat:**
- Dê **duplo clique** em `reset.bat`

**Via PowerShell:**
```powershell
.\reset.ps1
```

---

## Backup e Restauração dos Dados

### Criar um backup

**Via arquivo .bat:**
- Dê **duplo clique** em `backup.bat`
- O backup é salvo automaticamente em `deploy\backups\`

**Via PowerShell (com pasta personalizada):**
```powershell
# Salvar no local padrão:
.\backup.ps1

# Salvar em pasta específica:
.\backup.ps1 -BackupDir C:\meus-backups

# Salvar em disco externo:
.\backup.ps1 -BackupDir D:\backups-javos
```

### Restaurar um backup

**Via arquivo .bat:**
- Arraste o arquivo `.tar.gz` para cima do `restore.bat`

Ou, pelo Prompt de Comando (cmd), navegue até a pasta `deploy` e execute:
```cmd
restore.bat backups\javos-backup-20240101_120000.tar.gz
```

**Via PowerShell:**
```powershell
.\restore.ps1 backups\javos-backup-20240101_120000.tar.gz
```

### Onde ficam os backups?

Por padrão, os backups ficam em `deploy\backups\`. Recomendamos copiar os backups regularmente para:
- Um disco externo (HD/pendrive)
- OneDrive, Google Drive ou outro serviço na nuvem

---

## Solução de Problemas (Troubleshooting)

### Instalação do Docker falhou ou não iniciou

**Possível causa 1:** WSL 2 não instalado ou desatualizado.

**Solução:**
1. Abra o **PowerShell como Administrador** (clique direito no ícone do PowerShell → "Executar como administrador")
2. Execute: `wsl --update`
3. Reinicie o computador e tente novamente

**Possível causa 2:** Virtualização desabilitada.

**Solução:** Habilite a virtualização na BIOS/UEFI do seu computador (consulte o manual do fabricante).

### "Docker não está em execução" ou "Cannot connect to the Docker daemon"

**Causa:** O Docker Desktop não foi iniciado ou não terminou de inicializar.

**Solução:**
1. Procure o ícone da 🐳 baleia na barra de tarefas (canto inferior direito)
2. Se não estiver lá, abra o Docker Desktop pelo Menu Iniciar
3. Aguarde o ícone aparecer e parar de animar (pode levar 1-2 minutos)
4. Execute o script do JAVOS novamente

### "Este aplicativo não pode ser executado no seu PC"

**Causa:** Tentando executar um arquivo incompatível com sua versão do Windows.

**Solução:** Use os arquivos `.bat` ou `.ps1` em vez de arquivos `.exe`.

### Janela do terminal fecha imediatamente ao executar o .bat

**Causa:** Ocorreu um erro durante a execução.

**Solução:**
1. Abra o Prompt de Comando (cmd):
   - Pressione `Windows + R`
   - Digite `cmd` e pressione Enter
2. Navegue até a pasta `deploy`:
   ```cmd
   cd C:\caminho\para\javos\deploy
   ```
3. Execute o script diretamente para ver as mensagens de erro:
   ```cmd
   install.bat
   ```

### PowerShell não executa os scripts (.ps1)

**Causa:** A política de execução do PowerShell está bloqueando scripts.

**Solução:**
1. Abra o **PowerShell como Administrador**
2. Execute:
   ```powershell
   Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```
3. Responda `S` (Sim) quando perguntado
4. Tente executar o script novamente

### A aplicação não abre no navegador

**Verifique:**

1. Se o Docker Desktop está rodando (ícone da baleia na barra de tarefas)
2. Se a aplicação foi iniciada: execute `start.bat` novamente
3. Os logs para erros: execute `logs.bat`
4. Se a porta 8080 não está em uso:
   ```cmd
   netstat -ano | findstr :8080
   ```

Se a porta estiver em uso, mude no arquivo `.env`:
```
APP_PORT=8090
```

Depois pare e inicie novamente.

### Antivírus bloqueando os scripts

**Sintoma:** Aviso do Windows Defender ou outro antivírus ao tentar executar os scripts.

**Solução:**
1. Os scripts do JAVOS são seguros. Você pode adicionar a pasta `deploy` como exceção no antivírus.
2. No **Windows Defender**: Configurações → Privacidade e Segurança → Segurança do Windows → Proteção contra vírus e ameaças → Gerenciar configurações → Exclusões → Adicionar ou remover exclusões

### Windows Defender SmartScreen bloqueou o arquivo

**Solução:**
1. Clique em **"Mais informações"** na janela de aviso
2. Clique em **"Executar mesmo assim"**

Ou clique com o botão direito no arquivo → Propriedades → marque **"Desbloquear"** → OK.

### Acesso lento à aplicação

**Causa comum:** O Docker Desktop está usando muita memória.

**Solução:**
1. Abra o Docker Desktop
2. Vá em **Settings** (ícone de engrenagem) → **Resources**
3. Ajuste os limites de memória (recomendado: pelo menos 2 GB para o JAVOS)

### Erro "Hyper-V is not available"

**Causa:** O WSL 2 é necessário mas não está disponível em versões antigas do Windows.

**Solução:**
- Certifique-se de ter o Windows 10 versão 2004 ou superior
- Execute as atualizações do Windows
- Siga o guia de instalação do WSL 2: [https://aka.ms/wsl2](https://aka.ms/wsl2)

---

## Perguntas Frequentes

**Preciso ser administrador do computador para instalar?**

Para instalar o Docker Desktop, sim. Depois de instalado, você pode usar o JAVOS sem privilégios de administrador.

**O JAVOS funciona no Windows 7 ou 8?**

Não. O Docker Desktop requer Windows 10 (versão 2004 ou superior) ou Windows 11. Atualize para uma versão mais recente.

**Como outros computadores da rede acessam o JAVOS?**

1. Descubra o IP local do seu computador:
   - Pressione `Windows + R`, digite `cmd` e Enter
   - Execute: `ipconfig`
   - Procure por "Endereço IPv4"
2. Outros computadores na mesma rede podem acessar em: `http://SEU-IP:8080`
3. Certifique-se de que o **Firewall do Windows** não está bloqueando a porta 8080

**Como liberar a porta 8080 no Firewall do Windows?**

1. Abra o **Painel de Controle** → Sistema e Segurança → Firewall do Windows Defender
2. Clique em **"Configurações avançadas"**
3. Em **"Regras de entrada"**, clique em **"Nova Regra..."**
4. Selecione **"Porta"** → **TCP** → porta **8080**
5. Selecione **"Permitir a conexão"**
6. Marque todos os perfis de rede
7. Dê um nome como "JAVOS" e clique em Concluir

**O JAVOS inicia automaticamente quando ligo o computador?**

O Docker Desktop pode ser configurado para iniciar com o Windows. Vá em Docker Desktop → Settings → General → marque **"Start Docker Desktop when you log in"**. Os containers do JAVOS estão configurados com `restart: unless-stopped`, então iniciarão automaticamente junto com o Docker.

**Como atualizar para uma nova versão?**

1. Faça um backup: `backup.bat`
2. Pare a aplicação: `stop.bat`
3. Substitua os arquivos da pasta `deploy` pelos da nova versão
4. Execute a instalação novamente: `install.bat`
5. Inicie: `start.bat`

Seus dados serão mantidos.

**Esqueci minha senha. Como recupero?**

Atualmente, a única forma de recuperar o acesso é:

1. Faça um backup dos dados: `backup.bat`
2. Execute o reset: `reset.bat`  
   ⚠️ Isso apaga **todos os dados**!
3. Acesse novamente com usuário `admin` / senha `admin123`

---

## Comandos de Referência Rápida

| O que fazer | Arquivo .bat | PowerShell |
|-------------|-------------|------------|
| Instalar (primeira vez) | `install.bat` | `.\install.ps1` |
| Iniciar | `start.bat` | `.\start.ps1` |
| Parar | `stop.bat` | `.\stop.ps1` |
| Ver logs | `logs.bat` | `.\logs.ps1` |
| Fazer backup | `backup.bat` | `.\backup.ps1` |
| Restaurar backup | `restore.bat <arquivo>` | `.\restore.ps1 <arquivo>` |
| Resetar tudo | `reset.bat` | `.\reset.ps1` |

---

## Precisa de Ajuda?

- 📋 Veja os logs da aplicação: execute `logs.bat`
- 🐛 Reporte problemas em: [https://github.com/cesarsuchoj/javos/issues](https://github.com/cesarsuchoj/javos/issues)
- 📖 Documentação completa: [`/docs/README.md`](README.md)
- 🍎 Guia para macOS: [`/docs/README-macos.md`](README-macos.md)
- 🐧 Guia para Linux: [`/docs/README-linux.md`](README-linux.md)
