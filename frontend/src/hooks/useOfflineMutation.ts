import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useCallback } from 'react';
import { queueMutation, type QueuedMutation } from '../lib/offlineStorage';
import { processMutationQueue } from '../lib/syncManager';

interface OfflineMutationOptions<TData, TVariables> {
  /** The API call to make when online */
  mutationFn: (variables: TVariables) => Promise<TData>;
  /** Build the offline queue entry from the mutation variables */
  offlineEntry: (variables: TVariables) => {
    endpoint: string;
    method: QueuedMutation['method'];
    data?: unknown;
    description: string;
  };
  /** Query keys to invalidate after successful mutation */
  queryKeysToInvalidate: string[][];
  /** Standard react-query onSuccess */
  onSuccess?: () => void;
  /** Called when mutation is queued for offline sync */
  onQueued?: () => void;
}

/**
 * A mutation hook that works offline:
 * - If online: executes normally via mutationFn
 * - If offline: queues in IndexedDB and syncs when back online
 */
export function useOfflineMutation<TData = unknown, TVariables = void>(
  options: OfflineMutationOptions<TData, TVariables>
) {
  const queryClient = useQueryClient();

  const queueOffline = useCallback(
    async (variables: TVariables) => {
      const entry = options.offlineEntry(variables);
      await queueMutation({
        ...entry,
        queryKeysToInvalidate: options.queryKeysToInvalidate,
      });
      options.onQueued?.();
    },
    [options]
  );

  const mutation = useMutation<TData, Error, TVariables>({
    mutationFn: async (variables) => {
      if (!navigator.onLine) {
        await queueOffline(variables);
        // Return a synthetic response so the UI can proceed
        return undefined as unknown as TData;
      }
      return options.mutationFn(variables);
    },
    onSuccess: () => {
      options.onSuccess?.();
      // After a successful online mutation, also check if there's queued stuff
      if (navigator.onLine) {
        processMutationQueue(queryClient);
      }
    },
    onError: async (error, variables) => {
      // Network error — queue it offline
      if (!navigator.onLine || isNetworkError(error)) {
        try {
          await queueOffline(variables);
        } catch (queueError) {
          console.error('Failed to queue offline mutation:', queueError);
        }
      }
    },
  });

  return {
    ...mutation,
    /** Whether the last call was queued offline instead of sent */
    isOffline: !navigator.onLine,
  };
}

function isNetworkError(error: unknown): boolean {
  if (error instanceof Error) {
    return error.message === 'Network Error' || error.message.includes('ERR_NETWORK');
  }
  return false;
}
