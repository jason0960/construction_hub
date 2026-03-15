import { get, set, del, keys } from 'idb-keyval';
import type { PersistedClient, Persister } from '@tanstack/react-query-persist-client';

// ── React Query Cache Persister ──

/**
 * IDB-backed persister for React Query cache.
 * Stores the entire dehydrated query cache in IndexedDB.
 */
const QUERY_CACHE_KEY = 'ch-query-cache';

export const idbPersister: Persister = {
  persistClient: async (client: PersistedClient) => {
    await set(QUERY_CACHE_KEY, client);
  },
  restoreClient: async (): Promise<PersistedClient | undefined> => {
    return await get<PersistedClient>(QUERY_CACHE_KEY);
  },
  removeClient: async () => {
    await del(QUERY_CACHE_KEY);
  },
};

// ── Offline Mutation Queue ──

export interface QueuedMutation {
  id: string;
  timestamp: number;
  endpoint: string; // e.g. '/jobs/5/notes'
  method: 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  data?: unknown;
  description: string; // human-readable, e.g. "Add note to job #5"
  queryKeysToInvalidate: string[][]; // keys to invalidate on success
  status: 'pending' | 'syncing' | 'failed';
  error?: string;
  retries: number;
}

const MUTATION_QUEUE_PREFIX = 'ch-mutation-';

function mutationKey(id: string) {
  return `${MUTATION_QUEUE_PREFIX}${id}`;
}

export async function queueMutation(mutation: Omit<QueuedMutation, 'id' | 'timestamp' | 'status' | 'retries'>): Promise<string> {
  const id = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const entry: QueuedMutation = {
    ...mutation,
    id,
    timestamp: Date.now(),
    status: 'pending',
    retries: 0,
  };
  await set(mutationKey(id), entry);
  return id;
}

export async function getPendingMutations(): Promise<QueuedMutation[]> {
  const allKeys = await keys();
  const mutationKeys = allKeys.filter(
    (k) => typeof k === 'string' && k.startsWith(MUTATION_QUEUE_PREFIX)
  );
  const mutations: QueuedMutation[] = [];
  for (const key of mutationKeys) {
    const m = await get<QueuedMutation>(key);
    if (m) mutations.push(m);
  }
  // Sort oldest first for FIFO processing
  return mutations.sort((a, b) => a.timestamp - b.timestamp);
}

export async function updateMutationStatus(
  id: string,
  status: QueuedMutation['status'],
  error?: string
): Promise<void> {
  const m = await get<QueuedMutation>(mutationKey(id));
  if (m) {
    m.status = status;
    m.error = error;
    if (status === 'syncing') m.retries++;
    await set(mutationKey(id), m);
  }
}

export async function removeMutation(id: string): Promise<void> {
  await del(mutationKey(id));
}

export async function clearAllMutations(): Promise<void> {
  const allKeys = await keys();
  for (const key of allKeys) {
    if (typeof key === 'string' && key.startsWith(MUTATION_QUEUE_PREFIX)) {
      await del(key);
    }
  }
}
