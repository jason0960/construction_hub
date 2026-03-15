import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { jobsApi } from '../api/jobs';
import { crewApi, timeEntriesApi, permitsApi, materialsApi, notesApi } from '../api/jobDetails';
import { workersApi } from '../api/workers';
import StatusBadge from '../components/StatusBadge';
import CollapsibleSection from '../components/CollapsibleSection';
import Modal from '../components/Modal';
import JobForm from './JobForm';
import { formatCurrency, formatDate, formatDateTime } from '../utils/format';
import { useAuth } from '../contexts/AuthContext';
import { useState } from 'react';
import type {
  CrewAssignmentRequest,
  TimeEntryRequest,
  PermitRequest,
  MaterialRequest,
  JobNoteRequest,
} from '../types';

export default function JobDetailPage() {
  const { id } = useParams<{ id: string }>();
  const jobId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const isOwner = user?.role === 'OWNER' || user?.role === 'ADMIN';

  const [showEdit, setShowEdit] = useState(false);
  const [showCrewForm, setShowCrewForm] = useState(false);
  const [showTimeForm, setShowTimeForm] = useState(false);
  const [showPermitForm, setShowPermitForm] = useState(false);
  const [showMaterialForm, setShowMaterialForm] = useState(false);
  const [showNoteForm, setShowNoteForm] = useState(false);

  // ── Queries ──
  const { data: job, isLoading } = useQuery({
    queryKey: ['job', jobId],
    queryFn: () => jobsApi.get(jobId),
  });

  const { data: crew = [] } = useQuery({
    queryKey: ['crew', jobId],
    queryFn: () => crewApi.list(jobId),
  });

  const { data: timeEntries = [] } = useQuery({
    queryKey: ['timeEntries', jobId],
    queryFn: () => timeEntriesApi.list(jobId),
  });

  const { data: permits = [] } = useQuery({
    queryKey: ['permits', jobId],
    queryFn: () => permitsApi.list(jobId),
  });

  const { data: materials = [] } = useQuery({
    queryKey: ['materials', jobId],
    queryFn: () => materialsApi.list(jobId),
  });

  const { data: notes = [] } = useQuery({
    queryKey: ['notes', jobId],
    queryFn: () => notesApi.list(jobId),
  });

  // ── Delete ──
  const deleteMutation = useMutation({
    mutationFn: () => jobsApi.delete(jobId),
    onSuccess: () => navigate('/'),
  });

  const invalidate = (key: string) => () => queryClient.invalidateQueries({ queryKey: [key, jobId] });

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <p className="text-gray-500">Loading job...</p>
      </div>
    );
  }

  if (!job) {
    return (
      <div className="text-center py-16">
        <h2 className="text-lg font-semibold text-slate-900">Job not found</h2>
        <Link to="/" className="text-primary-600 text-sm mt-2 inline-block">
          Back to jobs
        </Link>
      </div>
    );
  }

  const siteDisplay = [job.siteAddress, job.siteUnit].filter(Boolean).join(' ');
  const cityStateZip = [job.siteCity, job.siteState].filter(Boolean).join(', ') +
    (job.siteZip ? ` ${job.siteZip}` : '');

  return (
    <div>
      {/* Breadcrumb + Header */}
      <div className="mb-6">
        <Link to="/" className="text-sm text-primary-600 hover:text-primary-700 mb-2 inline-block">
          ← Back to Jobs
        </Link>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-900">{job.title}</h1>
              <StatusBadge status={job.status} />
            </div>
            {job.description && (
              <p className="text-sm text-slate-500 mt-1">{job.description}</p>
            )}
          </div>
          {isOwner && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => setShowEdit(true)}
                className="text-sm font-medium text-primary-600 hover:text-primary-700 px-3 py-1.5 rounded-lg border border-slate-200 hover:border-primary-300 transition-colors"
              >
                Edit
              </button>
              <button
                onClick={() => {
                  if (confirm('Delete this job?')) deleteMutation.mutate();
                }}
                className="text-sm font-medium text-danger-600 hover:text-danger-700 px-3 py-1.5 rounded-lg border border-slate-200 hover:border-danger-300 transition-colors"
              >
                Delete
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="space-y-4">
        {/* Customer & Site */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <InfoCard title="Customer">
            <InfoRow label="Name" value={job.clientName} />
            <InfoRow label="Email" value={job.clientEmail} />
            <InfoRow label="Phone" value={job.clientPhone} />
          </InfoCard>
          <InfoCard title="Job Site">
            <InfoRow label="Address" value={siteDisplay} />
            <InfoRow label="City / State / Zip" value={cityStateZip} />
          </InfoCard>
        </div>

        {/* Timeline */}
        <InfoCard title="Timeline">
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            <InfoRow label="Start Date" value={formatDate(job.startDate)} />
            <InfoRow label="Est. End" value={formatDate(job.estimatedEndDate)} />
            <InfoRow label="Actual End" value={formatDate(job.actualEndDate)} />
          </div>
        </InfoCard>

        {/* Financials — owner only */}
        {isOwner && (
          <div className="bg-white rounded-xl border border-slate-200 p-4">
            <h3 className="text-sm font-semibold text-slate-900 mb-3">Financials</h3>
            <div className="grid grid-cols-2 sm:grid-cols-5 gap-4">
              <div>
                <p className="text-xs text-slate-500">Contract</p>
                <p className="text-sm font-semibold text-slate-900">{formatCurrency(job.contractPrice)}</p>
              </div>
              <div>
                <p className="text-xs text-slate-500">Labor</p>
                <p className="text-sm font-semibold text-red-600">-{formatCurrency(job.laborCost)}</p>
              </div>
              <div>
                <p className="text-xs text-slate-500">Materials</p>
                <p className="text-sm font-semibold text-red-600">-{formatCurrency(job.materialsCost)}</p>
              </div>
              <div>
                <p className="text-xs text-slate-500">Permits</p>
                <p className="text-sm font-semibold text-red-600">-{formatCurrency(job.permitFees)}</p>
              </div>
              <div>
                <p className="text-xs text-slate-500">Profit</p>
                <p className={`text-sm font-bold ${(job.profit ?? 0) >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {formatCurrency(job.profit)}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Crew */}
        <CollapsibleSection
          title="Crew"
          count={crew.length}
          defaultOpen
          actions={
            isOwner && (
              <AddButton onClick={() => setShowCrewForm(true)} />
            )
          }
        >
          {crew.length === 0 ? (
            <EmptyState text="No crew assigned" />
          ) : (
            <div className="space-y-2">
              {crew.map((c) => (
                <div key={c.id} className="flex items-center justify-between py-1.5">
                  <div>
                    <span className="text-sm font-medium text-slate-900">{c.workerName}</span>
                    {c.roleOnJob && <span className="text-xs text-slate-500 ml-2">{c.roleOnJob}</span>}
                    {c.workerTrade && <span className="text-xs text-slate-400 ml-1">({c.workerTrade})</span>}
                  </div>
                  <div className="flex items-center gap-2">
                    {c.totalHours != null && (
                      <span className="text-xs text-slate-500">{c.totalHours}h</span>
                    )}
                    <StatusBadge status={c.status} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </CollapsibleSection>

        {/* Time / Labor */}
        <CollapsibleSection
          title="Time & Labor"
          count={timeEntries.length}
          actions={<AddButton onClick={() => setShowTimeForm(true)} />}
        >
          {timeEntries.length === 0 ? (
            <EmptyState text="No time entries" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-xs text-slate-500 border-b border-slate-100">
                    <th className="pb-2 font-medium">Worker</th>
                    <th className="pb-2 font-medium">Date</th>
                    <th className="pb-2 font-medium text-right">Hours</th>
                    <th className="pb-2 font-medium">Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {timeEntries.map((t) => (
                    <tr key={t.id} className="border-b border-slate-50">
                      <td className="py-1.5">{t.workerName}</td>
                      <td className="py-1.5">{formatDate(t.entryDate)}</td>
                      <td className="py-1.5 text-right">{t.hours}h</td>
                      <td className="py-1.5 text-slate-500 truncate max-w-[200px]">{t.notes}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CollapsibleSection>

        {/* Permits */}
        <CollapsibleSection
          title="Permits"
          count={permits.length}
          actions={isOwner && <AddButton onClick={() => setShowPermitForm(true)} />}
        >
          {permits.length === 0 ? (
            <EmptyState text="No permits" />
          ) : (
            <div className="space-y-2">
              {permits.map((p) => (
                <div key={p.id} className="flex items-center justify-between py-1.5">
                  <div>
                    <span className="text-sm font-medium text-slate-900">{p.permitType}</span>
                    {p.permitNumber && (
                      <span className="text-xs text-slate-500 ml-2">#{p.permitNumber}</span>
                    )}
                    {p.expirationDate && (
                      <span className="text-xs text-slate-400 ml-2">
                        Exp: {formatDate(p.expirationDate)}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-3">
                    {p.fee != null && (
                      <span className="text-xs text-slate-500">{formatCurrency(p.fee)}</span>
                    )}
                    <StatusBadge status={p.status} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </CollapsibleSection>

        {/* Materials */}
        <CollapsibleSection
          title="Materials"
          count={materials.length}
          actions={isOwner && <AddButton onClick={() => setShowMaterialForm(true)} />}
        >
          {materials.length === 0 ? (
            <EmptyState text="No materials" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-xs text-slate-500 border-b border-slate-100">
                    <th className="pb-2 font-medium">Item</th>
                    <th className="pb-2 font-medium text-right">Qty</th>
                    <th className="pb-2 font-medium text-right">Unit Cost</th>
                    <th className="pb-2 font-medium text-right">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {materials.map((m) => (
                    <tr key={m.id} className="border-b border-slate-50">
                      <td className="py-1.5 font-medium">{m.name}</td>
                      <td className="py-1.5 text-right">{m.quantity}</td>
                      <td className="py-1.5 text-right">{formatCurrency(m.unitCost)}</td>
                      <td className="py-1.5 text-right font-medium">{formatCurrency(m.total)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CollapsibleSection>

        {/* Notes */}
        <CollapsibleSection
          title="Notes"
          count={notes.length}
          defaultOpen
          actions={<AddButton onClick={() => setShowNoteForm(true)} />}
        >
          {notes.length === 0 ? (
            <EmptyState text="No notes yet" />
          ) : (
            <div className="space-y-3">
              {notes.map((n) => (
                <div key={n.id} className="border-l-2 border-slate-200 pl-3 py-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-sm font-medium text-slate-900">{n.authorName}</span>
                    <span className="text-xs text-slate-400">{formatDateTime(n.createdAt)}</span>
                    {n.visibility === 'OWNER_ONLY' && (
                      <span className="text-xs bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded">
                        Private
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-slate-700 whitespace-pre-wrap">{n.content}</p>
                </div>
              ))}
            </div>
          )}
        </CollapsibleSection>
      </div>

      {/* ── Modals ── */}
      <Modal open={showEdit} onClose={() => setShowEdit(false)} title="Edit Job" wide>
        <JobForm
          jobId={jobId}
          initial={job}
          onSuccess={() => {
            setShowEdit(false);
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
          }}
        />
      </Modal>

      <Modal open={showCrewForm} onClose={() => setShowCrewForm(false)} title="Add Crew Member">
        <QuickCrewForm
          jobId={jobId}
          onSuccess={() => {
            setShowCrewForm(false);
            invalidate('crew')();
          }}
        />
      </Modal>

      <Modal open={showTimeForm} onClose={() => setShowTimeForm(false)} title="Log Time">
        <QuickTimeForm
          jobId={jobId}
          onSuccess={() => {
            setShowTimeForm(false);
            invalidate('timeEntries')();
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
          }}
        />
      </Modal>

      <Modal open={showPermitForm} onClose={() => setShowPermitForm(false)} title="Add Permit">
        <QuickPermitForm
          jobId={jobId}
          onSuccess={() => {
            setShowPermitForm(false);
            invalidate('permits')();
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
          }}
        />
      </Modal>

      <Modal open={showMaterialForm} onClose={() => setShowMaterialForm(false)} title="Add Material">
        <QuickMaterialForm
          jobId={jobId}
          onSuccess={() => {
            setShowMaterialForm(false);
            invalidate('materials')();
            queryClient.invalidateQueries({ queryKey: ['job', jobId] });
          }}
        />
      </Modal>

      <Modal open={showNoteForm} onClose={() => setShowNoteForm(false)} title="Add Note">
        <QuickNoteForm
          jobId={jobId}
          isOwner={isOwner}
          onSuccess={() => {
            setShowNoteForm(false);
            invalidate('notes')();
          }}
        />
      </Modal>
    </div>
  );
}

// ── Helper components ──

function InfoCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="bg-white rounded-xl border border-slate-200 p-4">
      <h3 className="text-sm font-semibold text-slate-900 mb-3">{title}</h3>
      <div className="space-y-1.5">{children}</div>
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value?: string | null }) {
  if (!value) return null;
  return (
    <div className="flex justify-between text-sm">
      <span className="text-slate-500">{label}</span>
      <span className="text-slate-900 font-medium text-right">{value}</span>
    </div>
  );
}

function EmptyState({ text }: { text: string }) {
  return <p className="text-sm text-slate-400 py-2">{text}</p>;
}

function AddButton({ onClick }: { onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className="text-xs text-primary-600 hover:text-primary-700 font-medium px-2 py-1 rounded hover:bg-primary-50 transition-colors"
    >
      + Add
    </button>
  );
}

// ── Quick inline forms ──

const inputCls =
  'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500';
const labelCls = 'block text-sm font-medium text-slate-700 mb-1';
const btnCls =
  'bg-primary-700 hover:bg-primary-800 text-white font-medium py-2 px-5 rounded-lg text-sm transition-colors disabled:opacity-50';

function QuickCrewForm({ jobId, onSuccess }: { jobId: number; onSuccess: () => void }) {
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
  const mutation = useMutation({
    mutationFn: (data: CrewAssignmentRequest) => crewApi.assign(jobId, data),
    onSuccess,
  });
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        mutation.mutate(form);
      }}
      className="space-y-3"
    >
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

function QuickTimeForm({ jobId, onSuccess }: { jobId: number; onSuccess: () => void }) {
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
  const mutation = useMutation({
    mutationFn: (data: TimeEntryRequest) => timeEntriesApi.create(jobId, data),
    onSuccess,
  });
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        mutation.mutate(form);
      }}
      className="space-y-3"
    >
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

function QuickPermitForm({ jobId, onSuccess }: { jobId: number; onSuccess: () => void }) {
  const [form, setForm] = useState<PermitRequest>({
    permitType: '',
    permitNumber: '',
    status: 'PENDING',
    issuingAuthority: '',
    fee: undefined,
    notes: '',
  });
  const [feeStr, setFeeStr] = useState('');
  const mutation = useMutation({
    mutationFn: (data: PermitRequest) => permitsApi.create(jobId, data),
    onSuccess,
  });
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        mutation.mutate({ ...form, fee: feeStr ? Number(feeStr) : undefined });
      }}
      className="space-y-3"
    >
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
          className={inputCls}
          value={feeStr}
          onChange={(e) => setFeeStr(e.target.value)}
          placeholder="0.00"
        />
      </div>
      <div className="flex justify-end">
        <button type="submit" disabled={mutation.isPending} className={btnCls}>
          {mutation.isPending ? 'Saving...' : 'Add Permit'}
        </button>
      </div>
    </form>
  );
}

function QuickMaterialForm({ jobId, onSuccess }: { jobId: number; onSuccess: () => void }) {
  const [form, setForm] = useState<MaterialRequest>({
    name: '',
    quantity: 1,
    unitCost: 0,
  });
  const [costStr, setCostStr] = useState('');
  const mutation = useMutation({
    mutationFn: (data: MaterialRequest) => materialsApi.create(jobId, data),
    onSuccess,
  });
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        mutation.mutate({ ...form, unitCost: costStr ? Number(costStr) : 0 });
      }}
      className="space-y-3"
    >
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
            className={inputCls}
            value={costStr}
            onChange={(e) => setCostStr(e.target.value)}
            placeholder="0.00"
          />
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

function QuickNoteForm({
  jobId,
  isOwner,
  onSuccess,
}: {
  jobId: number;
  isOwner: boolean;
  onSuccess: () => void;
}) {
  const [form, setForm] = useState<JobNoteRequest>({
    content: '',
    visibility: 'SHARED',
  });
  const mutation = useMutation({
    mutationFn: (data: JobNoteRequest) => notesApi.create(jobId, data),
    onSuccess,
  });
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        mutation.mutate(form);
      }}
      className="space-y-3"
    >
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
