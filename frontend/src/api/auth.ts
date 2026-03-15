import api from './client';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  InviteWorkerRequest,
} from '../types';

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/auth/login', data).then((r) => r.data),

  register: (data: RegisterRequest) =>
    api.post<AuthResponse>('/auth/register', data).then((r) => r.data),

  refresh: (refreshToken: string) =>
    api.post<AuthResponse>('/auth/refresh', { refreshToken }).then((r) => r.data),

  inviteWorker: (data: InviteWorkerRequest) =>
    api.post<AuthResponse>('/auth/invite-worker', data).then((r) => r.data),

  logout: (refreshToken: string) =>
    api.post('/auth/logout', { refreshToken }),

  me: () =>
    api.get('/auth/me').then((r) => r.data),
};
