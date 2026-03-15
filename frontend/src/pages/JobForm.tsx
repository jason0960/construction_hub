import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { jobsApi } from '../api/jobs';
import type { JobRequest, JobStatus } from '../types';

const JOB_STATUSES: JobStatus[] = [
  'LEAD',
  'ESTIMATED',
  'CONTRACTED',
  'IN_PROGRESS',
  'ON_HOLD',
  'COMPLETED',
  'CANCELLED',
];

interface JobFormProps {
  initial?: Partial<JobRequest>;
  jobId?: number;
  onSuccess: () => void;
}

export default function JobForm({ initial, jobId, onSuccess }: JobFormProps) {
  const [form, setForm] = useState<JobRequest>({
    title: initial?.title || '',
    description: initial?.description || '',
    status: initial?.status || 'LEAD',
    contractPrice: initial?.contractPrice ?? undefined,
    startDate: initial?.startDate || '',
    estimatedEndDate: initial?.estimatedEndDate || '',
    actualEndDate: initial?.actualEndDate || '',
    siteAddress: initial?.siteAddress || '',
    siteCity: initial?.siteCity || '',
    siteState: initial?.siteState || '',
    siteZip: initial?.siteZip || '',
    siteUnit: initial?.siteUnit || '',
    clientName: initial?.clientName || '',
    clientEmail: initial?.clientEmail || '',
    clientPhone: initial?.clientPhone || '',
  });

  const [error, setError] = useState('');

  const [priceStr, setPriceStr] = useState(
    initial?.contractPrice != null ? String(initial.contractPrice) : ''
  );

  const mutation = useMutation({
    mutationFn: (data: JobRequest) =>
      jobId ? jobsApi.update(jobId, data) : jobsApi.create(data),
    onSuccess,
    onError: (err: unknown) => {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Failed to save job';
      setError(msg);
    },
  });

  const set = (field: keyof JobRequest, value: string | number | undefined) =>
    setForm((f) => ({ ...f, [field]: value }));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.mutate({ ...form, contractPrice: priceStr ? Number(priceStr) : undefined });
  };

  const inputCls =
    'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500';
  const labelCls = 'block text-sm font-medium text-slate-700 mb-1';

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {error && (
        <div className="bg-danger-50 text-danger-700 text-sm rounded-lg px-4 py-3">{error}</div>
      )}

      {/* Job Info */}
      <fieldset>
        <legend className="text-sm font-semibold text-slate-900 mb-3">Job Information</legend>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="sm:col-span-2">
            <label className={labelCls}>Title *</label>
            <input
              required
              className={inputCls}
              value={form.title}
              onChange={(e) => set('title', e.target.value)}
              placeholder="Kitchen Remodel - 123 Main St"
            />
          </div>
          <div>
            <label className={labelCls}>Status</label>
            <select
              className={inputCls}
              value={form.status}
              onChange={(e) => set('status', e.target.value as JobStatus)}
            >
              {JOB_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className={labelCls}>Contract Price</label>
            <input
              className={inputCls}
              value={priceStr}
              onChange={(e) => setPriceStr(e.target.value)}
              placeholder="0.00"
            />
          </div>
          <div className="sm:col-span-2">
            <label className={labelCls}>Description</label>
            <textarea
              rows={2}
              className={inputCls}
              value={form.description}
              onChange={(e) => set('description', e.target.value)}
              placeholder="Brief description of the job..."
            />
          </div>
          <div>
            <label className={labelCls}>Start Date</label>
            <input
              type="date"
              className={inputCls}
              value={form.startDate}
              onChange={(e) => set('startDate', e.target.value)}
            />
          </div>
          <div>
            <label className={labelCls}>Est. End</label>
            <input
              type="date"
              className={inputCls}
              value={form.estimatedEndDate}
              onChange={(e) => set('estimatedEndDate', e.target.value)}
            />
          </div>
        </div>
      </fieldset>

      {/* Site */}
      <fieldset>
        <legend className="text-sm font-semibold text-slate-900 mb-3">Job Site</legend>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="sm:col-span-2">
            <label className={labelCls}>Address</label>
            <input
              className={inputCls}
              value={form.siteAddress}
              onChange={(e) => set('siteAddress', e.target.value)}
              placeholder="123 Main St"
            />
          </div>
          <div>
            <label className={labelCls}>Unit / Suite</label>
            <input
              className={inputCls}
              value={form.siteUnit}
              onChange={(e) => set('siteUnit', e.target.value)}
              placeholder="Apt 4B"
            />
          </div>
          <div>
            <label className={labelCls}>City</label>
            <input
              className={inputCls}
              value={form.siteCity}
              onChange={(e) => set('siteCity', e.target.value)}
            />
          </div>
          <div>
            <label className={labelCls}>State</label>
            <input
              className={inputCls}
              value={form.siteState}
              onChange={(e) => set('siteState', e.target.value)}
              placeholder="CA"
            />
          </div>
          <div>
            <label className={labelCls}>Zip</label>
            <input
              className={inputCls}
              value={form.siteZip}
              onChange={(e) => set('siteZip', e.target.value)}
            />
          </div>
        </div>
      </fieldset>

      {/* Client */}
      <fieldset>
        <legend className="text-sm font-semibold text-slate-900 mb-3">Customer</legend>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="sm:col-span-2">
            <label className={labelCls}>Name</label>
            <input
              className={inputCls}
              value={form.clientName}
              onChange={(e) => set('clientName', e.target.value)}
            />
          </div>
          <div>
            <label className={labelCls}>Email</label>
            <input
              type="email"
              className={inputCls}
              value={form.clientEmail}
              onChange={(e) => set('clientEmail', e.target.value)}
            />
          </div>
          <div>
            <label className={labelCls}>Phone</label>
            <input
              type="tel"
              className={inputCls}
              value={form.clientPhone}
              onChange={(e) => set('clientPhone', e.target.value)}
            />
          </div>
        </div>
      </fieldset>

      <div className="flex justify-end gap-2 pt-2">
        <button
          type="submit"
          disabled={mutation.isPending}
          className="bg-primary-700 hover:bg-primary-800 text-white font-medium py-2 px-5 rounded-lg text-sm transition-colors disabled:opacity-50"
        >
          {mutation.isPending ? 'Saving...' : jobId ? 'Update Job' : 'Create Job'}
        </button>
      </div>
    </form>
  );
}
