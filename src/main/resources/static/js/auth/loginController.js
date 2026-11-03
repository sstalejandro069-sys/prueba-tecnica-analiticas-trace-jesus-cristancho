import { api } from '../core/apiClient.js';
import { session } from '../core/session.js';

const form = document.getElementById('login-form');
const alertBox = document.getElementById('login-alert');
const submit = document.getElementById('login-submit');
const fields = {
  username: document.getElementById('username'),
  password: document.getElementById('password'),
};

if (session.current()) {
  window.location.replace('/dashboard');
}

if (new URLSearchParams(window.location.search).has('expired')) {
  showAlert('Su sesión expiró o no es válida. Ingrese de nuevo.', 'alert-warning');
}

function showAlert(message, kind = 'alert-error') {
  alertBox.className = `alert ${kind}`;
  alertBox.textContent = message;
}

function setFieldError(name, message) {
  fields[name].setAttribute('aria-invalid', message ? 'true' : 'false');
  document.getElementById(`${name}-error`).textContent = message ?? '';
}

function validate() {
  let valid = true;
  for (const [name, input] of Object.entries(fields)) {
    const empty = !input.value.trim();
    setFieldError(name, empty ? 'Este campo es obligatorio.' : null);
    valid &&= !empty;
  }
  return valid;
}

function setLoading(loading) {
  submit.disabled = loading;
  submit.replaceChildren(...(loading
    ? [Object.assign(document.createElement('span'), { className: 'spinner' }), 'Validando...']
    : ['Ingresar']));
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  alertBox.className = 'alert hidden';
  if (!validate()) return;

  setLoading(true);
  try {
    const result = await api.login(fields.username.value.trim(), fields.password.value);
    session.save(result);
    window.location.replace('/dashboard');
  } catch (error) {
    showAlert(error.message);
    fields.password.value = '';
    fields.password.focus();
    setLoading(false);
  }
});

Object.entries(fields).forEach(([name, input]) =>
  input.addEventListener('input', () => setFieldError(name, null)));
