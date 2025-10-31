export interface User {
  id: number;
  email: string;
  role: Role;
  status: UserStatus;
  createdAt?: Date;
  updatedAt?: Date;
}

export enum Role {
  BANK_ADMIN = 'BANK_ADMIN',
  ORGANIZATION = 'ORGANIZATION',
  EMPLOYEE = 'EMPLOYEE'
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING = 'PENDING'
}

export interface LoginRequest {
  email: string;
  password: string;
  captchaSessionId: string;  
  captchaAnswer: string;     

}

export interface CaptchaResponse {
  sessionId: string;
  imageData: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: Role;
  userId: number;
  message: string;
}

export interface RegisterOrganizationRequest {
  name: string;
  registrationNumber: string;
  email: string;
  password: string;
  address: string;
  contactPhone: string;
}

// For form data with file upload
// Note: Actual data is sent as FormData, not this interface
// This is just for documentation purposes
export interface RegisterOrganizationFormData {
  organization: RegisterOrganizationRequest;
  file: File;
}

export interface RegisterEmployeeRequest {
  email: string;
  password: string;
  confirmPassword: string;
  documentProofUrl: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}