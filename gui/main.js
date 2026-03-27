// ============================================================
// Javos GUI - Processo Principal (Main Process)
// ============================================================
'use strict';

const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const { spawn, exec } = require('child_process');
const os = require('os');

// ---- Caminhos ----
const DEPLOY_DIR = path.join(__dirname, '..', 'deploy');
const ENV_FILE = path.join(DEPLOY_DIR, '.env');
const ENV_EXAMPLE = path.join(DEPLOY_DIR, '.env.example');

// ---- Detecção de plataforma ----
const PLATFORM = process.platform; // 'win32' | 'darwin' | 'linux'

// ---- Processo de log em execução ----
const LOG_TAIL_LINES = 200;
let logProcess = null;
let mainWindow = null;

// ---- Utilitários de script ----
function getScriptPath(name) {
  if (PLATFORM === 'win32') {
    return path.join(DEPLOY_DIR, `${name}.bat`);
  } else if (PLATFORM === 'darwin') {
    const macScript = path.join(DEPLOY_DIR, `${name}-macos.sh`);
    return fs.existsSync(macScript) ? macScript : path.join(DEPLOY_DIR, `${name}.sh`);
  } else {
    return path.join(DEPLOY_DIR, `${name}.sh`);
  }
}

function runScript(scriptName, args = []) {
  return new Promise((resolve, reject) => {
    const scriptPath = getScriptPath(scriptName);

    if (!fs.existsSync(scriptPath)) {
      return reject(new Error(`Script não encontrado: ${scriptPath}`));
    }

    let cmd, cmdArgs;
    if (PLATFORM === 'win32') {
      cmd = 'cmd.exe';
      cmdArgs = ['/c', scriptPath, ...args];
    } else {
      cmd = '/bin/bash';
      cmdArgs = [scriptPath, ...args];
    }

    const child = spawn(cmd, cmdArgs, {
      cwd: DEPLOY_DIR,
      env: { ...process.env },
    });

    let stdout = '';
    let stderr = '';

    child.stdout.on('data', (data) => {
      stdout += data.toString();
      if (mainWindow) {
        mainWindow.webContents.send('script-output', data.toString());
      }
    });

    child.stderr.on('data', (data) => {
      stderr += data.toString();
      if (mainWindow) {
        mainWindow.webContents.send('script-output', data.toString());
      }
    });

    child.on('close', (code) => {
      if (code === 0) {
        resolve({ stdout, stderr, code });
      } else {
        reject(new Error(stderr || `Processo encerrado com código ${code}`));
      }
    });

    child.on('error', (err) => {
      reject(err);
    });
  });
}

// ---- Status dos serviços via Docker ----
function getServiceStatus() {
  return new Promise((resolve) => {
    exec(
      'docker ps --filter "name=javos" --format "{{.Names}}\\t{{.Status}}\\t{{.Ports}}"',
      { timeout: 10000 },
      (err, stdout) => {
        if (err) {
          resolve([]);
          return;
        }
        const lines = stdout.trim().split('\n').filter(Boolean);
        const services = lines.map((line) => {
          const parts = line.split('\t');
          const name = parts[0] || '';
          const status = parts[1] || '';
          const ports = parts[2] || '';
          return {
            name,
            status,
            ports,
            running: status.toLowerCase().startsWith('up'),
          };
        });
        resolve(services);
      }
    );
  });
}

// ---- Streaming de logs ----
function startLogStream(win) {
  stopLogStream();

  let cmd, args;
  if (PLATFORM === 'win32') {
    const logsScript = getScriptPath('logs');
    cmd = 'cmd.exe';
    args = ['/c', logsScript];
  } else {
    // Usa docker compose diretamente para melhor controle
    cmd = 'sh';
    args = ['-c', `docker compose logs --follow --tail=${LOG_TAIL_LINES}`];
  }

  logProcess = spawn(cmd, args, {
    cwd: DEPLOY_DIR,
    env: { ...process.env },
  });

  logProcess.stdout.on('data', (data) => {
    if (win && !win.isDestroyed()) {
      win.webContents.send('log-data', data.toString());
    }
  });

  logProcess.stderr.on('data', (data) => {
    if (win && !win.isDestroyed()) {
      win.webContents.send('log-data', data.toString());
    }
  });

  logProcess.on('close', () => {
    logProcess = null;
    if (win && !win.isDestroyed()) {
      win.webContents.send('log-stopped');
    }
  });

  logProcess.on('error', () => {
    logProcess = null;
  });
}

