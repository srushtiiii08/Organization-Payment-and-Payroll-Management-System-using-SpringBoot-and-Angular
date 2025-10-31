import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { EmployeeList } from '../../../shared/models/employee.model';
import { VendorList } from '../../../shared/models/vendor.model';
import { PaymentRequestType } from '../../../shared/models/payment-request.model';

@Component({
  selector: 'app-payment-request-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent, SidebarComponent, LoaderComponent],
  templateUrl: './payment-request-create.component.html',
  styleUrls: ['./payment-request-create.component.css']
})
export class PaymentRequestCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private alertService = inject(AlertService);

  requestForm!: FormGroup;
  loading = false;
  
  // ⭐ Payment Type Selection
  selectedPaymentType: PaymentRequestType = PaymentRequestType.SALARY_DISBURSEMENT;
  PaymentRequestType = PaymentRequestType; // Make enum available in template

  // Salary Disbursement Data
  employees: EmployeeList[] = [];
  selectedEmployees: Set<number> = new Set();
  totalSalaryAmount = 0;

  // Vendor Payment Data
  vendors: VendorList[] = [];
  selectedVendor: VendorList | null = null;

  // Common Data
  months = [
    'JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE',
    'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER'
  ];
  years: number[] = [];
  currentYear = new Date().getFullYear();
  currentMonth = new Date().getMonth();

  ngOnInit(): void {
    // Generate years (current year and 2 years back)
    for (let i = 0; i < 3; i++) {
      this.years.push(this.currentYear - i);
    }

    this.initializeForm();
    this.loadEmployees();
    this.loadVendors();
  }

  initializeForm(): void {
    this.requestForm = this.fb.group({
      // Common fields
      month: [this.months[this.currentMonth], [Validators.required]],
      year: [this.currentYear, [Validators.required]],
      remarks: [''],
      
      // Vendor-specific fields
      vendorAmount: ['', [Validators.min(1)]],
      invoiceNumber: [''],
      invoiceDate: [''],
      description: ['']
    });
  }

  // ⭐ SWITCH PAYMENT TYPE
  selectPaymentType(type: PaymentRequestType): void {
    this.selectedPaymentType = type;
    
    // Reset selections
    this.selectedEmployees.clear();
    this.selectedVendor = null;
    this.totalSalaryAmount = 0;
    
    // Update form validators based on type
    if (type === PaymentRequestType.VENDOR_PAYMENT) {
      this.requestForm.get('vendorAmount')?.setValidators([Validators.required, Validators.min(1)]);
      this.requestForm.get('invoiceNumber')?.setValidators([Validators.required]);
      this.requestForm.get('invoiceDate')?.setValidators([Validators.required]);
    } else {
      this.requestForm.get('vendorAmount')?.clearValidators();
      this.requestForm.get('invoiceNumber')?.clearValidators();
      this.requestForm.get('invoiceDate')?.clearValidators();
    }
    
    this.requestForm.get('vendorAmount')?.updateValueAndValidity();
    this.requestForm.get('invoiceNumber')?.updateValueAndValidity();
    this.requestForm.get('invoiceDate')?.updateValueAndValidity();
  }

  // ========================================
  // EMPLOYEE MANAGEMENT (Salary Disbursement)
  // ========================================

  loadEmployees(): void {
    this.loading = true;
    this.http.get<EmployeeList[]>(`${environment.apiUrl}/org/employees`).subscribe({
      next: (data) => {
        
        // ⭐ LOG EACH EMPLOYEE'S ELIGIBILITY
        data.forEach(emp => {
          console.log(`Employee: ${emp.name}`);
          console.log(`  - Status: ${emp.status} ${(emp.status === 'ACTIVE' || emp.status === 'ON_LEAVE') ? '✅' : '❌'}`);
          console.log(`  - Account Verified: ${emp.accountVerificationStatus} ${emp.accountVerificationStatus === 'VERIFIED' ? '✅' : '❌'}`);
          console.log(`  - Has Salary: ₹${emp.currentSalary || 'NONE'} ${emp.currentSalary && emp.currentSalary > 0 ? '✅' : '❌'}`);
          console.log('  ---');
        });

        // ⭐ Filter: ACTIVE or ON_LEAVE employees with VERIFIED accounts and salary
        this.employees = data.filter(emp => {
        const statusOk = emp.status === 'ACTIVE' || emp.status === 'ON_LEAVE';
        const accountOk = emp.accountVerificationStatus === 'VERIFIED';
        const salaryOk = emp.currentSalary && emp.currentSalary > 0;
        
        const eligible = statusOk && accountOk && salaryOk;
        
        if (!eligible) {
          console.log(`❌ ${emp.name} EXCLUDED:`);
          if (!statusOk) console.log(`   - Status is ${emp.status} (need ACTIVE or ON_LEAVE)`);
          if (!accountOk) console.log(`   - Account status is ${emp.accountVerificationStatus} (need VERIFIED)`);
          if (!salaryOk) console.log(`   - No active salary structure`);
        }
        return eligible;
      });

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employees:', error);
        this.alertService.error('Failed to load employees');
        this.loading = false;
      }
    });
  }

  toggleEmployee(employeeId: number): void {
    if (this.selectedEmployees.has(employeeId)) {
      this.selectedEmployees.delete(employeeId);
    } else {
      this.selectedEmployees.add(employeeId);
    }
    this.calculateTotalSalary();
  }

  selectAllEmployees(): void {
    if (this.selectedEmployees.size === this.employees.length) {
      this.selectedEmployees.clear();
    } else {
      this.employees.forEach(emp => this.selectedEmployees.add(emp.id));
    }
    this.calculateTotalSalary();
  }

  calculateTotalSalary(): void {
    this.totalSalaryAmount = 0;
    this.selectedEmployees.forEach(empId => {
      const employee = this.employees.find(e => e.id === empId);
      if (employee && employee.currentSalary) {
        this.totalSalaryAmount += employee.currentSalary;
      }
    });
  }

  // ========================================
  // VENDOR MANAGEMENT (Vendor Payment)
  // ========================================

  loadVendors(): void {
    this.http.get<VendorList[]>(`${environment.apiUrl}/org/vendors`).subscribe({
      next: (data) => {
        // ⭐ Filter: Only ACTIVE vendors
        this.vendors = data.filter(v => v.status === 'ACTIVE');
      },
      error: (error) => {
        console.error('Error loading vendors:', error);
        this.alertService.error('Failed to load vendors');
      }
    });
  }

  selectVendor(vendor: VendorList): void {
    this.selectedVendor = vendor;
  }

  // ========================================
  // FORM SUBMISSION
  // ========================================

  get f() {
    return this.requestForm.controls;
  }

  onSubmit(): void {
    // Validate based on payment type
    if (this.selectedPaymentType === PaymentRequestType.SALARY_DISBURSEMENT) {
      if (this.selectedEmployees.size === 0) {
        this.alertService.error('Please select at least one employee');
        return;
      }
    } else {
      if (!this.selectedVendor) {
        this.alertService.error('Please select a vendor');
        return;
      }
      
      if (this.requestForm.invalid) {
        Object.keys(this.requestForm.controls).forEach(key => {
          this.requestForm.get(key)?.markAsTouched();
        });
        this.alertService.error('Please fill all required fields');
        return;
      }
    }

    this.loading = true;

    let payload: any;

    if (this.selectedPaymentType === PaymentRequestType.SALARY_DISBURSEMENT) {
      // ⭐ Salary Payment Payload
      payload = {
        requestType: PaymentRequestType.SALARY_DISBURSEMENT,
        month: this.requestForm.value.month,
        year: this.requestForm.value.year,
        totalAmount: this.totalSalaryAmount,
        employeeCount: this.selectedEmployees.size,
        remarks: this.requestForm.value.remarks || null
      };
    } else {
      // ⭐ Vendor Payment Payload
      payload = {
        requestType: PaymentRequestType.VENDOR_PAYMENT,
        month: this.requestForm.value.month,
        year: this.requestForm.value.year,
        totalAmount: parseFloat(this.requestForm.value.vendorAmount),
        employeeCount: null,
        remarks: `Vendor: ${this.selectedVendor!.name} | Invoice: ${this.requestForm.value.invoiceNumber} | ${this.requestForm.value.description || ''}`
      };
    }

    console.log('📤 Submitting payment request:', payload);

    this.http.post(`${environment.apiUrl}/org/payment-requests`, payload).subscribe({
      next: (response) => {
        console.log('✅ Payment request created:', response);
        this.loading = false;
        this.alertService.success('Payment request submitted successfully');
        this.router.navigate(['/organization/payment-requests']);
      },
      error: (error) => {
        this.loading = false;
        console.error('❌ Error creating payment request:', error);
        
        if (error.error?.message) {
          this.alertService.error(error.error.message);
        } else {
          this.alertService.error('Failed to create payment request');
        }
      }
    });
  }

  cancel(): void {
    if (confirm('Are you sure? Any unsaved changes will be lost.')) {
      this.router.navigate(['/organization/payment-requests']);
    }
  }
}