// Cliente HTTP: agrega el token, normaliza errores (ApiError) y maneja la sesión expirada.
import { session } from './session.js';

export class ApiError extends Error {
  constructor(status, code, message, details = []) {
    super(message);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

async function request(path, { method = 'GET', json, formData, auth = true } = {}) {
  const headers = { Accept: 'application/json' };
  if (json !== undefined) headers['Content-Type'] = 'application/json';
  if (auth) {
    const token = session.token();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(path, {
      method,
      headers,
      body: json !== undefined ? JSON.stringify(json) : formData,
    });
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR', 'No fue posible conectar con el servidor. Verifique su conexión.');
  }

  if (response.status === 204) return null;
  const body = await response.json().catch(() => null);

  if (!response.ok) {
    if (response.status === 401 && auth) {
      session.clear();
      window.location.replace('/login?expired=1');
    }
    throw new ApiError(
      response.status,
      body?.code ?? `HTTP_${response.status}`,
      body?.message ?? 'Ocurrió un error inesperado.',
      body?.details ?? [],
    );
  }
  return body;
}

export const api = {
  login: (username, password) =>
    request('/api/auth/login', { method: 'POST', json: { username, password }, auth: false }),

  logout: () => request('/api/auth/logout', { method: 'POST' }),

  uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    return request('/api/inspections/upload', { method: 'POST', formData });
  },

  inspections: () => request('/api/inspections'),

  analytics: () => request('/api/analytics'),
};
