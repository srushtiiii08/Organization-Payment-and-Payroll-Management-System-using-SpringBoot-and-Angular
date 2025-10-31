import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { LoginRequest, CaptchaResponse } from '../../../shared/models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoaderComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertService = inject(AlertService);

  loginForm!: FormGroup;
  loading = false;
  showPassword = false;

  // 🆕 CAPTCHA PROPERTIES
  captchaImageData = '';
  captchaSessionId = '';
  loadingCaptcha = false;

  ngOnInit(): void {
    // Initialize form with CAPTCHA fields
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      captchaAnswer: ['', [Validators.required]] // 🆕 ADDED
    });

    // 🆕 LOAD CAPTCHA ON INIT
    this.loadCaptcha();
  }

  // 🆕 NEW METHOD: Load CAPTCHA
  loadCaptcha(): void {
    this.loadingCaptcha = true;
    
    this.authService.generateCaptcha().subscribe({
      next: (response: CaptchaResponse) => {
        this.captchaSessionId = response.sessionId;
        this.captchaImageData = response.imageData;
        this.loadingCaptcha = false;
      },
      error: (error) => {
        console.error('Failed to load CAPTCHA:', error);
        this.alertService.error('Failed to load CAPTCHA. Please refresh the page.');
        this.loadingCaptcha = false;
      }
    });
  }

  // 🆕 NEW METHOD: Refresh CAPTCHA
  refreshCaptcha(): void {
    this.loginForm.patchValue({ captchaAnswer: '' }); // Clear answer
    this.loadCaptcha();
  }

  // Getter for easy access to form controls in template
  get f() {
    return this.loginForm.controls;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    // Mark all fields as touched to show validation errors
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    // 🆕 CHECK IF CAPTCHA IS LOADED
    if (!this.captchaSessionId) {
      this.alertService.error('CAPTCHA not loaded. Please refresh.');
      return;
    }

    this.loading = true;

    // 🆕 BUILD LOGIN REQUEST WITH CAPTCHA
    const loginRequest: LoginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password,
      captchaSessionId: this.captchaSessionId,
      captchaAnswer: this.loginForm.value.captchaAnswer
    };

    this.authService.login(loginRequest).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('Login successful!');
        
        // Redirect based on role
        switch (response.role) {
          case 'BANK_ADMIN':
            this.router.navigate(['/admin/dashboard']);
            break;
          case 'ORGANIZATION':
            this.router.navigate(['/organization/dashboard']);
            break;
          case 'EMPLOYEE':
            this.router.navigate(['/employee/dashboard']);
            break;
          default:
            this.router.navigate(['/']);
        }
      },
      error: (error) => {
        this.loading = false;
        
        // 🆕 REFRESH CAPTCHA ON ERROR
        this.refreshCaptcha();
        
        // Show error message
        const errorMsg = error?.error?.message || 'Login failed. Please try again.';
        this.alertService.error(errorMsg);
        
        console.error('Login failed:', error);
      }
    });
  }
}