// Indicadores clave y análisis descriptivo generado por el backend.
import { el, formatPercent } from '../core/dom.js';

function percentOf(report, label) {
  return report.byStatus.find((c) => c.label === label)?.percentage ?? 0;
}

export function renderKpis(container, report) {
  const kpi = (label, value, modifier) =>
    el('div', { class: 'card kpi' },
      el('div', { class: 'kpi-label' }, label),
      el('div', { class: `kpi-value ${modifier ?? ''}` }, value));

  container.replaceChildren(
    kpi('Registros procesados', report.totalRecords.toLocaleString('es-CO')),
    kpi('Aprobados', formatPercent(percentOf(report, 'Aprobado')), 'is-success'),
    kpi('Rechazados', formatPercent(percentOf(report, 'Rechazado')), 'is-danger'),
    kpi('Zonas inspeccionadas', report.byLocation.length));
}

export function renderSummary(container, report) {
  container.replaceChildren(
    el('p', { class: 'summary-lead' }, report.summary),
    el('ul', { class: 'insights' }, report.insights.map((text) =>
      el('li', { class: text.startsWith('Alerta') ? 'is-alert' : undefined }, text))));
}
