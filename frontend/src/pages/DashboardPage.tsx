import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/dashboard';
import { useNavigate } from 'react-router-dom';
import { formatCurrency } from '../utils/format';
import StatusBadge from '../components/StatusBadge';

export default function DashboardPage() {
  const navigate = useNavigate();
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardApi.get,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <p className="text-gray-500">Loading dashboard...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex justify-center py-12">
        <div className="text-center">
          <p className="text-red-600 font-medium">Failed to load dashboard</p>
          <p className="text-sm text-gray-500 mt-1">Please try refreshing the page.</p>
        </div>
      </div>
    );
  }

  if (!data) return null;

  const statCards = [
    { label: 'Total Jobs', value: data.totalJobs, color: 'bg-blue-50 text-blue-700' },
    { label: 'Active Jobs', value: data.activeJobs, color: 'bg-green-50 text-green-700' },
    { label: 'Completed', value: data.completedJobs, color: 'bg-slate-50 text-slate-700' },
    { label: 'Workers', value: data.totalWorkers, color: 'bg-purple-50 text-purple-700' },
  ];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-xl font-bold text-slate-900">Dashboard</h1>
        <p className="text-sm text-slate-500 mt-0.5">Overview of your business</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 mb-6">
        {statCards.map((card) => (
          <div
            key={card.label}
            className="bg-white rounded-xl border border-slate-200 p-4"
          >
            <p className="text-xs font-medium text-slate-500 mb-1">{card.label}</p>
            <p className={`text-2xl font-bold ${card.color.split(' ')[1]}`}>
              {card.value}
            </p>
          </div>
        ))}
      </div>

      {/* Revenue card */}
      {data.totalRevenue > 0 && (
        <div className="bg-white rounded-xl border border-slate-200 p-4 mb-6">
          <p className="text-xs font-medium text-slate-500 mb-1">Completed Revenue</p>
          <p className="text-2xl font-bold text-green-700">
            {formatCurrency(data.totalRevenue)}
          </p>
        </div>
      )}

      {/* Jobs by status breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Status breakdown */}
        <div className="bg-white rounded-xl border border-slate-200 p-4">
          <h2 className="text-sm font-semibold text-slate-900 mb-3">Jobs by Status</h2>
          <div className="space-y-2">
            {Object.entries(data.jobsByStatus)
              .filter(([, count]) => count > 0)
              .map(([status, count]) => (
                <div key={status} className="flex items-center justify-between">
                  <StatusBadge status={status} />
                  <span className="text-sm font-medium text-slate-700">{count}</span>
                </div>
              ))}
            {Object.values(data.jobsByStatus).every((c) => c === 0) && (
              <p className="text-sm text-slate-500">No jobs yet</p>
            )}
          </div>
        </div>

        {/* Recent jobs */}
        <div className="bg-white rounded-xl border border-slate-200 p-4">
          <h2 className="text-sm font-semibold text-slate-900 mb-3">Recent Activity</h2>
          {data.recentJobs.length === 0 ? (
            <p className="text-sm text-slate-500">No jobs yet</p>
          ) : (
            <div className="space-y-2">
              {data.recentJobs.map((job) => (
                <button
                  key={job.id}
                  onClick={() => navigate(`/jobs/${job.id}`)}
                  className="w-full text-left flex items-center justify-between py-2 px-2 rounded-lg hover:bg-slate-50 transition-colors"
                >
                  <div>
                    <p className="text-sm font-medium text-slate-900">{job.title}</p>
                    {job.clientName && (
                      <p className="text-xs text-slate-500">{job.clientName}</p>
                    )}
                  </div>
                  <StatusBadge status={job.status} />
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
