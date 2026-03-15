// ── Auth ──
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  organizationName: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserInfo;
}

export interface UserInfo {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  organizationId: number;
  organizationName: string;
}

export type UserRole = 'OWNER' | 'ADMIN' | 'WORKER';

// ── Jobs ──
export type JobStatus =
  | 'LEAD'
  | 'ESTIMATED'
  | 'CONTRACTED'
  | 'IN_PROGRESS'
  | 'ON_HOLD'
  | 'COMPLETED'
  | 'CANCELLED';

export interface JobRequest {
  title: string;
  description?: string;
  clientId?: number;
  status?: JobStatus;
  siteAddress?: string;
  siteCity?: string;
  siteState?: string;
  siteZip?: string;
  siteUnit?: string;
  contractPrice?: number;
  startDate?: string;
  estimatedEndDate?: string;
  actualEndDate?: string;
  clientName?: string;
  clientPhone?: string;
  clientEmail?: string;
}

export interface JobResponse {
  id: number;
  title: string;
  description?: string;
  status: JobStatus;
  startDate?: string;
  estimatedEndDate?: string;
  actualEndDate?: string;
  contractPrice?: number;
  siteAddress?: string;
  siteCity?: string;
  siteState?: string;
  siteZip?: string;
  siteUnit?: string;
  clientId?: number;
  clientName?: string;
  clientPhone?: string;
  clientEmail?: string;
  laborCost?: number;
  materialsCost?: number;
  permitFees?: number;
  profit?: number;
  createdByName?: string;
  createdAt: string;
  updatedAt: string;
}

// ── Workers ──
export type WorkerStatus = 'ACTIVE' | 'INACTIVE';

export interface WorkerRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
  trade?: string;
  hourlyRate?: number;
  status?: string;
}

export interface WorkerJobAssignment {
  jobId: number;
  jobTitle: string;
  roleOnJob?: string;
  assignmentStatus: string;
}

export interface WorkerResponse {
  id: number;
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
  trade?: string;
  hourlyRate?: number;
  status: WorkerStatus;
  currentJobs: WorkerJobAssignment[];
}

// ── Crew Assignments ──
export interface CrewAssignmentRequest {
  workerId: number;
  roleOnJob?: string;
  startDate?: string;
  endDate?: string;
}

export interface CrewAssignmentResponse {
  id: number;
  jobId: number;
  jobTitle: string;
  workerId: number;
  workerName: string;
  workerTrade?: string;
  workerHourlyRate?: number;
  roleOnJob?: string;
  startDate?: string;
  endDate?: string;
  status: string;
  totalHours?: number;
}

// ── Time Entries ──
export interface TimeEntryRequest {
  workerId: number;
  entryDate: string;
  hours: number;
  clockIn?: string;
  clockOut?: string;
  notes?: string;
}

export interface TimeEntryResponse {
  id: number;
  jobId: number;
  jobTitle?: string;
  workerId: number;
  workerName: string;
  entryDate: string;
  hours: number;
  clockIn?: string;
  clockOut?: string;
  enteredByName?: string;
  notes?: string;
  createdAt: string;
}

// ── Permits ──
export type PermitStatus = 'PENDING' | 'ACTIVE' | 'EXPIRED' | 'RENEWED';

export interface PermitRequest {
  permitType: string;
  permitNumber?: string;
  issuingAuthority?: string;
  status?: PermitStatus;
  fee?: number;
  applicationDate?: string;
  issueDate?: string;
  expirationDate?: string;
  reminderDaysBefore?: number;
  notes?: string;
}

export interface PermitResponse {
  id: number;
  jobId: number;
  permitType: string;
  permitNumber?: string;
  issuingAuthority?: string;
  status: string;
  fee?: number;
  applicationDate?: string;
  issueDate?: string;
  expirationDate?: string;
  reminderDaysBefore?: number;
  notes?: string;
  createdAt: string;
}

// ── Materials ──
export interface MaterialRequest {
  name: string;
  quantity: number;
  unitCost: number;
  receiptDocumentId?: number;
}

export interface MaterialResponse {
  id: number;
  jobId: number;
  name: string;
  quantity: number;
  unitCost: number;
  total: number;
  receiptDocumentId?: number;
  receiptFileName?: string;
  createdAt: string;
}

// ── Job Notes ──
export type NoteVisibility = 'SHARED' | 'OWNER_ONLY';

export interface JobNoteRequest {
  content: string;
  visibility?: NoteVisibility;
}

export interface JobNoteResponse {
  id: number;
  jobId: number;
  authorId: number;
  authorName: string;
  content: string;
  visibility: string;
  createdAt: string;
}

// ── Invite ──
export interface InviteWorkerRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  trade?: string;
  hourlyRate?: number;
}
