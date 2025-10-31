export interface SalaryStructure {
  id: number;
  employeeId: number;
  employeeName: string;
  basicSalary: number;
  hra: number;
  dearnessAllowance: number;
  providentFund: number;
  otherAllowances: number;
  grossSalary: number;
  netSalary: number;
  effectiveFrom: Date;
  isActive: boolean;
  createdAt: Date;
}

export interface SalaryStructureRequest {
  employeeId: number;
  basicSalary: number;
  hra: number;
  dearnessAllowance: number;
  providentFund: number;
  otherAllowances: number;
  effectiveFrom: Date;
}

export interface SalaryPayment {
  id: number;
  employeeId: number;
  employeeName: string;
  amount: number;
  month: string;
  year: number;
  paymentDate: Date;
  status: PaymentStatus;
  transactionId: string;
  salarySlipUrl?: string;
  basicSalary: number;
  hra: number;
  dearnessAllowance: number;
  providentFund: number;
  otherAllowances: number;
  grossSalary: number;
  netSalary: number;
  createdAt: Date;
}

export interface SalaryPaymentHistory {
  id: number;
  month: string;
  year: number;
  amount: number;
  netSalary: number;
  paymentDate: Date;
  status: PaymentStatus;
  salarySlipUrl?: string;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}


export interface EmployeeSalaryOverview {
  employeeId: number;
  employeeName: string;
  department: string;
  designation: string;
  currentSalary: number;
  hasActiveSalary: boolean;
}