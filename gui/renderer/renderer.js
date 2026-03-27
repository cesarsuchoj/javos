/* ============================================================
   Javos GUI — Renderer Process
   ============================================================ */
'use strict';

const api = window.javosAPI;

// ---- Estado global ----
const state = {
  logLines: [],
  logStreaming: false,
  activeTab: 'dashboard',
  scriptRunning: false,   // true while a backup/restore script is capturing output
};

// ============================================================
// UTILITÁRIOS
// ============================================================

function toast(msg, type = 'info', duration = 4000) {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = `toast toast-${type} show`;
  clearTimeout(el._timer);
  el._timer = setTimeout(() => {
    el.classList.remove('show');
  }, duration);
}

function setLoading(btnId, loading, text) {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  btn.disabled = loading;
  if (text) {
    btn.dataset.originalText = btn.dataset.originalText || btn.innerHTML;
    if (loading) {
      btn.innerHTML = `<span class="spinner"></span> ${text}`;
    } else {
      btn.innerHTML = btn.dataset.originalText;
      delete btn.dataset.originalText;
    }
  }
}

function formatStatus(statusStr) {
  if (!statusStr) return 'Desconhecido';
  if (statusStr.toLowerCase().startsWith('up')) {
    return statusStr.replace(/^up /i, 'Em execução há ');
  }
  return statusStr;
}

// ============================================================
// TABS
// ============================================================

