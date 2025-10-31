import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-salary-structure-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent, LoaderComponent, SidebarComponent],
  templateUrl: './salary-structure-edit.component.html',
  styleUrls: ['./salary-structure-edit.component.css']
})
export class SalaryStructureEditComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private alertService = inject(AlertService);

  salaryForm!: FormGroup;
  loading = false;
  salaryId!: number;
  employeeId!: number;
  employeeName = '';

  // Calculated values
  grossSalary = 0;
  netSalary = 0;

  ngOnInit(): void {
    this.salaryId = Number(this.route.snapshot.paramMap.get('id'));
    
    this.salaryForm = this.fb.group({
      basicSalary: ['', [Validators.required, Validators.min(1)]],
      hra: ['', [Validators.required, Validators.min(0)]],
      dearnessAllowance: ['', [Validators.required, Validators.min(0)]],
      otherAllowances: [0, [Validators.min(0)]],
      providentFund: ['', [Validators.required, Validators.min(0)]],
      effectiveFrom: ['', [Validators.required]]
    });

    // Auto-calculate on value changes
    this.salaryForm.valueChanges.subscribe(() => {
      this.calculateSalary();
    });

    this.loadSalaryStructure();
  }

  loadSalaryStructure(): void {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/org/salary-structure/${this.salaryId}`).subscribe({
      next: (salary) => {
        this.employeeId = salary.employeeId;
        this.employeeName = salary.employeeName;
        
        // Format date for input field
        const formattedDate = salary.effectiveFrom 
          ? new Date(salary.effectiveFrom).toISOString().split('T')[0] 
          : '';

        this.salaryForm.patchValue({
          basicSalary: salary.basicSalary,
          hra: salary.hra,
          dearnessAllowance: salary.dearnessAllowance,
          otherAllowances: salary.otherAllowances,
          providentFund: salary.providentFund,
          effectiveFrom: formattedDate
        });

        this.calculateSalary();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading salary structure:', error);
        this.alertService.error('Failed to load salary structure');
        this.loading = false;
        this.router.navigate(['/organization/salary-structure']);
      }
    });
  }

  calculateSalary(): void {
    const values = this.salaryForm.value;
    
    const basic = parseFloat(values.basicSalary) || 0;
    const hra = parseFloat(values.hra) || 0;
    const da = parseFloat(values.dearnessAllowance) || 0;
    const other = parseFloat(values.otherAllowances) || 0;
    const pf = parseFloat(values.providentFund) || 0;

    this.grossSalary = basic + hra + da + other;
    this.netSalary = this.grossSalary - pf;
  }

  get f() {
    return this.salaryForm.controls;
  }

  onSubmit(): void {
  if (this.salaryForm.invalid) {
    Object.keys(this.salaryForm.controls).forEach(key => {
      this.salaryForm.get(key)?.markAsTouched();
    });
    return;
  }

  this.loading = true;

  // ⭐ CREATE PROPER PAYLOAD WITH employeeId
  const payload = {
    employeeId: this.employeeId,  // ⭐ ADD THIS - Backend needs it
    basicSalary: parseFloat(this.salaryForm.value.basicSalary),
    hra: parseFloat(this.salaryForm.value.hra),
    dearnessAllowance: parseFloat(this.salaryForm.value.dearnessAllowance),
    otherAllowances: parseFloat(this.salaryForm.value.otherAllowances || 0),
    providentFund: parseFloat(this.salaryForm.value.providentFund),
    effectiveFrom: this.salaryForm.value.effectiveFrom  // Date in yyyy-MM-dd format
  };

  console.log('📤 Sending update payload:', payload);

  this.http.put(
    `${environment.apiUrl}/org/salary-structure/${this.salaryId}`, 
    payload  // ⭐ USE THE PAYLOAD OBJECT
  ).subscribe({
    next: (response) => {
      console.log('✅ Update successful:', response);
      this.loading = false;
      this.alertService.success('Salary structure updated successfully');
      this.router.navigate(['/organization/salary-structure/employee', this.employeeId]);
    },
    error: (error) => {
      this.loading = false;
      console.error('❌ Error updating salary structure:', error);
      
      // ⭐ SHOW DETAILED ERROR MESSAGE
      if (error.error?.errors && error.error.errors.length > 0) {
        const validationErrors = error.error.errors.map((e: any) => e.message).join(', ');
        this.alertService.error(`Validation failed: ${validationErrors}`);
      } else if (error.error?.message) {
        this.alertService.error(error.error.message);
      } else {
        this.alertService.error('Failed to update salary structure');
      }
    }
    });
  }

  cancel(): void {
    if (confirm('Are you sure? Any unsaved changes will be lost.')) {
      this.router.navigate(['/organization/salary-structure/employee', this.employeeId]);
    }
  }
}