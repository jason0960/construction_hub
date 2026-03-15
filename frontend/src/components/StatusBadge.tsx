import type { JobStatus, PermitStatus, WorkerStatus } from '../types';

const statusColors: Record<string, string> = {
  // Job statuses
  LEAD: 'bg-purple-100 text-purple-700',
  ESTIMATED: 'bg-indigo-100 text-indigo-700',
  CONTRACTED: 'bg-blue-100 text-blue-700',
  IN_PROGRESS: 'bg-amber-100 text-amber-700',
  ON_HOLD: 'bg-orange-100 text-orange-700',
  COMPLETED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
  // Permit statuses
  PENDING: 'bg-slate-100 text-slate-700',
  ACTIVE: 'bg-green-100 text-green-700',
  EXPIRED: 'bg-orange-100 text-orange-700',
  RENEWED: 'bg-teal-100 text-teal-700',
  // Worker statuses
  INACTIVE: 'bg-slate-100 text-slate-700',
  // Crew assignment statuses
  ASSIGNED: 'bg-blue-100 text-blue-700',
  REMOVED: 'bg-red-100 text-red-700',
};

type AnyStatus = JobStatus | PermitStatus | WorkerStatus | string;

export default function StatusBadge({ status }: { status: AnyStatus }) {
  const color = statusColors[status] || 'bg-slate-100 text-slate-700';
  const label = status.replace(/_/g, ' ');
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${color}`}>
      {label}
    </span>
  );
}