document.querySelectorAll('.tab-btn').forEach((btn) => {
  btn.addEventListener('click', () => {
    const tab = btn.dataset.tab;
    document.querySelectorAll('.tab-btn').forEach((b) => b.classList.remove('active'));
    document.querySelectorAll('.tab-panel').forEach((p) => p.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById(`tab-${tab}`).classList.add('active');
    state.activeTab = tab;

    if (tab === 'dashboard') refreshStatus();
    if (tab === 'config') loadConfig();
  });
});

// ============================================================
// DASHBOARD — Status
// ============================================================

async function refreshStatus() {
  const list = document.getElementById('servicesList');
  const noServices = document.getElementById('noServices');
  const headerDot = document.getElementById('headerDot');
  const headerStatusText = document.getElementById('headerStatusText');

  list.innerHTML = '<div class="loading-row"><span class="spinner"></span> Verificando containers...</div>';
  noServices.style.display = 'none';

  try {
    const services = await api.getStatus();

    if (services.length === 0) {
      list.innerHTML = '';
      noServices.style.display = 'block';
      headerDot.className = 'status-dot dot-down';
      headerStatusText.textContent = 'Parado';
      return;
    }

    const allUp = services.every((s) => s.running);
    headerDot.className = `status-dot ${allUp ? 'dot-up' : 'dot-down'}`;
    headerStatusText.textContent = allUp ? 'Em execução' : 'Parcialmente parado';

    list.innerHTML = services
      .map((s) => {
        const badgeClass = s.running ? 'badge-up' : 'badge-down';
        const badgeText = s.running ? 'Ativo' : 'Parado';
        const ports = s.ports
          ? `<span class="service-ports" title="${s.ports}">${s.ports}</span>`
          : '';
        return `
          <div class="service-row">
            <span class="status-dot ${s.running ? 'dot-up' : 'dot-down'}"></span>
            <span class="service-name">${escapeHtml(s.name)}</span>
            <span class="service-status-badge ${badgeClass}">${badgeText}</span>
            ${ports}
            <small style="color:var(--text-muted);font-size:11px;margin-left:4px">
              ${escapeHtml(formatStatus(s.status))}
            </small>
          </div>`;
      })
      .join('');
  } catch {
    list.innerHTML = '';
    noServices.style.display = 'block';
    headerDot.className = 'status-dot dot-unknown';
    headerStatusText.textContent = 'Erro ao verificar';
  }
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// Auto-refresh a cada 15 segundos
setInterval(() => {
  if (state.activeTab === 'dashboard') refreshStatus();
}, 15000);

// ============================================================
// DASHBOARD — Info do Sistema
// ============================================================

async function loadSystemInfo() {
  const el = document.getElementById('systemInfo');
  try {
    const info = await api.systemInfo();
    const platformNames = { win32: 'Windows', darwin: 'macOS', linux: 'Linux' };
    const items = [
      { label: 'Plataforma', value: platformNames[info.platform] || info.platform },
      { label: 'Arquitetura', value: info.arch },
      { label: 'Hostname', value: info.hostname },
      {
        label: 'Arquivo .env',
        value: info.envExists
          ? '<span style="color:var(--accent-green)">✔ Configurado</span>'
          : '<span style="color:var(--accent-yellow)">⚠ Não encontrado</span>',
      },
      { label: 'Pasta Deploy', value: info.deployDir },
    ];
    el.innerHTML = items
      .map(
        (i) => `
        <div class="info-item">
          <div class="info-label">${i.label}</div>
          <div class="info-value">${i.value}</div>
        </div>`
      )
      .join('');
  } catch {
    el.innerHTML = '<div class="loading-row" style="color:var(--text-muted)">Não foi possível carregar.</div>';
  }
}

// ============================================================
// DASHBOARD — Controles
// ============================================================

function appendScriptOutput(text) {
  const wrap = document.getElementById('scriptOutputWrap');
  const out = document.getElementById('scriptOutput');
  wrap.style.display = 'block';
  out.textContent += text;
  out.scrollTop = out.scrollHeight;
}

document.getElementById('btnClearOutput').addEventListener('click', () => {
  document.getElementById('scriptOutput').textContent = '';
  document.getElementById('scriptOutputWrap').style.display = 'none';
});

// Single persistent script-output listener — routes to the correct target based on state
api.onScriptOutput((data) => {
  if (state.scriptRunning) {
    // Backup / restore panels handle their own output via dedicated listeners
    return;
  }
  appendScriptOutput(data);
});

document.getElementById('btnStart').addEventListener('click', async () => {
  document.getElementById('scriptOutput').textContent = '';
  setLoading('btnStart', true, 'Iniciando...');
  try {
    const res = await api.serviceStart();
    if (res.ok) {
      toast('✔ Serviços iniciados com sucesso!', 'success');
      await refreshStatus();
    } else {
      toast(`✖ Erro ao iniciar: ${res.error}`, 'error', 7000);
    }
  } catch (err) {
    toast(`✖ Erro inesperado: ${err.message}`, 'error', 7000);
  } finally {
    setLoading('btnStart', false);
  }
});

document.getElementById('btnStop').addEventListener('click', async () => {
  document.getElementById('scriptOutput').textContent = '';
  setLoading('btnStop', true, 'Parando...');
  try {
    const res = await api.serviceStop();
    if (res.ok) {
      toast('✔ Serviços parados com sucesso.', 'success');
      await refreshStatus();
    } else {
      toast(`✖ Erro ao parar: ${res.error}`, 'error', 7000);
    }
  } catch (err) {
    toast(`✖ Erro inesperado: ${err.message}`, 'error', 7000);
  } finally {
    setLoading('btnStop', false);
  }
});

document.getElementById('btnRestart').addEventListener('click', async () => {
  document.getElementById('scriptOutput').textContent = '';
  setLoading('btnRestart', true, 'Reiniciando...');
  try {
    const res = await api.serviceRestart();
    if (res.ok) {
      toast('✔ Serviços reiniciados com sucesso!', 'success');
      await refreshStatus();
    } else {
      toast(`✖ Erro ao reiniciar: ${res.error}`, 'error', 7000);
    }
  } catch (err) {
    toast(`✖ Erro inesperado: ${err.message}`, 'error', 7000);
  } finally {
    setLoading('btnRestart', false);
  }
});

document.getElementById('btnRefreshStatus').addEventListener('click', refreshStatus);

// ============================================================
// LOGS
// ============================================================

const logViewer = document.getElementById('logViewer');
const logDot = document.getElementById('logDot');
const logStatusText = document.getElementById('logStatusText');
const logLineCount = document.getElementById('logLineCount');
const chkAutoScroll = document.getElementById('chkAutoScroll');

function setLogStatus(streaming) {
  state.logStreaming = streaming;
  logDot.className = `status-dot ${streaming ? 'dot-streaming' : 'dot-unknown'}`;
  logStatusText.textContent = streaming ? 'Recebendo logs...' : 'Parado';
}

function appendLog(text) {
  const lines = text.split('\n');
  for (const line of lines) {
    if (!line) continue;
    const span = document.createElement('span');
    span.className = 'log-entry';

    // Coloriza por nível
    const lower = line.toLowerCase();
    if (lower.includes('error') || lower.includes('exception') || lower.includes('erro')) {
      span.classList.add('error');
    } else if (lower.includes('warn') || lower.includes('aviso')) {
      span.classList.add('warn');
    } else if (lower.includes('info')) {
      span.classList.add('info');
    }

    span.textContent = line + '\n';
    logViewer.appendChild(span);
    state.logLines.push(line);
  }

  logLineCount.textContent = `${state.logLines.length} linhas`;

  if (chkAutoScroll.checked) {
    logViewer.scrollTop = logViewer.scrollHeight;
  }
}

api.onLogData((data) => appendLog(data));
api.onLogStopped(() => setLogStatus(false));

document.getElementById('btnLogsStart').addEventListener('click', async () => {
  if (state.logStreaming) return;
  await api.logsStart();
  setLogStatus(true);
});

document.getElementById('btnLogsStop').addEventListener('click', async () => {
  await api.logsStop();
  setLogStatus(false);
});

document.getElementById('btnLogsClear').addEventListener('click', () => {
  logViewer.innerHTML = '';
  state.logLines = [];
  logLineCount.textContent = '0 linhas';
});

document.getElementById('btnLogsSave').addEventListener('click', async () => {
  if (state.logLines.length === 0) {
    toast('Nenhum log para salvar.', 'info');
    return;
  }
  const content = state.logLines.join('\n');
  const res = await api.logsSave(content);
  if (res.ok) {
    toast(`✔ Logs salvos em: ${res.path}`, 'success', 5000);
  } else if (!res.canceled) {
    toast('✖ Erro ao salvar logs.', 'error');
  }
});

// ============================================================
// CONFIGURAÇÕES
// ============================================================

// ---- Presets de banco de dados ----
const DB_PRESETS = {
  sqlite: {
    driver: 'org.sqlite.JDBC',
    platform: 'org.hibernate.community.dialect.SQLiteDialect',
    defaultPort: '',
    defaultUsername: '',
    buildUrl: (f) => `jdbc:sqlite:${f.filePath || './data/javos.db'}`,
  },
  mysql: {
    driver: 'com.mysql.cj.jdbc.Driver',
    platform: 'org.hibernate.dialect.MySQLDialect',
    defaultPort: '3306',
    defaultUsername: 'javos_user',
    buildUrl: (f) =>
      `jdbc:mysql://${f.host || 'localhost'}:${f.port || '3306'}/${f.dbname || 'javos'}` +
      '?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true',
  },
  postgresql: {
    driver: 'org.postgresql.Driver',
    platform: 'org.hibernate.dialect.PostgreSQLDialect',
    defaultPort: '5432',
    defaultUsername: 'javos_user',
    buildUrl: (f) =>
      `jdbc:postgresql://${f.host || 'localhost'}:${f.port || '5432'}/${f.dbname || 'javos'}`,
  },
  sqlserver: {
    driver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver',
    platform: 'org.hibernate.dialect.SQLServerDialect',
    defaultPort: '1433',
    defaultUsername: 'sa',
    buildUrl: (f) =>
      `jdbc:sqlserver://${f.host || 'localhost'}:${f.port || '1433'};databaseName=${f.dbname || 'javos'}`,
  },
};

function detectDbType(url) {
  if (!url) return 'sqlite';
  if (url.startsWith('jdbc:sqlite:')) return 'sqlite';
  if (url.startsWith('jdbc:mysql:')) return 'mysql';
  if (url.startsWith('jdbc:postgresql:')) return 'postgresql';
  if (url.startsWith('jdbc:sqlserver:')) return 'sqlserver';
  return 'sqlite';
}

function parseDbUrl(type, url) {
  if (!url) return {};
  try {
    if (type === 'sqlite') {
      const m = url.match(/^jdbc:sqlite:(.+)/);
      return { filePath: m ? m[1] : './data/javos.db' };
    }
    if (type === 'mysql') {
      const m = url.match(/^jdbc:mysql:\/\/([^:/]+):(\d+)\/([^?]+)/);
      return m ? { host: m[1], port: m[2], dbname: m[3] } : {};
    }
    if (type === 'postgresql') {
      const m = url.match(/^jdbc:postgresql:\/\/([^:/]+):(\d+)\/(.+)/);
      return m ? { host: m[1], port: m[2], dbname: m[3] } : {};
    }
    if (type === 'sqlserver') {
      const hm = url.match(/^jdbc:sqlserver:\/\/([^:;]+):(\d+)/);
      const dm = url.match(/databaseName=([^;]+)/i);
      return hm ? { host: hm[1], port: hm[2], dbname: dm ? dm[1] : 'javos' } : {};
    }
  } catch {}
  return {};
}

let currentDbType = 'sqlite';

function getDbFields() {
  if (currentDbType === 'sqlite') {
    return { filePath: document.getElementById('db_sqlite_path').value.trim() };
  }
  return {
    host: document.getElementById('db_host').value.trim(),
    port: document.getElementById('db_port').value.trim(),
    dbname: document.getElementById('db_dbname').value.trim(),
  };
}

function updateUrlPreview() {
  const preset = DB_PRESETS[currentDbType];
  if (!preset) return;
  document.getElementById('db_url_preview').value = preset.buildUrl(getDbFields());
}

function setDbType(type, fields) {
  if (!DB_PRESETS[type]) return;
  currentDbType = type;

  // Update selector cards
  document.querySelectorAll('.db-selector-card').forEach((card) => {
    const active = card.dataset.db === type;
    card.classList.toggle('active', active);
    card.setAttribute('aria-checked', String(active));
  });

  // Show / hide field groups
  const isSqlite = type === 'sqlite';
  document.getElementById('dbFieldsSqlite').style.display = isSqlite ? '' : 'none';
  document.getElementById('dbFieldsRemote').style.display = isSqlite ? 'none' : '';

  // Populate fields
  if (fields) {
    if (isSqlite) {
      document.getElementById('db_sqlite_path').value = fields.filePath || './data/javos.db';
    } else {
      document.getElementById('db_host').value = fields.host || 'localhost';
      document.getElementById('db_port').value = fields.port || DB_PRESETS[type].defaultPort;
      document.getElementById('db_dbname').value = fields.dbname || 'javos';
      if (fields.username !== undefined) {
        document.getElementById('db_username').value = fields.username;
      }
      if (fields.password !== undefined) {
        document.getElementById('db_password').value = fields.password;
      }
    }
  }

  updateUrlPreview();
  validateDbFields();
}

// ---- Validação inline ----
function setValidation(iconId, state) {
  const el = document.getElementById(iconId);
  if (!el) return;
  el.textContent = state === 'ok' ? '✔' : state === 'err' ? '✖' : '';
  el.className = `vi ${state}`;
}

function validatePort(value) {
  const n = parseInt(value, 10);
  return !isNaN(n) && n >= 1 && n <= 65535;
}

function validateDbFields() {
  let valid = true;

  if (currentDbType === 'sqlite') {
    const path = document.getElementById('db_sqlite_path').value.trim();
    if (!path) {
      setValidation('vi_sqlite_path', 'err');
      valid = false;
    } else {
      setValidation('vi_sqlite_path', 'ok');
    }
  } else {
    const host = document.getElementById('db_host').value.trim();
    const port = document.getElementById('db_port').value.trim();
    const dbname = document.getElementById('db_dbname').value.trim();

    if (!host) { setValidation('vi_db_host', 'err'); valid = false; }
    else { setValidation('vi_db_host', 'ok'); }

    if (!validatePort(port)) { setValidation('vi_db_port', 'err'); valid = false; }
    else { setValidation('vi_db_port', 'ok'); }

    if (!dbname) { setValidation('vi_db_dbname', 'err'); valid = false; }
    else { setValidation('vi_db_dbname', 'ok'); }
  }

  return valid;
}

function validateAppPort() {
  const val = document.getElementById('cfg_APP_PORT').value.trim();
  const ok = validatePort(val);
  setValidation('vi_app_port', ok ? 'ok' : 'err');
  return ok;
}

// ---- Seletor de banco ----
document.querySelectorAll('.db-selector-card').forEach((card) => {
  card.addEventListener('click', () => {
    const type = card.dataset.db;
    const preset = DB_PRESETS[type];
    if (!preset) return;
    const fields =
      type === 'sqlite'
        ? { filePath: './data/javos.db' }
        : {
            host: 'localhost',
            port: preset.defaultPort,
            dbname: 'javos',
            username: preset.defaultUsername,
            password: '',
          };
    setDbType(type, fields);
  });

  card.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      card.click();
    }
  });
});

