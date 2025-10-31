import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';
import { 
  LoginRequest, 
  LoginResponse, 
  RegisterOrganizationRequest,
  RegisterEmployeeRequest,
  ChangePasswordRequest,
  Role,
  CaptchaResponse
} from '../../shared/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private storageService = inject(StorageService);

  private apiUrl = `${environment.apiUrl}/auth`;
  
  // Observable for login state
  private currentUserSubject = new BehaviorSubject<any>(
    this.storageService.getUser()
  );
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {}

  // Generate CAPTCHA
  generateCaptcha(): Observable<CaptchaResponse> {
    return this.http.get<CaptchaResponse>(`${this.apiUrl}/captcha`);
  }
  
  // Login
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          // Save token and user data
          this.storageService.saveToken(response.token);
          const userData = {
            userId: response.userId,
            email: response.email,
            role: response.role
          };
          this.storageService.saveUser(userData);
          this.currentUserSubject.next(userData);
        })
      );
  }

  // Register Organization
  registerOrganization(formData: FormData): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/register/organization`, formData)
      .pipe(
        tap(response => {
          this.storageService.saveToken(response.token);
          const userData = {
            userId: response.userId,
            email: response.email,
            role: response.role
          };
          this.storageService.saveUser(userData);
          this.currentUserSubject.next(userData);
        })
      );
  }

  // Register Employee
  registerEmployee(data: RegisterEmployeeRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register/employee`, data);
  }

  // // Change Password
  // changePassword(data: ChangePasswordRequest): Observable<any> {
  //   return this.http.post(`${this.apiUrl}/change-password`, data);
  // }

  // 🆕 FORGOT PASSWORD METHOD
// Send OTP to email
forgotPassword(email: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/forgot-password`, { email });
}

// Reset password with OTP
resetPassword(data: { email: string; otp: string; newPassword: string }): Observable<any> {
  return this.http.post(`${this.apiUrl}/reset-password`, data);
}

  // Logout
  logout(): void {
    this.storageService.clear();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
    return !!this.storageService.getToken();
  }

  // Get current user
  getCurrentUser(): any {
    return this.storageService.getUser();
  }

  // Get user role
  getUserRole(): Role | null {
    const user = this.getCurrentUser();
    return user ? user.role : null;
  }

  // Check if user has specific role
  hasRole(role: Role): boolean {
    return this.getUserRole() === role;
  }
}