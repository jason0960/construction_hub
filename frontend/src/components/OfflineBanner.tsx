import { useOnlineStatus } from '../hooks/useOnlineStatus';
import { useSyncExternalStore } from 'react';
import { subscribeSyncStatus } from '../lib/syncManager';

// Shim for useSyncExternalStore — subscribeSyncStatus sends (pending, syncing)
let lastPending = 0;
let lastSyncing = false;
let cachedSnapshot = { pending: 0, syncing: false };

function subscribeSyncWrapper(onStoreChange: () => void) {
  return subscribeSyncStatus((pending, syncing) => {
    if (pending !== lastPending || syncing !== lastSyncing) {
      lastPending = pending;
      lastSyncing = syncing;
      cachedSnapshot = { pending, syncing };
      onStoreChange();
    }
  });
}

function getSyncSnapshot() {
  return cachedSnapshot;
}

function useSyncInfo() {
  return useSyncExternalStore(subscribeSyncWrapper, getSyncSnapshot, () => ({
    pending: 0,
    syncing: false,
  }));
}

/**
 * Floating banner that shows offline status and pending sync count.
 * Appears at the bottom of the screen.
 */
export default function OfflineBanner() {
  const isOnline = useOnlineStatus();
  const { pending, syncing } = useSyncInfo();

  // Nothing to show when online with no pending mutations
  if (isOnline && pending === 0 && !syncing) return null;

  return (
    <div className="fixed bottom-0 inset-x-0 z-50 flex justify-center pb-4 pointer-events-none">
      <div className="pointer-events-auto rounded-xl shadow-lg border px-4 py-2.5 flex items-center gap-3 text-sm font-medium max-w-sm mx-4
        bg-white border-slate-200">
        {!isOnline ? (
          <>
            <span className="flex h-2.5 w-2.5 relative">
              <span className="absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75 animate-ping" />
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-amber-500" />
            </span>
            <span className="text-slate-700">
              You're offline
              {pending > 0 && (
                <span className="text-slate-500 font-normal">
                  {' '}&middot; {pending} change{pending !== 1 ? 's' : ''} saved locally
                </span>
              )}
            </span>
          </>
        ) : syncing ? (
          <>
            <svg className="animate-spin h-4 w-4 text-primary-600" viewBox="0 0 24 24" fill="none">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
            <span className="text-slate-700">Syncing {pending} change{pending !== 1 ? 's' : ''}...</span>
          </>
        ) : pending > 0 ? (
          <>
            <span className="flex h-2.5 w-2.5 relative">
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-primary-500" />
            </span>
            <span className="text-slate-700">
              {pending} change{pending !== 1 ? 's' : ''} pending sync
            </span>
          </>
        ) : null}
      </div>
    </div>
  );
}
