import api from './client';
import type { WorkerRequest, WorkerResponse } from '../types';

export const workersApi = {
  list: () =>
    api.get<WorkerResponse[]>('/workers').then((r) => r.data),

  get: (id: number) =>
    api.get<WorkerResponse>(`/workers/${id}`).then((r) => r.data),

  create: (data: WorkerRequest) =>
    api.post<WorkerResponse>('/workers', data).then((r) => r.data),

  update: (id: number, data: WorkerRequest) =>
    api.put<WorkerResponse>(`/workers/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/workers/${id}`),
};