function stopLogStream() {
  if (logProcess) {
    logProcess.kill('SIGTERM');
    logProcess = null;
  }
}

// ---- Leitura do .env ----
function readEnvFile() {
  const file = fs.existsSync(ENV_FILE) ? ENV_FILE : ENV_EXAMPLE;
  if (!fs.existsSync(file)) return {};

  const content = fs.readFileSync(file, 'utf8');
  const result = {};
  const lines = content.split('\n');

  for (const line of lines) {
    const trimmed = line.trim();
    if (trimmed.startsWith('#') || !trimmed.includes('=')) continue;
    const eqIdx = trimmed.indexOf('=');
    const key = trimmed.substring(0, eqIdx).trim();
    const value = trimmed.substring(eqIdx + 1).trim();
    if (key) result[key] = value;
  }
  return result;
}

function writeEnvFile(vars) {
  const example = fs.existsSync(ENV_EXAMPLE)
    ? fs.readFileSync(ENV_EXAMPLE, 'utf8')
    : '';

  // Preserva comentários do .env.example e substitui/acrescenta valores
  const commentLines = example
    .split('\n')
    .filter((l) => l.trim().startsWith('#') || l.trim() === '');

  const lines = [];
  const usedKeys = new Set();

  // Reconstrói com base no exemplo preservando comentários
  if (example) {
    for (const line of example.split('\n')) {
      const trimmed = line.trim();
      if (trimmed.startsWith('#') || trimmed === '') {
        lines.push(line);
      } else if (trimmed.includes('=')) {
        const key = trimmed.substring(0, trimmed.indexOf('=')).trim();
        if (key in vars) {
          lines.push(`${key}=${vars[key]}`);
          usedKeys.add(key);
        } else {
          lines.push(line);
          usedKeys.add(key);
        }
      }
    }
  }

  // Adiciona chaves que não estavam no exemplo
  for (const [key, value] of Object.entries(vars)) {
    if (!usedKeys.has(key)) {
      lines.push(`${key}=${value}`);
    }
  }

  fs.writeFileSync(ENV_FILE, lines.join('\n'), 'utf8');
}

// ---- Criação da janela principal ----
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1100,
    height: 720,
    minWidth: 800,
    minHeight: 600,
    title: 'Javos — Gerenciamento de Deploy',
    backgroundColor: '#1a1d23',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
    },
    show: false,
  });

  mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));

  mainWindow.once('ready-to-show', () => {
    mainWindow.show();
  });

  mainWindow.on('closed', () => {
    stopLogStream();
    mainWindow = null;
  });
}

// ---- IPC Handlers ----

// Status dos serviços
ipcMain.handle('get-status', async () => {
  return getServiceStatus();
});

