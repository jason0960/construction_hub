import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { timeEntriesApi } from '../../api/jobDetails';
import { workersApi } from '../../api/workers';
import type { TimeEntryRequest } from '../../types';
import { inputCls, labelCls, btnCls } from './formStyles';
import { getErrorMessage } from '../../utils/error';

interface Props {
  jobId: number;
  onSuccess: () => void;
}

export default function QuickTimeForm({ jobId, onSuccess }: Props) {
  const { data: workers = [] } = useQuery({
    queryKey: ['workers'],
    queryFn: () => workersApi.list(),
  });
  const [form, setForm] = useState<TimeEntryRequest>({
    workerId: 0,
    entryDate: new Date().toISOString().split('T')[0],
    hours: 0,
    notes: '',
  });
  const [error, setError] = useState('');
  const mutation = useMutation({
    mutationFn: (data: TimeEntryRequest) => timeEntriesApi.create(jobId, data),
    onSuccess,
    onError: (err) => setError(getErrorMessage(err)),
  });

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        setError('');
        mutation.mutate(form);
      }}
      className="space-y-3"
    >
      {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-lg">{error}</p>}
      <div>
        <label className={labelCls}>Worker *</label>
        <select
          required
          className={inputCls}
          value={form.workerId || ''}
          onChange={(e) => setForm({ ...form, workerId: parseInt(e.target.value) || 0 })}
        >
          <option value="">-- Pick a worker --</option>
          {workers.map((w) => (
            <option key={w.id} value={w.id}>
              {w.firstName} {w.lastName}{w.trade ? ` - ${w.trade}` : ''}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className={labelCls}>Date *</label>
        <input
          type="date"
          required
          className={inputCls}
          value={form.entryDate}
          onChange={(e) => setForm({ ...form, entryDate: e.target.value })}
        />
      </div>
      <div>
        <label className={labelCls}>Hours *</label>
        <input
          type="number"
          step="0.25"
          min="0.25"
          required
          className={inputCls}
          value={form.hours || ''}
          onChange={(e) => setForm({ ...form, hours: parseFloat(e.target.value) })}
        />
      </div>
      <div>
        <label className={labelCls}>Notes</label>
        <input
          className={inputCls}
          value={form.notes}
          onChange={(e) => setForm({ ...form, notes: e.target.value })}
        />
      </div>
      <div className="flex justify-end">
        <button type="submit" disabled={mutation.isPending} className={btnCls}>
          {mutation.isPending ? 'Saving...' : 'Log Time'}
        </button>
      </div>
    </form>
  );
}
