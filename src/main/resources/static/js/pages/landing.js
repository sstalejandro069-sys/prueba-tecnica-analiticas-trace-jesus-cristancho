import { session } from '../core/session.js';

// Si ya hay sesión activa, los accesos llevan directo al panel.
if (session.current()) {
  document.querySelectorAll('[data-session-link]').forEach((link) => {
    link.href = '/dashboard';
    link.textContent = link.classList.contains('btn-lg') ? 'Ir al panel →' : 'Ir al panel';
  });
}
