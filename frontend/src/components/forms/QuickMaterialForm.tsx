import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { materialsApi } from '../../api/jobDetails';
import type { MaterialRequest } from '../../types';
import { inputCls, labelCls, btnCls } from './formStyles';
import { getErrorMessage } from '../../utils/error';

interface Props {
  jobId: number;
  onSuccess: () => void;
}

export default function QuickMaterialForm({ jobId, onSuccess }: Props) {
  const [form, setForm] = useState<MaterialRequest>({
    name: '',
    quantity: 1,
    unitCost: 0,
  });
  const [costStr, setCostStr] = useState('');
  const [error, setError] = useState('');
  const [costError, setCostError] = useState('');
  const mutation = useMutation({
    mutationFn: (data: MaterialRequest) => materialsApi.create(jobId, data),
    onSuccess,
    onError: (err) => setError(getErrorMessage(err)),
  });

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        setError('');
        setCostError('');
        if (costStr && (isNaN(Number(costStr)) || Number(costStr) < 0)) {
          setCostError('Enter a valid cost');
          return;
        }
        mutation.mutate({ ...form, unitCost: costStr ? Number(costStr) : 0 });
      }}
      className="space-y-3"
    >
      {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-lg">{error}</p>}
      <div>
        <label className={labelCls}>Item Name *</label>
        <input
          required
          className={inputCls}
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          placeholder="2x4 Lumber, Drywall sheets..."
        />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className={labelCls}>Qty *</label>
          <input
            type="number"
            min="0.01"
            step="0.01"
            required
            className={inputCls}
            value={form.quantity}
            onChange={(e) => setForm({ ...form, quantity: parseFloat(e.target.value) })}
          />
        </div>
        <div>
          <label className={labelCls}>Unit Cost *</label>
          <input
            required
            className={`${inputCls}${costError ? ' border-red-400' : ''}`}
            value={costStr}
            onChange={(e) => { setCostStr(e.target.value); setCostError(''); }}
            placeholder="0.00"
          />
          {costError && <p className="text-xs text-red-600 mt-1">{costError}</p>}
        </div>
      </div>
      <div className="flex justify-end">
        <button type="submit" disabled={mutation.isPending} className={btnCls}>
          {mutation.isPending ? 'Saving...' : 'Add Material'}
        </button>
      </div>
    </form>
  );
}
