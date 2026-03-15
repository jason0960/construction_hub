import type { QueryClient } from '@tanstack/react-query';
import api from '../api/client';
import { logger } from '../utils/logger';
import {
  getPendingMutations,
  updateMutationStatus,
  removeMutation,
  type QueuedMutation,
} from './offlineStorage';

type SyncListener = (pending: number, syncing: boolean) => void;

const syncListeners = new Set<SyncListener>();
let isSyncing = false;
let pendingCount = 0;

export function subscribeSyncStatus(listener: SyncListener) {
  syncListeners.add(listener);
  // Fire immediately with current state
  listener(pendingCount, isSyncing);
  return () => {
    syncListeners.delete(listener);
  };
}

function notifyListeners() {
  syncListeners.forEach((l) => l(pendingCount, isSyncing));
}

async function refreshPendingCount() {
  const mutations = await getPendingMutations();
  pendingCount = mutations.filter((m) => m.status !== 'failed').length;
  notifyListeners();
}

/**
 * Process the offline mutation queue: replay each pending mutation
 * against the server, then invalidate relevant query cache keys.
 */
export async function processMutationQueue(queryClient: QueryClient): Promise<{
  succeeded: number;
  failed: number;
}> {
  if (isSyncing) return { succeeded: 0, failed: 0 };
  if (!navigator.onLine) return { succeeded: 0, failed: 0 };

  isSyncing = true;
  notifyListeners();

  const mutations = await getPendingMutations();
  const pending = mutations.filter((m) => m.status === 'pending' || m.status === 'failed');

  logger.info('Sync', 'Starting offline queue sync', { pendingCount: pending.length });

  let succeeded = 0;
  let failed = 0;

  for (const mutation of pending) {
    if (!navigator.onLine) break; // Stop if we lose connection mid-sync

    try {
      await updateMutationStatus(mutation.id, 'syncing');
      notifyListeners();

      await replayMutation(mutation);

      // Success — remove from queue and invalidate queries
      await removeMutation(mutation.id);
      succeeded++;
      logger.debug('Sync', 'Mutation replayed successfully', { id: mutation.id, method: mutation.method, endpoint: mutation.endpoint });

      for (const keyParts of mutation.queryKeysToInvalidate) {
        queryClient.invalidateQueries({ queryKey: keyParts });
      }
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Unknown error';

      // If it's a 4xx client error, don't retry — mark as permanently failed
      if (isClientError(err)) {
        await updateMutationStatus(mutation.id, 'failed', errorMsg);
        failed++;
        logger.warn('Sync', 'Mutation permanently failed (client error)', { id: mutation.id, error: errorMsg });
      } else if (mutation.retries >= 3) {
        await updateMutationStatus(mutation.id, 'failed', `Max retries exceeded: ${errorMsg}`);
        failed++;
        logger.warn('Sync', 'Mutation permanently failed (max retries)', { id: mutation.id, retries: mutation.retries });
      } else {
        // Server error or network blip — leave as pending for next sync
        await updateMutationStatus(mutation.id, 'pending', errorMsg);
      }
    }
  }

  isSyncing = false;
  await refreshPendingCount();

  logger.info('Sync', 'Sync complete', { succeeded, failed });
  return { succeeded, failed };
}

async function replayMutation(mutation: QueuedMutation): Promise<void> {
  switch (mutation.method) {
    case 'POST':
      await api.post(mutation.endpoint, mutation.data);
      break;
    case 'PUT':
      await api.put(mutation.endpoint, mutation.data);
      break;
    case 'PATCH':
      await api.patch(mutation.endpoint, mutation.data);
      break;
    case 'DELETE':
      await api.delete(mutation.endpoint);
      break;
  }
}

function isClientError(err: unknown): boolean {
  if (err && typeof err === 'object' && 'response' in err) {
    const response = (err as { response?: { status?: number } }).response;
    if (response?.status && response.status >= 400 && response.status < 500) {
      return true;
    }
  }
  return false;
}

/**
 * Initialize sync manager: listen for online events and auto-sync.
 * Returns a cleanup function to remove event listeners.
 */
export function initSyncManager(queryClient: QueryClient): () => void {
  // Sync when coming back online
  const onOnline = () => {
    setTimeout(() => processMutationQueue(queryClient), 1000); // 1s debounce
  };
  window.addEventListener('online', onOnline);

  // Initial count
  refreshPendingCount();

  // Initial sync if we have pending mutations and are online
  if (navigator.onLine) {
    processMutationQueue(queryClient);
  }

  return () => {
    window.removeEventListener('online', onOnline);
  };
}
