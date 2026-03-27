# FAQ — Perguntas Frequentes sobre o JAVOS

Este documento reúne as dúvidas mais comuns sobre instalação, uso e manutenção do JAVOS.

---

## Índice

1. [Instalação](#instalação)
2. [Uso Diário](#uso-diário)
3. [Dados e Backup](#dados-e-backup)
4. [Acesso em Rede](#acesso-em-rede)
5. [Problemas Técnicos](#problemas-técnicos)
6. [Segurança](#segurança)
7. [Atualização](#atualização)

---

## Instalação

**Qual é a forma mais fácil de instalar o JAVOS?**

A forma mais simples é usando o Docker. Basta:
1. Instalar o Docker Desktop
2. Baixar os arquivos do JAVOS
3. Executar o script de instalação para o seu sistema

Consulte o guia do seu sistema operacional:
- 🪟 [Windows](README-windows.md)
- 🍎 [macOS](README-macos.md)
- 🐧 [Linux](README-linux.md)

---

**Preciso saber programar para usar o JAVOS?**

Não. O JAVOS foi projetado para ser usado por qualquer pessoa. Os scripts de instalação e operação foram criados para funcionar com um duplo clique (Windows) ou um único comando (Linux/macOS).

---

**Qual é a diferença entre o arquivo `.bat`, `.ps1` e `.sh`?**

| Arquivo | Sistema | Como usar |
|---------|---------|-----------|
| `.bat` | Windows | Duplo clique no Explorador de Arquivos |
| `.ps1` | Windows (PowerShell) | Abrir PowerShell e digitar `.\nome.ps1` |
| `.sh` | Linux e macOS | Abrir Terminal e digitar `./nome.sh` |
| `-macos.sh` | macOS (otimizado) | Abrir Terminal e digitar `./nome-macos.sh` |

---

**Por que preciso do Docker?**

O Docker cria um ambiente isolado e padronizado para o JAVOS rodar. Isso significa que:
- Você não precisa instalar Java, Maven ou banco de dados separadamente
- A aplicação funciona da mesma forma em qualquer computador
- É muito mais fácil fazer backup, restauração e atualização

---

**Posso instalar o JAVOS sem o Docker?**

Sim, para quem prefere instalar diretamente. Você precisará de:
- Java 17 ou superior
- Maven 3.8 ou superior
- Node.js 18 ou superior

Consulte a [documentação técnica](README.md) para instruções detalhadas.

---

**A instalação trava em "Downloading" por muito tempo. É normal?**

Sim! Na primeira instalação, o Docker precisa baixar a imagem base (~400 MB) e compilar o código. Dependendo da sua velocidade de internet e do computador, isso pode levar de **5 a 20 minutos**. Aguarde pacientemente.

---

## Uso Diário

**Como acesso o JAVOS depois de instalado?**

1. Execute o script de inicialização (`start.bat`, `./start.sh` ou `./start-macos.sh`)
2. Aguarde alguns segundos
3. Abra o navegador e acesse: [http://localhost:8080](http://localhost:8080)

---

**Qual é o usuário e senha padrão?**

- **Usuário:** `admin`
- **Senha:** `admin123`

⚠️ **Altere a senha imediatamente após o primeiro acesso!** Esta senha padrão é conhecida publicamente e representa um risco de segurança.

---

**Como altero minha senha?**

Após fazer login, acesse o menu de perfil do usuário no canto superior direito e escolha a opção de alterar senha.

---

**Preciso deixar o Docker aberto para usar o JAVOS?**

O Docker Desktop precisa estar em execução em segundo plano (você verá o ícone da baleia na barra de tarefas/menu). Mas você não precisa manter a janela principal do Docker Desktop aberta.

---

**O JAVOS é lento para carregar na primeira vez. Por quê?**

Na primeira vez que o container inicia, o sistema precisa:
- Inicializar o banco de dados
- Criar as tabelas e dados iniciais
- Carregar a aplicação Java (JVM)

Esse processo pode levar de 30 a 60 segundos. Nas próximas vezes, será muito mais rápido.

---

**Como faço para o JAVOS abrir automaticamente quando ligo o computador?**

Configure o Docker Desktop para iniciar com o sistema:
- **Windows/macOS:** Docker Desktop → Settings → General → "Start Docker Desktop when you log in"
- **Linux:** Execute `sudo systemctl enable docker`

Os containers do JAVOS já estão configurados para reiniciar automaticamente (`restart: unless-stopped`), então iniciarão junto com o Docker.

---

## Dados e Backup

**Onde os dados do JAVOS são armazenados?**

Os dados ficam em um **volume Docker** chamado `javos-data`, que é gerenciado automaticamente pelo Docker. O arquivo de banco de dados SQLite fica dentro desse volume, em `/app/data/javos.db`.

---

**Com que frequência devo fazer backup?**

Recomendamos:
- **Uso pessoal/pequeno:** semanalmente
- **Uso em empresa/intenso:** diariamente
- **Antes de qualquer atualização:** sempre

---

**Para onde vão os arquivos de backup?**

Por padrão, os backups ficam em `deploy/backups/` com o nome `javos-backup-YYYYMMDD_HHMMSS.tar.gz`. Você pode especificar uma pasta diferente:
- Linux/macOS: `./backup.sh /caminho/para/pasta`
- Windows: `.\backup.ps1 -BackupDir C:\caminho\para\pasta`

Recomendamos copiar os backups para um local externo (nuvem, disco externo) regularmente.

---

**Posso restaurar um backup em uma máquina diferente?**

Sim! Copie o arquivo `.tar.gz` do backup para a pasta `deploy/backups/` do novo computador e execute o script de restauração.

---

**O que acontece se eu executar o reset?**

O reset (`reset.bat`, `./reset.sh` ou `./reset-macos.sh`) apaga **todos os dados** do banco de dados e reinicia a aplicação com os dados padrão (usuário `admin`). Esta operação é **irreversível** sem um backup.

---

## Acesso em Rede

**Como faço para outros computadores acessarem o JAVOS?**

1. Descubra o endereço IP do computador onde o JAVOS está instalado:
   - Windows: `ipconfig` no Prompt de Comando
   - Linux: `ip addr show` ou `hostname -I` no Terminal
   - macOS: `ipconfig getifaddr en0` no Terminal
2. No firewall, libere a porta 8080
3. Outros computadores na mesma rede acessam em: `http://IP-DO-SERVIDOR:8080`

---

**Posso acessar o JAVOS pela internet (de fora da minha rede)?**

O JAVOS foi projetado para uso em rede local. Para acesso externo, seria necessário configurar roteamento de portas no seu roteador (port forwarding). **Atenção:** expor o JAVOS à internet sem configurações adequadas de segurança representa riscos. Para uso profissional, consulte um profissional de TI.

---

**Quantos usuários podem usar o JAVOS simultaneamente?**

O JAVOS com SQLite funciona bem para até 5-10 usuários simultâneos. Para equipes maiores, recomendamos configurar o MySQL como banco de dados. Consulte a [documentação técnica](README.md#como-trocar-de-sqlite-para-mysql).

---

## Problemas Técnicos

**A aplicação não abre. O que faço?**

1. Verifique se o Docker está rodando (ícone da baleia visível)
2. Execute o script de início novamente
3. Aguarde 60 segundos (primeira inicialização é mais lenta)
4. Verifique os logs: `logs.bat` / `./logs.sh` / `./logs-macos.sh`
5. Veja o guia de troubleshooting do seu sistema operacional

---

**Esqueci minha senha. Como recupero?**

Atualmente, a única forma de recuperar o acesso é executar o reset:

1. Faça um backup dos dados: `backup.bat` / `./backup.sh`
2. Execute o reset: `reset.bat` / `./reset.sh`
   > ⚠️ Isso apagará **todos os dados**!
3. Acesse com usuário `admin` / senha `admin123`
4. Restaure o backup se necessário: `restore.bat <arquivo>` / `./restore.sh <arquivo>`

---

**A porta 8080 já está em uso. O que faço?**

Mude a porta do JAVOS editando o arquivo `.env` na pasta `deploy`:

```
APP_PORT=8090
```

Depois pare e inicie a aplicação novamente. O JAVOS estará disponível em `http://localhost:8090`.

---

**O Docker usa muito espaço no disco. Como libero?**

```bash
# Ver quanto espaço o Docker usa
docker system df

# Limpar imagens, containers e volumes não utilizados
docker system prune -a

# ATENÇÃO: não execute o comando abaixo se não tiver backup,
# pois apagará os volumes de dados:
# docker system prune -a --volumes
```

---

## Segurança

**O JAVOS é seguro para uso empresarial?**

O JAVOS usa autenticação JWT, HTTPS pode ser configurado e o banco de dados fica isolado no container Docker. Para uso empresarial, recomendamos:
1. Alterar a senha padrão imediatamente
2. Trocar o `JWT_SECRET` no arquivo `.env` por um valor longo e aleatório
3. Usar MySQL em vez de SQLite para dados críticos
4. Fazer backups regulares
5. Considerar o uso de HTTPS com certificado SSL

---

**O que é o `JWT_SECRET` no arquivo `.env`?**

É uma chave secreta usada para assinar os tokens de autenticação. Se alguém tiver acesso a essa chave, poderá forjar tokens de acesso. **Nunca compartilhe o arquivo `.env` e troque o valor padrão em produção.**

---

**Meus dados ficam seguros no Docker?**

Sim. Os dados ficam em um volume Docker isolado no seu próprio computador. Eles não são enviados para a internet a menos que você configure explicitamente.

---

## Atualização

**Como atualizo para uma nova versão?**

1. Faça um backup dos dados atuais
2. Pare a aplicação
3. Substitua os arquivos da pasta `deploy` pelos da nova versão
4. Execute o instalador novamente
5. Inicie a aplicação

Seus dados serão mantidos, pois ficam em um volume Docker separado do código.

---

**Vou perder meus dados ao atualizar?**

Não, desde que você **não execute** o `reset` durante a atualização. Os dados ficam em um volume Docker separado e são preservados durante atualizações normais.

---

**Como sei se há uma nova versão disponível?**

Acompanhe as releases em: [https://github.com/cesarsuchoj/javos/releases](https://github.com/cesarsuchoj/javos/releases)

---

## Ainda tem dúvidas?

- 📖 Consulte a [documentação técnica completa](README.md)
- 🐛 Abra um chamado em: [https://github.com/cesarsuchoj/javos/issues](https://github.com/cesarsuchoj/javos/issues)
- 🪟 [Guia Windows](README-windows.md)
- 🍎 [Guia macOS](README-macos.md)
- 🐧 [Guia Linux](README-linux.md)
