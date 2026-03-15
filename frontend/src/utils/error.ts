import axios from 'axios';

/**
 * Extract a user-friendly error message from an API error response.
 */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;
    if (data && typeof data === 'object' && 'message' in data && typeof data.message === 'string') {
      return data.message;
    }
    if (error.response?.status === 401) return 'Session expired. Please log in again.';
    if (error.response?.status === 403) return 'You do not have permission to perform this action.';
    if (error.response?.status === 404) return 'The requested resource was not found.';
    if (error.response?.status === 409) return 'A conflict occurred. The resource may have been modified.';
    if (error.message) return error.message;
  }

  if (error instanceof Error) return error.message;
  if (typeof error === 'string') return error;

  return 'An unexpected error occurred.';
}
