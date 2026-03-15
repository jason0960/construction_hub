import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { permitsApi } from '../../api/jobDetails';
import type { PermitRequest } from '../../types';
import { inputCls, labelCls, btnCls } from './formStyles';
import { getErrorMessage } from '../../utils/error';

interface Props {
  jobId: number;
  onSuccess: () => void;
}

export default function QuickPermitForm({ jobId, onSuccess }: Props) {
  const [form, setForm] = useState<PermitRequest>({
    permitType: '',
    permitNumber: '',
    status: 'PENDING',
    issuingAuthority: '',
    fee: undefined,
    notes: '',
  });
  const [feeStr, setFeeStr] = useState('');
  const [error, setError] = useState('');
  const [feeError, setFeeError] = useState('');
  const mutation = useMutation({
    mutationFn: (data: PermitRequest) => permitsApi.create(jobId, data),
    onSuccess,
    onError: (err) => setError(getErrorMessage(err)),
  });

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        setError('');
        setFeeError('');
        if (feeStr && (isNaN(Number(feeStr)) || Number(feeStr) < 0)) {
          setFeeError('Enter a valid fee amount');
          return;
        }
        mutation.mutate({ ...form, fee: feeStr ? Number(feeStr) : undefined });
      }}
      className="space-y-3"
    >
      {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-lg">{error}</p>}
      <div>
        <label className={labelCls}>Permit Type *</label>
        <input
          required
          className={inputCls}
          value={form.permitType}
          onChange={(e) => setForm({ ...form, permitType: e.target.value })}
          placeholder="Building, Electrical, Plumbing..."
        />
      </div>
      <div>
        <label className={labelCls}>Permit Number</label>
        <input
          className={inputCls}
          value={form.permitNumber}
          onChange={(e) => setForm({ ...form, permitNumber: e.target.value })}
        />
      </div>
      <div>
        <label className={labelCls}>Issuing Authority</label>
        <input
          className={inputCls}
          value={form.issuingAuthority}
          onChange={(e) => setForm({ ...form, issuingAuthority: e.target.value })}
        />
      </div>
      <div>
        <label className={labelCls}>Fee</label>
        <input
          className={`${inputCls}${feeError ? ' border-red-400' : ''}`}
          value={feeStr}
          onChange={(e) => { setFeeStr(e.target.value); setFeeError(''); }}
          placeholder="0.00"
        />
        {feeError && <p className="text-xs text-red-600 mt-1">{feeError}</p>}
      </div>
      <div className="flex justify-end">
        <button type="submit" disabled={mutation.isPending} className={btnCls}>
          {mutation.isPending ? 'Saving...' : 'Add Permit'}
        </button>
      </div>
    </form>
  );
}
