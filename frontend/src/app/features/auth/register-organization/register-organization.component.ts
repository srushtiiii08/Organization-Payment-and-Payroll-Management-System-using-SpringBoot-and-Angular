import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-register-organization',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoaderComponent],
  templateUrl: './register-organization.component.html',
  styleUrls: ['./register-organization.component.css']
})
export class RegisterOrganizationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertService = inject(AlertService);

  registerForm!: FormGroup;
  loading = false;
  showPassword = false;
  
  // 🆕 FILE HANDLING PROPERTIES
  selectedFile: File | null = null;
  fileError: string = '';
  
  // 🆕 ALLOWED FILE TYPES AND SIZE
  private readonly ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
  private readonly MAX_SIZE = 10 * 1024 * 1024; // 10MB in bytes

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      registrationNumber: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      address: ['', [Validators.required]],
      contactPhone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]]
    });
  }

  get f() {
    return this.registerForm.controls;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  // 🆕 FILE SELECTION HANDLER
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    this.fileError = '';
    
    if (!file) {
      this.selectedFile = null;
      return;
    }

    // ✅ VALIDATE FILE TYPE
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      this.fileError = 'Invalid file type. Only PDF, JPG, JPEG, and PNG are allowed.';
      this.selectedFile = null;
      event.target.value = ''; // Clear the input
      return;
    }

    // ✅ VALIDATE FILE SIZE
    if (file.size > this.MAX_SIZE) {
      this.fileError = `File size exceeds 10MB. Your file: ${this.getFileSize(file.size)}`;
      this.selectedFile = null;
      event.target.value = ''; // Clear the input
      return;
    }

    // ✅ FILE IS VALID
    this.selectedFile = file;
    console.log('✅ File selected:', file.name, this.getFileSize(file.size));
  }

  // 🆕 FORMAT FILE SIZE FOR DISPLAY
  getFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    else if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    else return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  }

  // 🆕 UPDATED SUBMIT METHOD
  onSubmit(): void {
    // ✅ VALIDATE FORM
    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
      return;
    }

    // ✅ VALIDATE FILE IS SELECTED
    if (!this.selectedFile) {
      this.fileError = 'Verification document is required';
      return;
    }

    this.loading = true;

    // 📦 CREATE FORM DATA (multipart/form-data)
    const formData = new FormData();
    
    // Add JSON data as a Blob with proper content type
    const organizationData = {
      name: this.registerForm.value.name,
      registrationNumber: this.registerForm.value.registrationNumber,
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      address: this.registerForm.value.address,
      contactPhone: this.registerForm.value.contactPhone
    };
    
    formData.append('organization', new Blob([JSON.stringify(organizationData)], {
      type: 'application/json'
    }));
    
    // Add file
    formData.append('file', this.selectedFile);

    console.log('📤 Submitting registration with file:', this.selectedFile.name);

    // 🚀 SEND TO BACKEND
    this.authService.registerOrganization(formData).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('Registration successful! Your account is pending verification.');
        this.router.navigate(['/organization/dashboard']);
      },
      error: (error) => {
        this.loading = false;
        console.error('❌ Registration failed:', error);
        // Error is handled by the interceptor (AlertService)
      }
    });
  }
}