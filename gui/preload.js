// ============================================================
// Javos GUI - Preload Script (Ponte segura entre Main e Renderer)
// ============================================================
'use strict';

const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('javosAPI', {
  // Status dos serviços
  getStatus: () => ipcRenderer.invoke('get-status'),

  // Controle dos serviços
  serviceStart: () => ipcRenderer.invoke('service-start'),
  serviceStop: () => ipcRenderer.invoke('service-stop'),
  serviceRestart: () => ipcRenderer.invoke('service-restart'),

  // Logs
  logsStart: () => ipcRenderer.invoke('logs-start'),
  logsStop: () => ipcRenderer.invoke('logs-stop'),
  logsSave: (content) => ipcRenderer.invoke('logs-save', content),
  onLogData: (callback) => ipcRenderer.on('log-data', (_event, data) => callback(data)),
  onLogStopped: (callback) => ipcRenderer.on('log-stopped', () => callback()),
  onScriptOutput: (callback) => ipcRenderer.on('script-output', (_event, data) => callback(data)),
  removeLogListeners: () => {
    ipcRenderer.removeAllListeners('log-data');
    ipcRenderer.removeAllListeners('log-stopped');
    ipcRenderer.removeAllListeners('script-output');
  },

  // Configurações
  configRead: () => ipcRenderer.invoke('config-read'),
  configSave: (vars) => ipcRenderer.invoke('config-save', vars),
  envExists: () => ipcRenderer.invoke('env-exists'),

  // Backup e Restore
  backupRun: () => ipcRenderer.invoke('backup-run'),
  restoreRun: () => ipcRenderer.invoke('restore-run'),

  // Utilitários
  openDeployDir: () => ipcRenderer.invoke('open-deploy-dir'),
  systemInfo: () => ipcRenderer.invoke('system-info'),

  // Configurações avançadas
  configGetDefaults: () => ipcRenderer.invoke('config-get-defaults'),
  dbTestConnection: (params) => ipcRenderer.invoke('db-test-connection', params),
});
