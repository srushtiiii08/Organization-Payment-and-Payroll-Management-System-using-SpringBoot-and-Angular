export interface Employee {
  id: number;
  organizationId: number;
  organizationName: string;
  userId: number;
  name: string;
  email: string;
  phone: string;
  address: string;
  department: string;
  designation: string;
  dateOfJoining: Date;
  bankAccountNumber: string;
  bankName: string;
  ifscCode: string;
  accountProofUrl?: string;
  accountVerificationStatus: AccountVerificationStatus;
  status: EmployeeStatus;
  createdAt: Date;
  updatedAt: Date;
}

export interface EmployeeList {
  id: number;
  name: string;
  email: string;
  department: string;
  designation: string;
  dateOfJoining: Date;
  status: EmployeeStatus;
  accountVerificationStatus: AccountVerificationStatus;
  currentSalary?: number;
}

export interface EmployeeProfile {
  id: number;
  name: string;
  email: string;
  phone: string;
  address: string;
  department: string;
  designation: string;
  dateOfJoining: Date;
  bankAccountNumber: string;
  bankName: string;
  ifscCode: string;
  accountVerificationStatus: AccountVerificationStatus;
  currentSalary?: number;
  organizationName: string;
  profilePictureUrl?: string;
}

export interface EmployeeRequest {
  name: string;
  email: string;
  phone: string;
  address: string;
  department: string;
  designation: string;
  dateOfJoining: Date;
  bankAccountNumber: string;
  bankName: string;
  ifscCode: string;
}

export enum AccountVerificationStatus {
  PENDING = 'PENDING',
  VERIFIED = 'VERIFIED',
  REJECTED = 'REJECTED'
}

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  TERMINATED = 'TERMINATED',
  ON_LEAVE = 'ON_LEAVE'
}