// Live URL / validation update
['db_sqlite_path', 'db_host', 'db_port', 'db_dbname'].forEach((id) => {
  const el = document.getElementById(id);
  if (el) {
    el.addEventListener('input', () => {
      updateUrlPreview();
      validateDbFields();
    });
  }
});

document.getElementById('cfg_APP_PORT').addEventListener('input', validateAppPort);

// ---- Testar conexão ----
document.getElementById('btnTestConnection').addEventListener('click', async () => {
  const statusEl = document.getElementById('testConnectionStatus');
  const btn = document.getElementById('btnTestConnection');

  statusEl.textContent = '';
  statusEl.className = 'test-status';

  if (!validateDbFields()) {
    statusEl.textContent = '⚠ Corrija os erros antes de testar.';
    statusEl.className = 'test-status warn';
    return;
  }

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Testando...';

  try {
    const params = { type: currentDbType };
    if (currentDbType !== 'sqlite') {
      params.host = document.getElementById('db_host').value.trim();
      params.port = document.getElementById('db_port').value.trim();
    }
    const res = await api.dbTestConnection(params);
    statusEl.textContent = res.ok ? `✔ ${res.message}` : `✖ ${res.message}`;
    statusEl.className = `test-status ${res.ok ? 'ok' : 'err'}`;
  } catch (err) {
    statusEl.textContent = `✖ Erro: ${err.message}`;
    statusEl.className = 'test-status err';
  } finally {
    btn.disabled = false;
    btn.innerHTML = '🔌 Testar Conexão';
  }
});

