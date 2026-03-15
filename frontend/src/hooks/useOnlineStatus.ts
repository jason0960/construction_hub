import { useSyncExternalStore, useCallback } from 'react';

// ── Online / Offline detection ──

type Listener = () => void;
const listeners = new Set<Listener>();

function subscribe(listener: Listener) {
  listeners.add(listener);
  window.addEventListener('online', listener);
  window.addEventListener('offline', listener);
  return () => {
    listeners.delete(listener);
    window.removeEventListener('online', listener);
    window.removeEventListener('offline', listener);
  };
}

function getSnapshot() {
  return navigator.onLine;
}

function getServerSnapshot() {
  return true; // SSR always assumes online
}

/**
 * Reactive hook: returns true when the browser has network connectivity.
 */
export function useOnlineStatus(): boolean {
  return useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
}

/**
 * Hook that returns a function to check if we're online
 * (useful in mutation callbacks without re-render dependency).
 */
export function useIsOnline() {
  const isOnline = useOnlineStatus();
  const checkOnline = useCallback(() => navigator.onLine, []);
  return { isOnline, checkOnline };
}
