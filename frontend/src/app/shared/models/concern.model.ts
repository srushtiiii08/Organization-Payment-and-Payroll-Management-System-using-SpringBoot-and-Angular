export interface Concern {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeEmail: string;
  organizationId?: number;
  organizationName?: string;
  subject: string;
  description: string;
  status: ConcernStatus;
  priority: ConcernPriority;
  response?: string;
  respondedBy?: number;
  respondedByName?: string;
  respondedAt?: Date;
  attachmentUrl?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface ConcernList {
  id: number;
  subject: string;
  title?: string; //some APIs return 'title' instead of 'subject'
  description?: string; // for displaying in list
  category?: string; // for categorization
  reportedBy?: string; // employee name who reported
  employeeName: string;
  status: ConcernStatus | string;
  priority: ConcernPriority | string;
  createdAt: Date | string;
}

export interface ConcernCreate {
  subject: string;
  description: string;
  priority: ConcernPriority;
  attachment?: File;
}

export interface ConcernResponse {
  response: string;
}

export enum ConcernStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export enum ConcernPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}