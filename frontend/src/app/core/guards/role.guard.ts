import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AlertService } from '../services/alert.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const alertService = inject(AlertService);

  const requiredRole = route.data['role'];
  const userRole = authService.getUserRole();

  if (userRole === requiredRole) {
    return true;
  }

  // Show error and redirect based on actual role
  alertService.error('Access Denied: You do not have permission to access this page');

  // Redirect to appropriate dashboard based on user's role
  switch (userRole) {
    case 'BANK_ADMIN':
      router.navigate(['/admin/dashboard']);
      break;
    case 'ORGANIZATION':
      router.navigate(['/organization/dashboard']);
      break;
    case 'EMPLOYEE':
      router.navigate(['/employee/dashboard']);
      break;
    default:
      router.navigate(['/login']);
  }

  return false;
};