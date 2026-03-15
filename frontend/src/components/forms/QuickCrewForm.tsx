import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { crewApi } from '../../api/jobDetails';
import { workersApi } from '../../api/workers';
import type { CrewAssignmentRequest } from '../../types';
import { inputCls, labelCls, btnCls } from './formStyles';
import { getErrorMessage } from '../../utils/error';

interface Props {
  jobId: number;
  onSuccess: () => void;
}

export default function QuickCrewForm({ jobId, onSuccess }: Props) {
  const { data: workers = [] } = useQuery({
    queryKey: ['workers'],
    queryFn: () => workersApi.list(),
  });
  const [form, setForm] = useState<CrewAssignmentRequest>({
    workerId: 0,
    roleOnJob: '',
    startDate: '',
    endDate: '',
  });
  const [error, setError] = useState('');
  const mutation = useMutation({
    mutationFn: (data: CrewAssignmentRequest) => crewApi.assign(jobId, data),
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
        <label className={labelCls}>Role on Job</label>
        <input
          className={inputCls}
          value={form.roleOnJob}
          onChange={(e) => setForm({ ...form, roleOnJob: e.target.value })}
          placeholder="Lead Electrician, Plumber..."
        />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className={labelCls}>Start</label>
          <input
            type="date"
            className={inputCls}
            value={form.startDate}
            onChange={(e) => setForm({ ...form, startDate: e.target.value })}
          />
        </div>
        <div>
          <label className={labelCls}>End</label>
          <input
            type="date"
            className={inputCls}
            value={form.endDate}
            onChange={(e) => setForm({ ...form, endDate: e.target.value })}
          />
        </div>
      </div>
      <div className="flex justify-end">
        <button type="submit" disabled={mutation.isPending} className={btnCls}>
          {mutation.isPending ? 'Saving...' : 'Assign'}
        </button>
      </div>
    </form>
  );
}
