import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workersApi } from '../api/workers';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import type { WorkerRequest, WorkerStatus } from '../types';

const WORKER_STATUSES: WorkerStatus[] = ['ACTIVE', 'INACTIVE'];

export default function WorkersPage() {
  const queryClient = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);

  const { data: workers = [], isLoading } = useQuery({
    queryKey: ['workers'],
    queryFn: workersApi.list,
  });

  const editWorker = workers.find((w) => w.id === editId);

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Workers</h1>
          <p className="text-sm text-slate-500 mt-0.5">{workers.length} crew members</p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="inline-flex items-center gap-1.5 bg-primary-700 hover:bg-primary-800 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
        >
          <PlusIcon className="w-4 h-4" />
          Add Worker
        </button>
      </div>

      {/* Workers list */}
      {isLoading ? (
        <div className="flex justify-center py-12">
          <p className="text-gray-500">Loading workers...</p>
        </div>
      ) : workers.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg border border-gray-200">
          <h3 className="text-sm font-medium text-gray-800">No workers yet</h3>
          <p className="text-sm text-gray-500 mt-1">Add your first crew member to get started.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {workers.map((w) => (
            <div
              key={w.id}
              className="bg-white rounded-xl border border-slate-200 p-4 hover:border-primary-300 transition-colors"
            >
              <div className="flex items-start justify-between mb-2">
                <div>
                  <h3 className="text-sm font-semibold text-slate-900">
                    {w.firstName} {w.lastName}
                  </h3>
                  {w.trade && <p className="text-xs text-slate-500">{w.trade}</p>}
                </div>
                <StatusBadge status={w.status} />
              </div>

              <div className="space-y-1 text-sm text-slate-600 mb-3">
                {w.email && <p className="truncate">{w.email}</p>}
                {w.phone && <p>{w.phone}</p>}
                {w.hourlyRate != null && (
                  <p className="font-medium">${w.hourlyRate.toFixed(2)}/hr</p>
                )}
              </div>

              {/* Current jobs */}
              {w.currentJobs?.length > 0 && (
                <div className="text-xs text-slate-400 mb-2">
                  {w.currentJobs.length} active job{w.currentJobs.length !== 1 ? 's' : ''}
                </div>
              )}

              <div className="flex items-center gap-2 pt-2 border-t border-slate-100">
                <button
                  onClick={() => setEditId(w.id)}
                  className="text-xs text-primary-600 hover:text-primary-700 font-medium"
                >
                  Edit
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Worker Modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Add Worker">
        <WorkerForm
          onSuccess={() => {
            setShowCreate(false);
            queryClient.invalidateQueries({ queryKey: ['workers'] });
          }}
        />
      </Modal>

      {/* Edit Worker Modal */}
      <Modal
        open={editId !== null}
        onClose={() => setEditId(null)}
        title="Edit Worker"
      >
        {editWorker && (
          <WorkerForm
            workerId={editWorker.id}
            initial={{
              firstName: editWorker.firstName,
              lastName: editWorker.lastName,
              email: editWorker.email || '',
              phone: editWorker.phone || '',
              trade: editWorker.trade || '',
              hourlyRate: editWorker.hourlyRate,
              status: editWorker.status,
            }}
            onSuccess={() => {
              setEditId(null);
              queryClient.invalidateQueries({ queryKey: ['workers'] });
            }}
          />
        )}
      </Modal>
    </div>
  );
}

// ── Worker Form ──
function WorkerForm({
  initial,
  workerId,
  onSuccess,
}: {
  initial?: Partial<WorkerRequest>;
  workerId?: number;
  onSuccess: () => void;
}) {
  const [form, setForm] = useState<WorkerRequest>({
    firstName: initial?.firstName || '',
    lastName: initial?.lastName || '',
    email: initial?.email || '',
    phone: initial?.phone || '',
    trade: initial?.trade || '',
    hourlyRate: initial?.hourlyRate ?? undefined,
    status: initial?.status || 'ACTIVE',
  });
  const [error, setError] = useState('');

  const [rateStr, setRateStr] = useState(
    initial?.hourlyRate != null ? String(initial.hourlyRate) : ''
  );

  const mutation = useMutation({
    mutationFn: (data: WorkerRequest) =>
      workerId ? workersApi.update(workerId, data) : workersApi.create(data),
    onSuccess,
    onError: (err: unknown) => {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Failed to save';
      setError(msg);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.mutate({ ...form, hourlyRate: rateStr ? Number(rateStr) : undefined });
  };

  const inputCls =
    'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500';
  const labelCls = 'block text-sm font-medium text-slate-700 mb-1';

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-3"
    >
      {error && (
        <div className="bg-danger-50 text-danger-700 text-sm rounded-lg px-4 py-3">{error}</div>
      )}
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className={labelCls}>First Name *</label>
          <input
            required
            className={inputCls}
            value={form.firstName}
            onChange={(e) => setForm({ ...form, firstName: e.target.value })}
          />
        </div>
        <div>
          <label className={labelCls}>Last Name *</label>
          <input
            required
            className={inputCls}
            value={form.lastName}
            onChange={(e) => setForm({ ...form, lastName: e.target.value })}
          />
        </div>
      </div>
      <div>
        <label className={labelCls}>Trade</label>
        <input
          className={inputCls}
          value={form.trade}
          onChange={(e) => setForm({ ...form, trade: e.target.value })}
          placeholder="Electrician, Plumber, Carpenter..."
        />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className={labelCls}>Email</label>
          <input
            type="email"
            className={inputCls}
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </div>
        <div>
          <label className={labelCls}>Phone</label>
          <input
            type="tel"
            className={inputCls}
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
          />
        </div>
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className={labelCls}>Hourly Rate</label>
          <input
            className={inputCls}
            value={rateStr}
            onChange={(e) => setRateStr(e.target.value)}
            placeholder="0.00"
          />
        </div>
        <div>
          <label className={labelCls}>Status</label>
          <select
            className={inputCls}
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value as WorkerStatus })}
          >
            {WORKER_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div className="flex justify-end pt-2">
        <button
          type="submit"
          disabled={mutation.isPending}
          className="bg-primary-700 hover:bg-primary-800 text-white font-medium py-2 px-5 rounded-lg text-sm transition-colors disabled:opacity-50"
        >
          {mutation.isPending ? 'Saving...' : workerId ? 'Update' : 'Add Worker'}
        </button>
      </div>
    </form>
  );
}

function PlusIcon({ className }: { className?: string }) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
    </svg>
  );
}
