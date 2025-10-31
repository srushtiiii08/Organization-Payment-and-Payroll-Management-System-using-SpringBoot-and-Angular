import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-vendor-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent, SidebarComponent, LoaderComponent],
  templateUrl: './vendor-create.component.html',
  styleUrls: ['./vendor-create.component.css']
})
export class VendorCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private alertService = inject(AlertService);

  vendorForm!: FormGroup;
  loading = false;

  serviceTypes = [
    'IT Services',
    'Cleaning Services',
    'Security Services',
    'Catering',
    'Maintenance',
    'Consulting',
    'Transportation',
    'Other'
  ];

  ngOnInit(): void {
    this.vendorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      address: ['', [Validators.required]],
      serviceType: ['', [Validators.required]],
      bankAccountNumber: ['', [Validators.required, Validators.pattern('^[0-9]{9,18}$')]],
      bankName: ['', [Validators.required]],
      ifscCode: ['', [Validators.required, Validators.pattern('^[A-Z]{4}0[A-Z0-9]{6}$')]],
      panNumber: ['', [Validators.required, Validators.pattern('^[A-Z]{5}[0-9]{4}[A-Z]{1}$')]],
      gstNumber: ['', [Validators.pattern('^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$')]],
      contractStartDate: ['', [Validators.required]],
      contractEndDate: ['']
    });
  }

  get f() {
    return this.vendorForm.controls;
  }

  onSubmit(): void {
  if (this.vendorForm.invalid) {
    Object.keys(this.vendorForm.controls).forEach(key => {
      this.vendorForm.get(key)?.markAsTouched();
    });
    return;
  }

  this.loading = true;

  // ✅ Format the payload properly
  const payload = {
    ...this.vendorForm.value,
    contractStartDate: this.vendorForm.value.contractStartDate,   // Ensure dates are in YYYY-MM-DD format
    contractEndDate: this.vendorForm.value.contractEndDate || null,
    // Ensure empty strings are null
    gstNumber: this.vendorForm.value.gstNumber || null
  };

  // ✅ Log the data being sent for debugging
  console.log('📤 Sending vendor data:', this.vendorForm.value);

  this.http.post(`${environment.apiUrl}/org/vendors`, payload).subscribe({
    next: () => {
      this.loading = false;
      this.alertService.success('Vendor created successfully');
      this.router.navigate(['/organization/vendors']);
    },
    error: (error) => {
      this.loading = false;
      console.error('❌ Full Error Object:', error);
      console.error('❌ Error Response:', error.error);
      
      if (error.error?.errors && Array.isArray(error.error.errors)) {
        const errorMessages = error.error.errors
          .map((err: any) => `• ${err.field}: ${err.message}`)
          .join('\n');
        alert(`❌ Validation Errors:\n\n${errorMessages}`);
      } else if (error.error?.message) {
        alert(`❌ Error: ${error.error.message}`);
      } else {
        alert('❌ Failed to create vendor. Check console for details.');
      }
    }
  });
  }

  cancel(): void {
    if (confirm('Are you sure? Any unsaved changes will be lost.')) {
      this.router.navigate(['/organization/vendors']);
    }
  }
}