export interface Vendor {
  id: number;
  organizationId: number;
  name: string;
  email: string;
  phone: string;
  address: string;
  serviceType: string;
  bankAccountNumber: string;
  bankName: string;
  ifscCode: string;
  panNumber: string;
  gstNumber?: string;
  contractStartDate: Date;
  contractEndDate?: Date;
  status: VendorStatus;
  createdAt: Date;
  updatedAt: Date;
}

export interface VendorList {
  id: number;
  name: string;
  email: string;
  serviceType: string;
  status: VendorStatus;
  contractStartDate: string | Date | number;
}

export interface VendorCreate {
  name: string;
  email: string;
  phone: string;
  address: string;
  serviceType: string;
  bankAccountNumber: string;
  bankName: string;
  ifscCode: string;
  panNumber: string;
  gstNumber?: string;
  contractStartDate: string;
  contractEndDate?: string;
}

export enum VendorStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BLACKLISTED = 'BLACKLISTED'
}