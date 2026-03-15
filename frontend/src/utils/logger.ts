/**
 * Lightweight structured logger for the frontend.
 * In production builds (import.meta.env.PROD), debug logs are suppressed.
 * All log calls include a [tag] prefix for easy filtering in DevTools.
 */

const isDev = import.meta.env.DEV;

function formatArgs(tag: string, message: string, data?: Record<string, unknown>): string {
  const parts = [`[${tag}] ${message}`];
  if (data) {
    parts.push(
      Object.entries(data)
        .map(([k, v]) => `${k}=${typeof v === 'object' ? JSON.stringify(v) : v}`)
        .join(' ')
    );
  }
  return parts.join(' | ');
}

export const logger = {
  /** Debug logs — suppressed in production */
  debug(tag: string, message: string, data?: Record<string, unknown>) {
    if (isDev) {
      console.debug(formatArgs(tag, message, data));
    }
  },

  /** Informational logs — always emitted */
  info(tag: string, message: string, data?: Record<string, unknown>) {
    console.info(formatArgs(tag, message, data));
  },

  /** Warning logs — always emitted */
  warn(tag: string, message: string, data?: Record<string, unknown>) {
    console.warn(formatArgs(tag, message, data));
  },

  /** Error logs — always emitted, includes optional error object */
  error(tag: string, message: string, error?: unknown, data?: Record<string, unknown>) {
    console.error(formatArgs(tag, message, data), error || '');
  },
};
