export interface Organization {
  id: number;
  name: string;
  registrationNumber: string;
  email: string;
  address: string;
  contactPhone: string;
  verified: boolean;
  verificationDocumentsUrl?: string;
  remarks?: string;
  verifiedAt?: Date;
  verifiedBy?: number;
  verifiedByName?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface OrganizationList {
  id: number;
  name: string;
  email: string;
  registrationNumber: string;
  contactPhone: string;
  verified: boolean;
  createdAt: Date;
  userStatus?: string;
}

export interface OrganizationVerificationRequest {
  verified: boolean;
  remarks?: string;
}