// ---- Restaurar padrão ----
document.getElementById('btnRestoreDefaults').addEventListener('click', async () => {
  const defaults = await api.configGetDefaults();
  if (!defaults || Object.keys(defaults).length === 0) {
    toast('Arquivo .env.example não encontrado.', 'error');
    return;
  }

  if (defaults.APP_PORT !== undefined) {
    document.getElementById('cfg_APP_PORT').value = defaults.APP_PORT;
  }
  if (defaults.JWT_SECRET !== undefined) {
    document.getElementById('cfg_JWT_SECRET').value = defaults.JWT_SECRET;
  }

  const url = defaults.SPRING_DATASOURCE_URL || '';
  const dbType = detectDbType(url);
  const parsed = parseDbUrl(dbType, url);
  if (dbType !== 'sqlite') {
    parsed.username = defaults.SPRING_DATASOURCE_USERNAME || '';
    parsed.password = defaults.SPRING_DATASOURCE_PASSWORD || '';
  }
  setDbType(dbType, parsed);

  validateAppPort();
  toast('✔ Valores padrão restaurados. Salve para confirmar.', 'info');
});

document.getElementById('btnConfigReload').addEventListener('click', loadConfig);

document.getElementById('btnOpenDeployDir').addEventListener('click', () => {
  api.openDeployDir();
});

