export interface PaymentRequest {
  id: number;
  organizationId: number;
  organizationName: string;
  requestType: PaymentRequestType;
  totalAmount: number;
  employeeCount?: number;
  month: string;
  year: number;
  status: PaymentRequestStatus;
  remarks?: string;
  rejectionReason?: string;
  approvedBy?: number;
  approvedByName?: string;
  approvedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

export interface PaymentRequestList {
  id: number;
  organizationName: string;
  requestType: PaymentRequestType;
  totalAmount: number;
  month: string;
  year: number;
  status: PaymentRequestStatus;
  createdAt: Date;
}

export interface PaymentRequestRequest {
  requestType: PaymentRequestType;
  totalAmount: number;
  employeeCount?: number;
  month: string;
  year: number;
  remarks?: string;
}

export enum PaymentRequestType {
  SALARY_DISBURSEMENT = 'SALARY_DISBURSEMENT',
  VENDOR_PAYMENT = 'VENDOR_PAYMENT'
}

export enum PaymentRequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED'
}

export interface PaymentRequestSummary {
  total: number;
  pending: number;
  approved: number;
  rejected: number;
  completed: number;
}