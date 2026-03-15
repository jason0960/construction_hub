import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { notesApi } from '../../api/jobDetails';
import type { JobNoteRequest } from '../../types';
import { inputCls, labelCls, btnCls } from './formStyles';
import { getErrorMessage } from '../../utils/error';

interface Props {
  jobId: number;
  isOwner: boolean;
  onSuccess: () => void;
}

export default function QuickNoteForm({ jobId, isOwner, onSuccess }: Props) {
  const [form, setForm] = useState<JobNoteRequest>({
    content: '',
    visibility: 'SHARED',
  });
  const [error, setError] = useState('');
  const mutation = useMutation({
    mutationFn: (data: JobNoteRequest) => notesApi.create(jobId, data),
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
        <label className={labelCls}>Note *</label>
        <textarea
          required
          rows={4}
          className={inputCls}
          value={form.content}
          onChange={(e) => setForm({ ...form, content: e.target.value })}
          placeholder="Add a note about this job..."
        />
      </div>
      {isOwner && (
        <div>
          <label className={labelCls}>Visibility</label>
          <select
            className={inputCls}
            value={form.visibility}
            onChange={(e) =>
              setForm({ ...form, visibility: e.target.value as JobNoteRequest['visibility'] })
            }
          >
            <option value="SHARED">Shared with crew</option>
            <option value="OWNER_ONLY">Private (Owner only)</option>
          </select>
        </div>
      )}
      <div className="flex justify-end">
        <button type="submit" disabled={mutation.isPending} className={btnCls}>
          {mutation.isPending ? 'Saving...' : 'Add Note'}
        </button>
      </div>
    </form>
  );
}
