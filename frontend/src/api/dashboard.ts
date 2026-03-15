import api from './client';

export interface DashboardData {
  totalJobs: number;
  activeJobs: number;
  completedJobs: number;
  totalWorkers: number;
  totalRevenue: number;
  totalCosts: number;
  jobsByStatus: Record<string, number>;
  recentJobs: {
    id: number;
    title: string;
    status: string;
    clientName?: string;
    updatedAt?: string;
  }[];
}

export const dashboardApi = {
  get: () => api.get<DashboardData>('/dashboard').then((r) => r.data),
};
