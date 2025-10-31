import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoaderComponent, FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private alertService = inject(AlertService);

  // Forms
  emailForm!: FormGroup;
  otpForm!: FormGroup;
  passwordForm!: FormGroup;

  // State management
  currentStep: 'email' | 'otp' | 'password' = 'email';
  loading = false;
  userEmail = '';

  // OTP input - only for display purposes
  otpDigits: string[] = ['', '', '', '', '', ''];
  
  // Password visibility
  showPassword = false;
  showConfirmPassword = false;

  ngOnInit(): void {
    // Step 1: Email form
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    // Step 2: OTP form
    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]]
    });

    // Step 3: Password form
    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  // Custom validator for password match
  passwordMatchValidator(group: FormGroup) {
    const password = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  // Getters for form controls
  get emailControl() {
    return this.emailForm.controls;
  }

  get passwordControl() {
    return this.passwordForm.controls;
  }

  // STEP 1: Send OTP to email
  sendOtp(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.userEmail = this.emailForm.value.email;

    console.log('📧 Sending OTP to:', this.userEmail);

    this.authService.forgotPassword(this.userEmail).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('OTP sent to your email!');
        this.currentStep = 'otp';
        console.log('✅ OTP sent successfully');
      },
      error: (error) => {
        this.loading = false;
        console.error('❌ Failed to send OTP:', error);
      }
    });
  }

  onOtpInput(index: number, event: any): void {
    const input = event.target as HTMLInputElement;
    let value = input.value;

    // Only allow numeric characters
    value = value.replace(/[^0-9]/g, '');

    // Take only the last character if multiple characters were entered
    if (value.length > 1) {
      value = value.slice(-1);
    }

    // Update the input value directly
    input.value = value;
    this.otpDigits[index] = value;

    // Move to next input if current has value and we're not at the last field
    if (value && index < 5) {
      const nextInput = document.getElementById(`otp-${index + 1}`) as HTMLInputElement;
      if (nextInput) {
        nextInput.focus();
        nextInput.select();
      }
    }

    console.log('Current OTP:', this.otpDigits.join(''));
  }

  onOtpKeydown(index: number, event: KeyboardEvent): void {
    const input = event.target as HTMLInputElement;

    // Handle backspace
    if (event.key === 'Backspace') {
      event.preventDefault();
      
      // Clear current field
      input.value = '';
      this.otpDigits[index] = '';
      
      // Move to previous field
      if (index > 0) {
        const prevInput = document.getElementById(`otp-${index - 1}`) as HTMLInputElement;
        if (prevInput) {
          prevInput.focus();
          prevInput.select();
        }
      }
      return;
    }

    // Handle delete key
    if (event.key === 'Delete') {
      event.preventDefault();
      input.value = '';
      this.otpDigits[index] = '';
      return;
    }

    // Handle arrow keys
    if (event.key === 'ArrowLeft' && index > 0) {
      event.preventDefault();
      const prevInput = document.getElementById(`otp-${index - 1}`) as HTMLInputElement;
      if (prevInput) prevInput.focus();
      return;
    }

    if (event.key === 'ArrowRight' && index < 5) {
      event.preventDefault();
      const nextInput = document.getElementById(`otp-${index + 1}`) as HTMLInputElement;
      if (nextInput) nextInput.focus();
      return;
    }

    // Allow only numeric keys
    if (!/^[0-9]$/.test(event.key) && 
        !['Tab', 'Enter'].includes(event.key)) {
      event.preventDefault();
    }
  }

  // Updated paste handler with better clipboard access
  onOtpPaste(event: ClipboardEvent): void {
    event.preventDefault();
    console.log('Paste event triggered');
    
    // Try to get clipboard data
    let pastedData = '';
    
    if (event.clipboardData) {
      pastedData = event.clipboardData.getData('text');
    } else if ((window as any).clipboardData) {
      // IE fallback
      pastedData = (window as any).clipboardData.getData('Text');
    }
    
    console.log('Raw paste data:', pastedData);
    
    if (!pastedData) {
      console.log('No paste data found');
      return;
    }
    
    // Remove non-numeric characters and take first 6 digits
    const numericData = pastedData.replace(/\D/g, '').slice(0, 6);
    console.log('Extracted numeric data:', numericData);
    
    if (numericData.length === 0) {
      console.log('No numeric data found');
      return;
    }
    
    // Clear all fields and array
    this.otpDigits = ['', '', '', '', '', ''];
    
    // Fill the digits
    for (let i = 0; i < 6; i++) {
      const input = document.getElementById(`otp-${i}`) as HTMLInputElement;
      if (input) {
        const digit = i < numericData.length ? numericData[i] : '';
        input.value = digit;
        this.otpDigits[i] = digit;
        console.log(`Field ${i}: ${digit}`);
      }
    }
    
    // Focus handling
    const focusIndex = Math.min(numericData.length, 5);
    setTimeout(() => {
      const focusInput = document.getElementById(`otp-${focusIndex}`) as HTMLInputElement;
      if (focusInput) {
        focusInput.focus();
      }
    }, 100);
    
    console.log('Final OTP:', this.otpDigits.join(''));
  }

  // Alternative paste method using navigator.clipboard API
  async handlePasteFromClipboard(inputIndex: number): Promise<void> {
    try {
      console.log('Attempting to read from clipboard...');
      
      if (navigator.clipboard && navigator.clipboard.readText) {
        const text = await navigator.clipboard.readText();
        console.log('Clipboard text:', text);
        
        const numericData = text.replace(/\D/g, '').slice(0, 6);
        console.log('Numeric data:', numericData);
        
        if (numericData.length > 0) {
          // Clear all fields
          this.otpDigits = ['', '', '', '', '', ''];
          
          // Fill the digits
          for (let i = 0; i < 6; i++) {
            const input = document.getElementById(`otp-${i}`) as HTMLInputElement;
            if (input) {
              const digit = i < numericData.length ? numericData[i] : '';
              input.value = digit;
              this.otpDigits[i] = digit;
            }
          }
          
          console.log('Filled OTP:', this.otpDigits.join(''));
        }
      }
    } catch (error) {
      console.error('Clipboard read failed:', error);
    }
  }

  // STEP 2: Verify OTP
  verifyOtp(): void {
    const otp = this.otpDigits.join('');
    
    if (otp.length !== 6) {
      this.alertService.error('Please enter complete 6-digit OTP');
      return;
    }

    console.log('🔍 Verifying OTP:', otp);
    
    // Move to password step (OTP will be verified during password reset)
    this.currentStep = 'password';
    this.alertService.success('OTP verified! Set your new password.');
  }

  // STEP 3: Reset password
  resetPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const otp = this.otpDigits.join('');

    const resetData = {
      email: this.userEmail,
      otp: otp,
      newPassword: this.passwordForm.value.newPassword
    };

    console.log('🔐 Resetting password...');

    this.authService.resetPassword(resetData).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('Password reset successfully! Please login.');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.loading = false;
        console.error('❌ Password reset failed:', error);
      }
    });
  }

  // Resend OTP
  resendOtp(): void {
    this.loading = true;

    this.authService.forgotPassword(this.userEmail).subscribe({
      next: (response) => {
        this.loading = false;
        this.alertService.success('New OTP sent to your email!');
        
        // Clear previous OTP - both array and DOM
        this.otpDigits = ['', '', '', '', '', ''];
        for (let i = 0; i < 6; i++) {
          const input = document.getElementById(`otp-${i}`) as HTMLInputElement;
          if (input) {
            input.value = '';
          }
        }
        
        // Focus on first input
        setTimeout(() => {
          const firstInput = document.getElementById('otp-0') as HTMLInputElement;
          if (firstInput) {
            firstInput.focus();
          }
        }, 100);
      },
      error: (error) => {
        this.loading = false;
        console.error('❌ Failed to resend OTP:', error);
      }
    });
  }

  // Toggle password visibility
  togglePasswordVisibility(field: 'password' | 'confirmPassword'): void {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  // Go back to previous step
  goBack(): void {
    if (this.currentStep === 'otp') {
      this.currentStep = 'email';
      this.otpDigits = ['', '', '', '', '', ''];
    } else if (this.currentStep === 'password') {
      this.currentStep = 'otp';
    }
  }
}