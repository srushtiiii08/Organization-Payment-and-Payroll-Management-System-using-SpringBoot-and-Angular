import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-employee-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent, LoaderComponent, SidebarComponent],
  templateUrl: './employee-edit.component.html',
  styleUrls: ['./employee-edit.component.css']
})
export class EmployeeEditComponent implements OnInit {
  // Inject services using Angular's new inject() function
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private alertService = inject(AlertService);

  employeeForm!: FormGroup;
  loading = false;
  employeeId!: number;

  minDate: string = '';
  maxDate: string = '';

  // Same dropdown options as create form
  departments = ['IT', 'HR', 'Finance', 'Operations', 'Sales', 'Marketing', 'Other'];
  designations = ['Manager', 'Senior Developer', 'Developer', 'Analyst', 'Associate', 'Executive', 'Other'];

  ngOnInit(): void {
    

    // Get the employee ID from the URL parameter
    // Example: /organization/employees/edit/7 → employeeId = 7
    this.employeeId = Number(this.route.snapshot.paramMap.get('id'));
    
    // Initialize the form with validation rules (same as create)
    this.initializeForm();
    
    // Load existing employee data from backend
    this.loadEmployeeData();
  }

  initializeForm(): void {
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

  loadEmployeeData(): void {
    this.loading = true;
    
    // Call backend API: GET /api/org/employees/{id}
    this.http.get(`${environment.apiUrl}/org/employees/${this.employeeId}`).subscribe({
      next: (employee: any) => {
        // Format date from backend (LocalDate) to HTML input format (yyyy-MM-dd)
        const formattedDate = employee.dateOfJoining 
          ? new Date(employee.dateOfJoining).toISOString().split('T')[0] 
          : '';

        // Fill the form with existing employee data
        this.employeeForm.patchValue({
          name: employee.name,
          email: employee.email,
          phone: employee.phone,
          address: employee.address,
          department: employee.department,
          designation: employee.designation,
          dateOfJoining: formattedDate,
          bankAccountNumber: employee.bankAccountNumber,
          bankName: employee.bankName,
          ifscCode: employee.ifscCode
        });
        
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employee:', error);
        this.alertService.error('Failed to load employee data');
        this.loading = false;
        // Redirect back to list if employee not found
        this.router.navigate(['/organization/employees']);
      }
    });
  }

  // Getter for easy access to form controls in template
  get f() {
    return this.employeeForm.controls;
  }

  onSubmit(): void {
    // Mark all fields as touched to show validation errors
    if (this.employeeForm.invalid) {
      Object.keys(this.employeeForm.controls).forEach(key => {
        this.employeeForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;

    // Call backend API: PUT /api/org/employees/{id}
    this.http.put(`${environment.apiUrl}/org/employees/${this.employeeId}`, this.employeeForm.value).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('Employee updated successfully!');
        // Redirect to detail page after successful update
        this.router.navigate(['/organization/employees', this.employeeId]);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error updating employee:', error);
        this.alertService.error('Failed to update employee');
      }
    });
  }

  cancel(): void {
    // Confirm before discarding changes
    if (confirm('Are you sure? Any unsaved changes will be lost.')) {
      this.router.navigate(['/organization/employees', this.employeeId]);
    }
  }
}