// Iniciar
ipcMain.handle('service-start', async () => {
  try {
    await runScript('start');
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
});

// Parar
ipcMain.handle('service-stop', async () => {
  try {
    await runScript('stop');
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
});

// Reiniciar (stop + start)
ipcMain.handle('service-restart', async () => {
  try {
    await runScript('stop');
    await runScript('start');
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
});

// Iniciar streaming de logs
ipcMain.handle('logs-start', () => {
  if (mainWindow) startLogStream(mainWindow);
  return { ok: true };
});

// Parar streaming de logs
ipcMain.handle('logs-stop', () => {
  stopLogStream();
  return { ok: true };
});

// Salvar logs em arquivo
ipcMain.handle('logs-save', async (_event, content) => {
  const result = await dialog.showSaveDialog(mainWindow, {
    title: 'Salvar Logs',
    defaultPath: `javos-logs-${new Date().toISOString().substring(0, 10)}.txt`,
    filters: [
      { name: 'Arquivos de Texto', extensions: ['txt'] },
      { name: 'Todos os Arquivos', extensions: ['*'] },
    ],
  });
  if (result.canceled || !result.filePath) return { ok: false };
  fs.writeFileSync(result.filePath, content, 'utf8');
  return { ok: true, path: result.filePath };
});

// Ler configurações
ipcMain.handle('config-read', () => {
  return readEnvFile();
});

// Salvar configurações
ipcMain.handle('config-save', (_event, vars) => {
  try {
    writeEnvFile(vars);
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
});

// Verificar se .env existe
ipcMain.handle('env-exists', () => {
  return fs.existsSync(ENV_FILE);
});

// Backup
ipcMain.handle('backup-run', async () => {
  // Abre diálogo para escolher pasta destino
  const result = await dialog.showOpenDialog(mainWindow, {
    title: 'Escolher pasta para backup',
    properties: ['openDirectory'],
  });
  if (result.canceled || result.filePaths.length === 0) return { ok: false, canceled: true };
  const destDir = result.filePaths[0];
  try {
    await runScript('backup', [destDir]);
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
});

// Restore
ipcMain.handle('restore-run', async () => {
  const result = await dialog.showOpenDialog(mainWindow, {
    title: 'Selecionar arquivo de backup',
    filters: [
      { name: 'Backup Javos', extensions: ['tar.gz', 'gz'] },
      { name: 'Todos os Arquivos', extensions: ['*'] },
    ],
    properties: ['openFile'],
  });
  if (result.canceled || result.filePaths.length === 0) return { ok: false, canceled: true };
  const backupFile = result.filePaths[0];
  try {
    await runScript('restore', [backupFile]);
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
});

// Abrir pasta de deploy no explorador
ipcMain.handle('open-deploy-dir', () => {
  shell.openPath(DEPLOY_DIR);
  return { ok: true };
});

// Ler valores padrão do .env.example
ipcMain.handle('config-get-defaults', () => {
  if (!fs.existsSync(ENV_EXAMPLE)) return {};
  const content = fs.readFileSync(ENV_EXAMPLE, 'utf8');
  const result = {};
  for (const line of content.split('\n')) {
    const trimmed = line.trim();
    if (trimmed.startsWith('#') || !trimmed.includes('=')) continue;
    const eqIdx = trimmed.indexOf('=');
    const key = trimmed.substring(0, eqIdx).trim();
    const value = trimmed.substring(eqIdx + 1).trim();
    if (key) result[key] = value;
  }
  return result;
});

// Testar conexão com o banco de dados
ipcMain.handle('db-test-connection', async (_event, { type, host, port }) => {
  if (type === 'sqlite') {
    return { ok: true, message: 'Configuração SQLite válida (arquivo local, sem conexão de rede necessária).' };
  }

  const net = require('net');
  return new Promise((resolve) => {
    const socket = new net.Socket();
    const portNum = parseInt(port, 10);

    if (isNaN(portNum) || portNum < 1 || portNum > 65535) {
      resolve({ ok: false, message: 'Número de porta inválido.' });
      return;
    }

    socket.setTimeout(5000);

    socket.on('connect', () => {
      socket.destroy();
      resolve({ ok: true, message: `Porta ${portNum} acessível em "${host}".` });
    });

    socket.on('timeout', () => {
      socket.destroy();
      resolve({ ok: false, message: `Timeout: não foi possível conectar em "${host}:${portNum}" (5 s).` });
    });

    socket.on('error', (err) => {
      resolve({ ok: false, message: `Erro de conexão: ${err.message}` });
    });

    try {
      socket.connect(portNum, host);
    } catch (err) {
      resolve({ ok: false, message: `Erro: ${err.message}` });
    }
  });
});

// Informações do sistema
ipcMain.handle('system-info', () => {
  return {
    platform: PLATFORM,
    arch: os.arch(),
    hostname: os.hostname(),
    deployDir: DEPLOY_DIR,
    envExists: fs.existsSync(ENV_FILE),
  };
});

// ---- Ciclo de vida do app ----
app.whenReady().then(() => {
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  stopLogStream();
  if (PLATFORM !== 'darwin') app.quit();
});
