// Estado de la aplicación con patrón publicador/suscriptor.
export function createStore(initialState) {
  let state = { ...initialState };
  const listeners = new Set();

  return {
    getState: () => state,

    setState(partial) {
      const previous = state;
      state = { ...state, ...partial };
      listeners.forEach((listener) => listener(state, previous));
    },

    subscribe(listener) {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
  };
}