// Toggle JWT visibility
document.getElementById('btnToggleJwt').addEventListener('click', () => {
  const input = document.getElementById('cfg_JWT_SECRET');
  const btn = document.getElementById('btnToggleJwt');
  if (input.type === 'password') {
    input.type = 'text';
    btn.textContent = '🙈 Ocultar';
  } else {
    input.type = 'password';
    btn.textContent = '👁 Mostrar';
  }
});

async function loadConfig() {
  const notice = document.getElementById('configNotice');
  const envExists = await api.envExists();
  notice.style.display = envExists ? 'none' : 'flex';

  const vars = await api.configRead();

  if (vars.APP_PORT !== undefined) {
    document.getElementById('cfg_APP_PORT').value = vars.APP_PORT;
  }
  if (vars.JWT_SECRET !== undefined) {
    document.getElementById('cfg_JWT_SECRET').value = vars.JWT_SECRET;
  }

  const url = vars.SPRING_DATASOURCE_URL || '';
  const dbType = detectDbType(url);
  const parsed = parseDbUrl(dbType, url);
  if (dbType !== 'sqlite') {
    parsed.username = vars.SPRING_DATASOURCE_USERNAME || '';
    parsed.password = vars.SPRING_DATASOURCE_PASSWORD || '';
  }
  setDbType(dbType, parsed);

  validateAppPort();
}

