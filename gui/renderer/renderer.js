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

const CONFIG_FIELDS = [
  'APP_PORT',
  'JWT_SECRET',
  'SPRING_DATASOURCE_URL',
  'SPRING_DATASOURCE_DRIVER_CLASS_NAME',
  'SPRING_DATASOURCE_USERNAME',
  'SPRING_DATASOURCE_PASSWORD',
  'SPRING_JPA_DATABASE_PLATFORM',
];

async function loadConfig() {
  const notice = document.getElementById('configNotice');
  const envExists = await api.envExists();
  notice.style.display = envExists ? 'none' : 'flex';

  const vars = await api.configRead();
  for (const key of CONFIG_FIELDS) {
    const el = document.getElementById(`cfg_${key}`);
    if (el && key in vars) {
      el.value = vars[key];
    }
  }
}

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

document.getElementById('configForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const msgEl = document.getElementById('configSaveMsg');

  const vars = {};
  for (const key of CONFIG_FIELDS) {
    const el = document.getElementById(`cfg_${key}`);
    if (el) vars[key] = el.value;
  }

  const res = await api.configSave(vars);
  msgEl.style.display = 'block';
  if (res.ok) {
    msgEl.className = 'config-save-msg success';
    msgEl.textContent = '✔ Configurações salvas com sucesso! Reinicie os serviços para aplicar.';
    toast('✔ Configurações salvas!', 'success');
    // Atualiza notice (arquivo .env agora existe)
    document.getElementById('configNotice').style.display = 'none';
  } else {
    msgEl.className = 'config-save-msg error';
    msgEl.textContent = `✖ Erro ao salvar: ${res.error}`;
    toast('✖ Erro ao salvar configurações.', 'error');
  }

  setTimeout(() => {
    msgEl.style.display = 'none';
  }, 6000);
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
