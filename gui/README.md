# Javos GUI — Interface Gráfica de Gerenciamento do Deploy

Interface desktop multiplataforma para gerenciar o deploy do **Javos**, construída com [Electron.js](https://www.electronjs.org/).

## Funcionalidades

- **Dashboard** — Visualize o status dos containers Docker em tempo real, com indicadores visuais de ativo/parado
- **Controle dos Serviços** — Inicie, pare e reinicie os serviços com um clique
- **Logs em Tempo Real** — Acompanhe os logs do Docker Compose com colorização e opção de salvar em arquivo
- **Configurações** — Edite o arquivo `.env` de forma visual e segura, sem precisar de editor de texto
- **Backup & Restore** — Crie e restaure backups dos dados com seleção de pasta/arquivo via diálogo nativo

## Pré-requisitos

- [Node.js](https://nodejs.org/) v18 ou superior
- [Docker](https://www.docker.com/) e Docker Compose instalados
- Os scripts de deploy presentes na pasta `../deploy/`

## Instalação

```bash
# A partir da pasta gui/
cd gui
npm install
```

## Execução

```bash
# Iniciar a interface gráfica
npm start
```

## Distribuição (Empacotamento)

Para gerar um executável distribuível para sua plataforma:

```bash
# Windows
npm run build:win

# macOS
npm run build:mac

# Linux
npm run build:linux
```

Os arquivos gerados estarão na pasta `dist/`.

## Estrutura

```
gui/
├── main.js          # Processo principal do Electron (Node.js)
├── preload.js       # Ponte segura entre processo principal e renderer
├── renderer/
│   ├── index.html   # Interface principal
│   ├── styles.css   # Estilos (tema escuro moderno)
│   └── renderer.js  # Lógica da interface
└── package.json
```

## Integração com os Scripts de Deploy

A interface detecta automaticamente o sistema operacional e chama os scripts corretos:

| Ação      | Linux          | macOS              | Windows       |
|-----------|----------------|--------------------|---------------|
| Iniciar   | `start.sh`     | `start-macos.sh`   | `start.bat`   |
| Parar     | `stop.sh`      | `stop-macos.sh`    | `stop.bat`    |
| Backup    | `backup.sh`    | `backup-macos.sh`  | `backup.bat`  |
| Restaurar | `restore.sh`   | `restore-macos.sh` | `restore.bat` |

## Notas de Segurança

- A comunicação entre o processo principal (Node.js) e a interface usa `contextIsolation: true` e `contextBridge`, seguindo as melhores práticas de segurança do Electron
- Nenhuma credencial é armazenada pela interface — apenas o arquivo `.env` na pasta `deploy/`
