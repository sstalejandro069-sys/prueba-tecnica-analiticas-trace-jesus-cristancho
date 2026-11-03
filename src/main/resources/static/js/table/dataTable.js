// Tabla interactiva: búsqueda, filtro por estado, orden por columna y paginación.
import { el, formatDate, statusBadge } from '../core/dom.js';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'date', label: 'Fecha', format: formatDate },
  { key: 'location', label: 'Ubicación' },
  { key: 'status', label: 'Estado', render: statusBadge },
  { key: 'inspector', label: 'Inspector' },
  { key: 'observations', label: 'Observaciones', wrap: true },
];
const PAGE_SIZE = 10;

export function createDataTable(container, counter) {
  const view = { records: [], query: '', status: '', sortKey: 'date', sortDir: 'asc', page: 1 };

  const search = el('input', {
    class: 'input', type: 'search', placeholder: 'Buscar por ID, ubicación, inspector...', 'aria-label': 'Buscar',
  });
  const statusFilter = el('select', { class: 'select', 'aria-label': 'Filtrar por estado' });
  const wrap = el('div', { class: 'table-wrap' });
  const footer = el('div', { class: 'table-footer' });
  container.replaceChildren(el('div', { class: 'table-toolbar' }, search, statusFilter), wrap, footer);

  search.addEventListener('input', () => update({ query: search.value.trim().toLowerCase(), page: 1 }));
  statusFilter.addEventListener('change', () => update({ status: statusFilter.value, page: 1 }));

  function update(partial) {
    Object.assign(view, partial);
    render();
  }

  function filtered() {
    const rows = view.records.filter((r) =>
      (!view.status || r.status === view.status) &&
      (!view.query || COLUMNS.some((c) => String(r[c.key] ?? '').toLowerCase().includes(view.query))));
    const dir = view.sortDir === 'asc' ? 1 : -1;
    return rows.sort((a, b) =>
      String(a[view.sortKey] ?? '').localeCompare(String(b[view.sortKey] ?? ''), 'es', { numeric: true }) * dir);
  }

  function header() {
    return el('thead', {}, el('tr', {}, COLUMNS.map((c) => {
      const active = view.sortKey === c.key;
      const arrow = active ? (view.sortDir === 'asc' ? '▲' : '▼') : '';
      return el('th', { 'aria-sort': active ? (view.sortDir === 'asc' ? 'ascending' : 'descending') : 'none' },
        el('button', {
          type: 'button',
          onClick: () => update({
            sortKey: c.key,
            sortDir: active && view.sortDir === 'asc' ? 'desc' : 'asc',
          }),
        }, c.label, arrow));
    })));
  }

  function render() {
    const rows = filtered();
    const pages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE));
    view.page = Math.min(view.page, pages);
    const pageRows = rows.slice((view.page - 1) * PAGE_SIZE, view.page * PAGE_SIZE);

    const body = el('tbody', {}, pageRows.length
      ? pageRows.map((r) => el('tr', {}, COLUMNS.map((c) => {
        const value = r[c.key];
        const content = c.render ? c.render(value) : (c.format ? c.format(value) : value ?? '—');
        return el('td', { class: c.wrap ? 'wrap' : undefined }, content);
      })))
      : el('tr', {}, el('td', { colspan: COLUMNS.length, class: 'muted' }, 'No hay registros que coincidan con el filtro.')));

    wrap.replaceChildren(el('table', {}, header(), body));

    const from = rows.length ? (view.page - 1) * PAGE_SIZE + 1 : 0;
    footer.replaceChildren(
      el('span', { class: 'muted' }, `Mostrando ${from}–${from + pageRows.length - (rows.length ? 1 : 0)} de ${rows.length}`),
      el('div', { class: 'pager' },
        el('button', { class: 'btn', type: 'button', disabled: view.page <= 1, onClick: () => update({ page: view.page - 1 }) }, '← Anterior'),
        el('button', { class: 'btn', type: 'button', disabled: view.page >= pages, onClick: () => update({ page: view.page + 1 }) }, 'Siguiente →')));
    counter.textContent = `${view.records.length} registros`;
  }

  return {
    setRecords(records) {
      view.records = [...records];
      const statuses = [...new Set(records.map((r) => r.status))].sort();
      statusFilter.replaceChildren(el('option', { value: '' }, 'Todos los estados'),
        statuses.map((s) => el('option', { value: s }, s)));
      statusFilter.value = statuses.includes(view.status) ? view.status : '';
      view.status = statusFilter.value;
      view.page = 1;
      render();
    },
  };
}
