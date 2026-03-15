import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { jobsApi } from '../api/jobs';
import type { JobStatus } from '../types';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import JobForm from './JobForm';
import { formatCurrency, formatDate } from '../utils/format';
import { useAuth } from '../contexts/AuthContext';

const STATUS_TABS: { label: string; value: JobStatus | '' }[] = [
  { label: 'All', value: '' },
  { label: 'Lead', value: 'LEAD' },
  { label: 'Estimated', value: 'ESTIMATED' },
  { label: 'Contracted', value: 'CONTRACTED' },
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'On Hold', value: 'ON_HOLD' },
  { label: 'Completed', value: 'COMPLETED' },
];

export default function JobsPage() {
  const { user } = useAuth();
  const isOwner = user?.role === 'OWNER' || user?.role === 'ADMIN';
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [search, setSearch] = useState('');
  const [showCreate, setShowCreate] = useState(false);

  const { data: jobs = [], isLoading, refetch } = useQuery({
    queryKey: ['jobs'],
    queryFn: () => jobsApi.list(),
  });

  const filtered = useMemo(() => {
    let result = jobs;
    if (statusFilter) {
      result = result.filter((j) => j.status === statusFilter);
    }
    if (search) {
      const q = search.toLowerCase();
      result = result.filter(
        (j) =>
          j.title.toLowerCase().includes(q) ||
          j.clientName?.toLowerCase().includes(q) ||
          j.siteAddress?.toLowerCase().includes(q)
      );
    }
    return result;
  }, [jobs, statusFilter, search]);

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">
            {isOwner ? 'Jobs' : 'My Jobs'}
          </h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {filtered.length} job{filtered.length !== 1 ? 's' : ''}
          </p>
        </div>
        {isOwner && (
          <button
            onClick={() => setShowCreate(true)}
            className="inline-flex items-center gap-1.5 bg-primary-700 hover:bg-primary-800 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            <PlusIcon className="w-4 h-4" />
            New Job
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3 mb-4">
        <div className="flex gap-1 overflow-x-auto pb-1">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              onClick={() => setStatusFilter(tab.value)}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
                statusFilter === tab.value
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        <input
          type="text"
          placeholder="Search jobs..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="sm:ml-auto w-full sm:w-64 rounded-lg border border-slate-300 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>

      {/* Jobs list */}
      {isLoading ? (
        <div className="flex justify-center py-12">
          <p className="text-gray-500">Loading jobs...</p>
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg border border-gray-200">
          <h3 className="text-sm font-medium text-gray-800">No jobs found</h3>
          <p className="text-sm text-gray-500 mt-1">
            {isOwner ? 'Create a job to get started.' : 'No jobs assigned to you yet.'}
          </p>
        </div>
      ) : (
        <div className="space-y-2">
          {filtered.map((job) => (
            <Link
              key={job.id}
              to={`/jobs/${job.id}`}
              className="block bg-white rounded-xl border border-slate-200 px-4 py-3.5 hover:border-primary-300 hover:shadow-sm transition-all"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="text-sm font-semibold text-slate-900 truncate">{job.title}</h3>
                    <StatusBadge status={job.status} />
                  </div>
                  <p className="text-sm text-slate-500 truncate">
                    {job.clientName}{job.siteAddress ? ` • ${job.siteAddress}, ${job.siteCity}` : ''}
                  </p>
                  <div className="flex items-center gap-4 mt-2 text-xs text-slate-400">
                    {job.startDate && <span>Start: {formatDate(job.startDate)}</span>}
                    {job.estimatedEndDate && <span>Est. End: {formatDate(job.estimatedEndDate)}</span>}
                  </div>
                </div>
                {isOwner && job.contractPrice != null && (
                  <div className="text-right shrink-0">
                    <p className="text-sm font-semibold text-slate-900">
                      {formatCurrency(job.contractPrice)}
                    </p>
                    {job.profit != null && (
                      <p
                        className={`text-xs font-medium ${
                          job.profit >= 0 ? 'text-green-600' : 'text-red-600'
                        }`}
                      >
                        {formatCurrency(job.profit)} profit
                      </p>
                    )}
                  </div>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Create Job Modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Job" wide>
        <JobForm
          onSuccess={() => {
            setShowCreate(false);
            refetch();
          }}
        />
      </Modal>
    </div>
  );
}

function PlusIcon({ className }: { className?: string }) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
    </svg>
  );
}
