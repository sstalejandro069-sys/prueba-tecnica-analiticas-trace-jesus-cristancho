// Punto de entrada del panel: conecta el estado con los componentes de cada funcionalidad.
import { api } from '../core/apiClient.js';
import { session } from '../core/session.js';
import { createStore } from '../core/store.js';
import { el } from '../core/dom.js';
import { initUploader } from '../ingestion/uploader.js';
import { createDataTable } from '../table/dataTable.js';
import { createCharts } from '../analytics/chartView.js';
import { renderKpis, renderSummary } from '../analytics/summaryView.js';

const current = session.require();
if (current) {
  start(current);
}

function start(user) {
  const $ = (id) => document.getElementById(id);
  $('current-user').textContent = user.username;

  const store = createStore({ records: [], analytics: null });
  const table = createDataTable($('data-table'), $('table-count'));
  const charts = createCharts({ statusCanvas: $('chart-status'), locationCanvas: $('chart-location') });

  store.subscribe((state, previous) => {
    const hasData = state.records.length > 0 && state.analytics?.totalRecords > 0;
    $('empty-state').classList.toggle('hidden', hasData);
    $('results').classList.toggle('hidden', !hasData);
    if (!hasData) return;

    if (state.records !== previous.records) table.setRecords(state.records);
    if (state.analytics !== previous.analytics) {
      renderKpis($('kpis'), state.analytics);
      renderSummary($('summary'), state.analytics);
      charts.render(state.analytics);
    }
  });

  async function refresh() {
    const [records, analytics] = await Promise.all([api.inspections(), api.analytics()]);
    store.setState({ records, analytics });
  }

  initUploader({
    dropzone: $('dropzone'),
    input: $('file-input'),
    feedback: $('upload-feedback'),
    onUploaded: refresh,
  });

  $('logout-btn').addEventListener('click', async () => {
    try {
      await api.logout();
    } finally {
      session.clear();
      window.location.replace('/');
    }
  });

  // Recupera la última carga del servidor (persistencia en memoria).
  refresh().catch((error) => {
    $('upload-feedback').replaceChildren(el('div', { class: 'alert alert-error', role: 'alert' }, error.message));
  });
}
