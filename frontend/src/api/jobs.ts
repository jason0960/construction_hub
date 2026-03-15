import api from './client';
import type { JobRequest, JobResponse, JobStatus } from '../types';

export const jobsApi = {
  list: () =>
    api.get<JobResponse[]>('/jobs').then((r) => r.data),

  get: (id: number) =>
    api.get<JobResponse>(`/jobs/${id}`).then((r) => r.data),

  create: (data: JobRequest) =>
    api.post<JobResponse>('/jobs', data).then((r) => r.data),

  update: (id: number, data: JobRequest) =>
    api.put<JobResponse>(`/jobs/${id}`, data).then((r) => r.data),

  updateStatus: (id: number, status: JobStatus) =>
    api.patch<JobResponse>(`/jobs/${id}/status?status=${status}`).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/jobs/${id}`),
};
