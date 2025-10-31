import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { CloudinaryService } from '../../../core/services/cloudinary.service';

@Component({
  selector: 'app-register-employee',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register-employee.component.html',
  styleUrls: ['./register-employee.component.css']
})
export class RegisterEmployeeComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertService = inject(AlertService);
  private cloudinaryService = inject(CloudinaryService); 

  registerForm!: FormGroup;
  loading = false;
  showPassword = false;
  showConfirmPassword = false;

  //File upload properties
  selectedFile: File | null = null;
  uploading = false;
  documentUrl: string = '';

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required, 
        Validators.minLength(8),
        this.passwordStrengthValidator
      ]],
      confirmPassword: ['', [Validators.required]],
      documentProofUrl: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  //File selection handler
  onFileSelected(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      const file = target.files[0];
      
      // Validate file
      const validation = this.cloudinaryService.validateFile(file, 5, [
        'image/jpeg', 'image/png', 'image/jpg', 'application/pdf'
      ]);
      
      if (!validation.valid) {
        this.alertService.error(validation.error!);
        return;
      }
      
      this.selectedFile = file;
      this.uploadDocument();
    }
  }

  //Upload document to Cloudinary
  uploadDocument(): void {
    if (!this.selectedFile) return;
    
    this.uploading = true;
    
    this.cloudinaryService.uploadDocument(this.selectedFile, 'employee_documents').subscribe({
      next: (url) => {
        this.documentUrl = url;
        this.registerForm.patchValue({ documentProofUrl: url });
        this.uploading = false;
        this.alertService.success('Document uploaded successfully!');
      },
      error: (error) => {
        this.uploading = false;
        this.alertService.error('Failed to upload document. Please try again.');
        console.error('Upload error:', error);
      }
    });
  }

  //Remove uploaded document
  removeDocument(): void {
    this.selectedFile = null;
    this.documentUrl = '';
    this.registerForm.patchValue({ documentProofUrl: '' });
    
    // Reset file input
    const fileInput = document.getElementById('documentFile') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }
  
  // Custom validator for password strength
  passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    const hasNumber = /[0-9]/.test(value);
    const hasUpper = /[A-Z]/.test(value);
    const hasLower = /[a-z]/.test(value);
    const hasSpecial = /[@#$%^&+=]/.test(value);

    const valid = hasNumber && hasUpper && hasLower && hasSpecial;

    if (!valid) {
      return {
        passwordStrength: {
          hasNumber,
          hasUpper,
          hasLower,
          hasSpecial
        }
      };
    }

    return null;
  }

  // Custom validator to check if passwords match
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) return null;

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  get f() {
    return this.registerForm.controls;
  }

  togglePasswordVisibility(field: 'password' | 'confirmPassword'): void {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;

    this.authService.registerEmployee(this.registerForm.value).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('Registration completed successfully! Please login with your credentials.');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.loading = false;
        console.error('Registration failed:', error);
      }
    });
  }
}