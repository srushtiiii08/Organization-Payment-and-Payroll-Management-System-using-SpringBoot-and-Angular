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

@Component({
  selector: 'app-employee-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent, LoaderComponent, SidebarComponent],
  templateUrl: './employee-create.component.html',
  styleUrls: ['./employee-create.component.css']
})
export class EmployeeCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private alertService = inject(AlertService);

  minDate: string = '';
  maxDate: string = '';

  employeeForm!: FormGroup;
  loading = false;

  departments = ['IT', 'HR', 'Finance', 'Operations', 'Sales', 'Marketing', 'Other'];
  designations = ['Manager', 'Senior Developer', 'Developer', 'Analyst', 'Associate', 'Executive', 'Other'];

  ngOnInit(): void {
    // Set date constraints
    const today = new Date();
    const maxDate = new Date();
    maxDate.setDate(today.getDate() + 30);
  
    this.minDate = today.toISOString().split('T')[0];
    this.maxDate = maxDate.toISOString().split('T')[0];
    
    this.employeeForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      address: ['', [Validators.required]],
      department: ['', [Validators.required]],
      designation: ['', [Validators.required]],
      dateOfJoining: ['', [Validators.required]],
      bankAccountNumber: ['', [Validators.required, Validators.pattern('^[0-9]{9,18}$')]],
      bankName: ['', [Validators.required]],
      ifscCode: ['', [Validators.required, Validators.pattern('^[A-Z]{4}0[A-Z0-9]{6}$')]]
    });
  }

  get f() {
    return this.employeeForm.controls;
  }

  onSubmit(): void {
    if (this.employeeForm.invalid) {
      Object.keys(this.employeeForm.controls).forEach(key => {
        this.employeeForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;

    this.http.post(`${environment.apiUrl}/org/employees`, this.employeeForm.value).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('Employee created successfully! Invitation email sent.');
        this.router.navigate(['/organization/employees']);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error creating employee:', error);
      }
    });
  }

  cancel(): void {
    if (confirm('Are you sure? Any unsaved changes will be lost.')) {
      this.router.navigate(['/organization/employees']);
    }
  }
}