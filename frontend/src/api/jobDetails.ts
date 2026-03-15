import api from './client';
import type {
  CrewAssignmentRequest,
  CrewAssignmentResponse,
  TimeEntryRequest,
  TimeEntryResponse,
  PermitRequest,
  PermitResponse,
  MaterialRequest,
  MaterialResponse,
  JobNoteRequest,
  JobNoteResponse,
} from '../types';

// ── Crew ──
export const crewApi = {
  list: (jobId: number) =>
    api.get<CrewAssignmentResponse[]>(`/jobs/${jobId}/crew`).then((r) => r.data),

  assign: (jobId: number, data: CrewAssignmentRequest) =>
    api.post<CrewAssignmentResponse>(`/jobs/${jobId}/crew`, data).then((r) => r.data),

  update: (jobId: number, id: number, data: CrewAssignmentRequest) =>
    api.put<CrewAssignmentResponse>(`/jobs/${jobId}/crew/${id}`, data).then((r) => r.data),

  remove: (jobId: number, id: number) =>
    api.delete(`/jobs/${jobId}/crew/${id}`),
};

// ── Time Entries ──
export const timeEntriesApi = {
  list: (jobId: number) =>
    api.get<TimeEntryResponse[]>(`/jobs/${jobId}/time-entries`).then((r) => r.data),

  create: (jobId: number, data: TimeEntryRequest) =>
    api.post<TimeEntryResponse>(`/jobs/${jobId}/time-entries`, data).then((r) => r.data),

  update: (jobId: number, id: number, data: TimeEntryRequest) =>
    api.put<TimeEntryResponse>(`/jobs/${jobId}/time-entries/${id}`, data).then((r) => r.data),

  delete: (jobId: number, id: number) =>
    api.delete(`/jobs/${jobId}/time-entries/${id}`),
};

// ── Permits ──
export const permitsApi = {
  list: (jobId: number) =>
    api.get<PermitResponse[]>(`/jobs/${jobId}/permits`).then((r) => r.data),

  create: (jobId: number, data: PermitRequest) =>
    api.post<PermitResponse>(`/jobs/${jobId}/permits`, data).then((r) => r.data),

  update: (jobId: number, id: number, data: PermitRequest) =>
    api.put<PermitResponse>(`/jobs/${jobId}/permits/${id}`, data).then((r) => r.data),

  delete: (jobId: number, id: number) =>
    api.delete(`/jobs/${jobId}/permits/${id}`),
};

// ── Materials ──
export const materialsApi = {
  list: (jobId: number) =>
    api.get<MaterialResponse[]>(`/jobs/${jobId}/materials`).then((r) => r.data),

  create: (jobId: number, data: MaterialRequest) =>
    api.post<MaterialResponse>(`/jobs/${jobId}/materials`, data).then((r) => r.data),

  update: (jobId: number, id: number, data: MaterialRequest) =>
    api.put<MaterialResponse>(`/jobs/${jobId}/materials/${id}`, data).then((r) => r.data),

  delete: (jobId: number, id: number) =>
    api.delete(`/jobs/${jobId}/materials/${id}`),
};

// ── Notes ──
export const notesApi = {
  list: (jobId: number) =>
    api.get<JobNoteResponse[]>(`/jobs/${jobId}/notes`).then((r) => r.data),

  create: (jobId: number, data: JobNoteRequest) =>
    api.post<JobNoteResponse>(`/jobs/${jobId}/notes`, data).then((r) => r.data),
};
