// Gestión de la sesión del navegador (token Bearer emitido por /api/auth/login).
const KEY = 'trace.session';

function read() {
  try {
    return JSON.parse(sessionStorage.getItem(KEY));
  } catch {
    return null;
  }
}

export const session = {
  save({ token, username, expiresAt }) {
    sessionStorage.setItem(KEY, JSON.stringify({ token, username, expiresAt }));
  },

  clear() {
    sessionStorage.removeItem(KEY);
  },

  current() {
    const data = read();
    if (!data?.token) return null;
    if (data.expiresAt && new Date(data.expiresAt) <= new Date()) {
      this.clear();
      return null;
    }
    return data;
  },

  token() {
    return this.current()?.token ?? null;
  },

  /** Redirige al login si no hay sesión activa. */
  require() {
    const current = this.current();
    if (!current) {
      window.location.replace('/login');
      return null;
    }
    return current;
  },
};
