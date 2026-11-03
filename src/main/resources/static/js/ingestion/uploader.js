// Selector de archivos: validación previa en el cliente, carga y retroalimentación.
import { api } from '../core/apiClient.js';
import { el } from '../core/dom.js';

const ALLOWED_EXTENSIONS = ['csv', 'xlsx'];
const MAX_BYTES = 5 * 1024 * 1024;

export function initUploader({ dropzone, input, feedback, onUploaded }) {
  const hint = dropzone.querySelector('#dropzone-hint');
  const defaultHint = hint.textContent;

  function preValidate(file) {
    const extension = file.name.split('.').pop().toLowerCase();
    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      return `El archivo "${file.name}" no es válido. Solo se aceptan archivos .csv o .xlsx.`;
    }
    if (file.size === 0) return 'El archivo está vacío.';
    if (file.size > MAX_BYTES) return 'El archivo supera el tamaño máximo permitido (5 MB).';
    return null;
  }

  function setBusy(busy, fileName) {
    dropzone.classList.toggle('is-busy', busy);
    hint.textContent = busy ? `Procesando ${fileName}...` : defaultHint;
  }

  async function handle(file) {
    if (!file) return;
    const problem = preValidate(file);
    if (problem) {
      renderError(feedback, { message: problem, details: [] });
      return;
    }

    setBusy(true, file.name);
    try {
      const result = await api.uploadFile(file);
      renderResult(feedback, result);
      await onUploaded(result);
    } catch (error) {
      renderError(feedback, error);
    } finally {
      setBusy(false);
      input.value = '';
    }
  }

  input.addEventListener('change', () => handle(input.files[0]));

  ['dragenter', 'dragover'].forEach((type) => dropzone.addEventListener(type, (event) => {
    event.preventDefault();
    dropzone.classList.add('is-dragover');
  }));
  ['dragleave', 'drop'].forEach((type) => dropzone.addEventListener(type, (event) => {
    event.preventDefault();
    dropzone.classList.remove('is-dragover');
  }));
  dropzone.addEventListener('drop', (event) => handle(event.dataTransfer.files[0]));
}

function renderResult(container, result) {
  const hasErrors = result.errors.length > 0;
  const box = el('div', { class: `alert ${hasErrors ? 'alert-warning' : 'alert-success'}` },
    el('strong', {}, `Archivo "${result.fileName}" procesado.`),
    ` Se cargaron ${result.validRows} de ${result.totalRows} registros.`,
    hasErrors ? ` ${result.invalidRows} fila(s) fueron descartadas por errores de validación.` : '');

  if (hasErrors) {
    box.append(el('details', {},
      el('summary', {}, `Ver ${result.errors.length} error(es) de validación`),
      el('div', { class: 'error-rows' },
        el('ul', {}, result.errors.map((e) => el('li', {}, `Fila ${e.row} · ${e.column}: ${e.message}`))))));
  }
  container.replaceChildren(box);
}

function renderError(container, error) {
  const box = el('div', { class: 'alert alert-error', role: 'alert' }, el('strong', {}, error.message));
  if (error.details?.length) {
    box.append(el('div', { class: 'error-rows' }, el('ul', {}, error.details.map((d) => el('li', {}, d)))));
  }
  container.replaceChildren(box);
}
