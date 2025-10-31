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
  selector: 'app-salary-structure-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent, LoaderComponent, SidebarComponent],
  templateUrl: './salary-structure-create.component.html',
  styleUrls: ['./salary-structure-create.component.css']
})
export class SalaryStructureCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private alertService = inject(AlertService);

  salaryForm!: FormGroup;
  loading = false;
  employeeId!: number;
  employeeName = '';
  minDate!: string;

  // Calculated values
  grossSalary = 0;
  netSalary = 0;

  ngOnInit(): void {
    this.employeeId = Number(this.route.snapshot.paramMap.get('employeeId'));
    
    // SET MINIMUM DATE TO TODAY
    this.minDate = this.getTodayDate();
    
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

    this.loadEmployeeDetails();
  }

  //Get today's date in yyyy-MM-dd format
  getTodayDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  //Custom validator for future dates
  futureDateValidator(control: any): { [key: string]: any } | null {
    if (!control.value) {
      return null; // Don't validate empty value
    }

    const selectedDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Reset time to start of day

    if (selectedDate < today) {
      return { pastDate: true };
    }

    return null;
  }


  loadEmployeeDetails(): void {
    this.http.get<any>(`${environment.apiUrl}/org/employees/${this.employeeId}`).subscribe({
      next: (employee) => {
        this.employeeName = employee.name;
      },
      error: (error) => {
        console.error('Error loading employee:', error);
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

      //SHOW SPECIFIC ERROR FOR PAST DATE
      if (this.salaryForm.get('effectiveFrom')?.hasError('pastDate')) {
        this.alertService.error('Cannot create salary structure with past dates. Please select today or a future date.');
      } else {
        this.alertService.error('Please fill all required fields correctly');
      }
      return;
    }

    this.loading = true;

    const payload = {
      ...this.salaryForm.value,
      employeeId: this.employeeId
    };

    this.http.post(
      `${environment.apiUrl}/org/salary-structure/employee/${this.employeeId}`, 
      payload
    ).subscribe({
      next: () => {
        this.loading = false;
        this.alertService.success('Salary structure created successfully');
        this.router.navigate(['/organization/salary-structure/employee', this.employeeId]);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error creating salary structure:', error);

        //bETTER ERROR HANDLING
        if (error.error?.message) {
          this.alertService.error(error.error.message);
        } else if (error.status === 500) {
          this.alertService.error('Server error. Please try again or contact support.');
        } else {
          this.alertService.error('Failed to create salary structure');
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