document.getElementById('configForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const msgEl = document.getElementById('configSaveMsg');

  const portOk = validateAppPort();
  const dbOk = validateDbFields();
  if (!portOk || !dbOk) {
    msgEl.style.display = 'block';
    msgEl.className = 'config-save-msg error';
    msgEl.textContent = '✖ Corrija os erros de validação antes de salvar.';
    setTimeout(() => { msgEl.style.display = 'none'; }, 5000);
    return;
  }

  const preset = DB_PRESETS[currentDbType];
  const dbFields = getDbFields();

  const vars = {
    APP_PORT: document.getElementById('cfg_APP_PORT').value.trim(),
    JWT_SECRET: document.getElementById('cfg_JWT_SECRET').value,
    SPRING_DATASOURCE_URL: preset.buildUrl(dbFields),
    SPRING_DATASOURCE_DRIVER_CLASS_NAME: preset.driver,
    SPRING_DATASOURCE_USERNAME:
      currentDbType !== 'sqlite' ? document.getElementById('db_username').value : '',
    SPRING_DATASOURCE_PASSWORD:
      currentDbType !== 'sqlite' ? document.getElementById('db_password').value : '',
    SPRING_JPA_DATABASE_PLATFORM: preset.platform,
  };

  const res = await api.configSave(vars);
  msgEl.style.display = 'block';
  if (res.ok) {
    msgEl.className = 'config-save-msg success';
    msgEl.textContent = '✔ Configurações salvas com sucesso!';
    toast('✔ Configurações salvas!', 'success');
    document.getElementById('configNotice').style.display = 'none';
    document.getElementById('restartNotice').style.display = 'flex';
  } else {
    msgEl.className = 'config-save-msg error';
    msgEl.textContent = `✖ Erro ao salvar: ${res.error}`;
    toast('✖ Erro ao salvar configurações.', 'error');
  }

  setTimeout(() => { msgEl.style.display = 'none'; }, 6000);
});

// Reiniciar a partir do aviso de restart
document.getElementById('btnRestartNow').addEventListener('click', async () => {
  document.getElementById('restartNotice').style.display = 'none';
  setLoading('btnRestartNow', true, 'Reiniciando...');
  try {
    const res = await api.serviceRestart();
    if (res.ok) {
      toast('✔ Serviços reiniciados com sucesso!', 'success');
      await refreshStatus();
    } else {
      toast(`✖ Erro ao reiniciar: ${res.error}`, 'error', 7000);
      document.getElementById('restartNotice').style.display = 'flex';
    }
  } catch (err) {
    toast(`✖ Erro: ${err.message}`, 'error', 7000);
    document.getElementById('restartNotice').style.display = 'flex';
  } finally {
    setLoading('btnRestartNow', false);
  }
});

// ============================================================
// BACKUP & RESTORE
// ============================================================

document.getElementById('btnBackup').addEventListener('click', async () => {
  const wrap = document.getElementById('backupOutputWrap');
  const out = document.getElementById('backupOutput');
  wrap.style.display = 'none';
  out.textContent = '';

  state.scriptRunning = true;

  // Dedicated listener that appends output to the backup panel
  const handleOutput = (data) => {
    wrap.style.display = 'block';
    out.textContent += data;
    out.scrollTop = out.scrollHeight;
  };
  api.onScriptOutput(handleOutput);

  setLoading('btnBackup', true, 'Fazendo backup...');
  try {
    const res = await api.backupRun();
    if (res.ok) {
      toast('✔ Backup criado com sucesso!', 'success');
    } else if (!res.canceled) {
      toast(`✖ Erro no backup: ${res.error || 'Desconhecido'}`, 'error', 7000);
    }
  } catch (err) {
    toast(`✖ Erro inesperado: ${err.message}`, 'error', 7000);
  } finally {
    state.scriptRunning = false;
    setLoading('btnBackup', false);
  }
});

document.getElementById('btnRestore').addEventListener('click', async () => {
  const wrap = document.getElementById('restoreOutputWrap');
  const out = document.getElementById('restoreOutput');
  wrap.style.display = 'none';
  out.textContent = '';

  state.scriptRunning = true;

  const handleOutput = (data) => {
    wrap.style.display = 'block';
    out.textContent += data;
    out.scrollTop = out.scrollHeight;
  };
  api.onScriptOutput(handleOutput);

  setLoading('btnRestore', true, 'Restaurando...');
  try {
    const res = await api.restoreRun();
    if (res.ok) {
      toast('✔ Backup restaurado! Execute Iniciar para subir os serviços.', 'success', 6000);
    } else if (!res.canceled) {
      toast(`✖ Erro na restauração: ${res.error || 'Desconhecido'}`, 'error', 7000);
    }
  } catch (err) {
    toast(`✖ Erro inesperado: ${err.message}`, 'error', 7000);
  } finally {
    state.scriptRunning = false;
    setLoading('btnRestore', false);
  }
});

// ============================================================
// INICIALIZAÇÃO
// ============================================================

(async function init() {
  await refreshStatus();
  await loadSystemInfo();
})();
