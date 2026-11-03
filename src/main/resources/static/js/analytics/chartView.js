// Gráficas con Chart.js: distribución por estado (dona) y por ubicación (barras).
const STATUS_COLORS = { Aprobado: '#16a34a', Rechazado: '#dc2626', Pendiente: '#d97706' };
const FALLBACK = ['#0ea5a4', '#38bdf8', '#6366f1', '#a855f7', '#f43f5e'];

export function createCharts({ statusCanvas, locationCanvas }) {
  const charts = {};
  const available = typeof window.Chart !== 'undefined';

  function replace(key, canvas, config) {
    charts[key]?.destroy();
    charts[key] = new window.Chart(canvas, config);
  }

  return {
    render(report) {
      if (!available) {
        [statusCanvas, locationCanvas].forEach((c) => {
          c.parentElement.textContent = 'No se pudo cargar la librería de gráficas (sin conexión a la CDN).';
        });
        return;
      }

      replace('status', statusCanvas, {
        type: 'doughnut',
        data: {
          labels: report.byStatus.map((c) => c.label),
          datasets: [{
            data: report.byStatus.map((c) => c.count),
            backgroundColor: report.byStatus.map((c, i) => STATUS_COLORS[c.label] ?? FALLBACK[i % FALLBACK.length]),
            borderWidth: 2,
          }],
        },
        options: {
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'bottom' },
            tooltip: {
              callbacks: {
                label: (ctx) => ` ${ctx.label}: ${ctx.parsed} (${report.byStatus[ctx.dataIndex].percentage}%)`,
              },
            },
          },
        },
      });

      replace('location', locationCanvas, {
        type: 'bar',
        data: {
          labels: report.byLocation.map((c) => c.label),
          datasets: [{
            label: 'Inspecciones',
            data: report.byLocation.map((c) => c.count),
            backgroundColor: '#0ea5a4',
            borderRadius: 6,
            maxBarThickness: 36,
          }],
        },
        options: {
          indexAxis: 'y',
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: { x: { beginAtZero: true, ticks: { precision: 0 } } },
        },
      });
    },
  };
}
