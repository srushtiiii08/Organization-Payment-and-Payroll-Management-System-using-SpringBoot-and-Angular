import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AlertService } from '../services/alert.service';
import { StorageService } from '../services/storage.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const alertService = inject(AlertService);
  const storageService = inject(StorageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 400:
            errorMessage = error.error?.message || 'Bad Request';
            break;
          case 401:
            errorMessage = error.error?.message || 'Unauthorized';
            // Auto logout on 401
            storageService.clear();
            router.navigate(['/login']);
            break;
          case 403:
            errorMessage = error.error?.message || 'Access Denied';
            break;
          case 404:
            errorMessage = error.error?.message || 'Resource Not Found';
            break;
          case 409:
            errorMessage = error.error?.message || 'Conflict - Resource Already Exists';
            break;
          case 500:
            errorMessage = error.error?.message || 'Internal Server Error';
            break;
          default:
            errorMessage = error.error?.message || `Error: ${error.statusText}`;
        }
      }

      // Show error alert
      alertService.error(errorMessage);

      return throwError(() => error);
    })
  );
};