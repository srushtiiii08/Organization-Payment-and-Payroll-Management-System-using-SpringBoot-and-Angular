import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { Employee, EmployeeStatus, AccountVerificationStatus } from '../../../shared/models/employee.model';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.css']
})
export class EmployeeDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);
  private alertService = inject(AlertService);

   EmployeeStatus = EmployeeStatus;
  AccountVerificationStatus = AccountVerificationStatus;

  employee: Employee | null = null;
  loading = true;
  employeeId!: number;

  ngOnInit(): void {
    this.employeeId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadEmployeeDetails();
  }

  loadEmployeeDetails(): void {
    this.http.get<Employee>(`${environment.apiUrl}/org/employees/${this.employeeId}`).subscribe({
      next: (data) => {
        this.employee = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employee:', error);
        this.loading = false;
        this.router.navigate(['/organization/employees']);
      }
    });
  }

 

  getStatusClass(status: EmployeeStatus): string {
    switch(status) {
      case EmployeeStatus.ACTIVE: return 'badge-success';
      case EmployeeStatus.INACTIVE: return 'badge-secondary';
      case EmployeeStatus.TERMINATED: return 'badge-danger';
      case EmployeeStatus.ON_LEAVE: return 'badge-warning';
      default: return 'badge-secondary';
    }
  }

  getVerificationClass(status: AccountVerificationStatus): string {
    switch(status) {
      case AccountVerificationStatus.VERIFIED: return 'badge-success';
      case AccountVerificationStatus.PENDING: return 'badge-warning';
      case AccountVerificationStatus.REJECTED: return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  /*Check if employee is terminated*/
  isEmployeeTerminated(): boolean {
    return this.employee?.status === EmployeeStatus.TERMINATED;
  }

  /**
   * Verify account - with termination check
   */
  verifyAccount(): void {
    // Prevent verification if terminated
    if (this.isEmployeeTerminated()) {
      this.alertService.error('Cannot verify account for terminated employee');
      return;
    }

    if (confirm('Are you sure you want to verify this employee account?')) {
      this.http.post(`${environment.apiUrl}/org/employees/${this.employeeId}/verify-account`, {}).subscribe({
        next: () => {
          this.alertService.success('Employee account verified successfully');
          this.loadEmployeeDetails();
        },
        error: (error) => {
          console.error('Error verifying account:', error);
          this.alertService.error('Failed to verify account');
        }
      });
    }
  }


  /* Reactivate a terminated employee*/
  reactivateEmployee(): void {
    if (confirm('Are you sure you want to reactivate this employee? They will regain access to the system.')) {
      this.http.put(`${environment.apiUrl}/org/employees/${this.employeeId}/status?status=${EmployeeStatus.ACTIVE}`, {}).subscribe({
        next: () => {
          this.alertService.success('Employee reactivated successfully');
          this.loadEmployeeDetails();
        },
        error: (error) => {
          console.error('Error reactivating employee:', error);
          this.alertService.error('Failed to reactivate employee');
        }
      });
    }
  }

  updateStatus(status: EmployeeStatus): void {
    let confirmMessage = `Are you sure you want to change status to ${status}?`;
    
    // Special confirmation for termination
    if (status === EmployeeStatus.TERMINATED) {
      confirmMessage = 'Are you sure you want to TERMINATE this employee? This will:\n\n' +
        '• Revoke their system access\n' +
        '• Disable account verification\n' +
        '• Disable salary management\n\n' +
        'You can reactivate them later if needed.';
    }

    if (confirm(confirmMessage)) {
      this.http.put(`${environment.apiUrl}/org/employees/${this.employeeId}/status?status=${status}`, {}).subscribe({
        next: () => {
          this.alertService.success(`Employee status updated to ${status}`);
          this.loadEmployeeDetails();
        },
        error: (error) => {
          console.error('Error updating status:', error);
          this.alertService.error('Failed to update employee status');
        }
      });
    }
  }


  // METHODS FOR DOCUMENT HANDLING
  /**
   * Check if the URL is a PDF file
   */
  isPDF(url: string): boolean {
    if (!url) return false;
    const lowerUrl = url.toLowerCase();
    return lowerUrl.endsWith('.pdf') || lowerUrl.includes('.pdf');
  }

  /**
   * Check if the URL is an image file
   */
  isImage(url: string): boolean {
    if (!url) return false;
    const lowerUrl = url.toLowerCase();
    return lowerUrl.endsWith('.jpg') || 
           lowerUrl.endsWith('.jpeg') || 
           lowerUrl.endsWith('.png') ||
           lowerUrl.includes('/image/') ||
           lowerUrl.includes('image%2F');
  }

  /**
   * Get human-readable file type from URL
   */
  getFileType(url: string): string {
    if (!url) return 'Unknown';
    
    if (this.isPDF(url)) return 'PDF Document';
    if (this.isImage(url)) {
      if (url.toLowerCase().includes('.jpg') || url.toLowerCase().includes('.jpeg')) {
        return 'JPEG Image';
      }
      if (url.toLowerCase().includes('.png')) {
        return 'PNG Image';
      }
      return 'Image File';
    }
    
    return 'Document';
  }

  /**
   * Extract filename from Cloudinary URL
   */
  getFileName(url: string): string {
    if (!url) return 'document';
    
    try {
      // Extract filename from Cloudinary URL
      const parts = url.split('/');
      const filenamePart = parts[parts.length - 1];
      
      // Remove any query parameters
      const filename = filenamePart.split('?')[0];
      
      return filename || 'account_proof';
    } catch (error) {
      return 'account_proof';
    }
  }

  /**
   * Handle image load errors
   */
  onImageError(event: any): void {
    // console.error('❌ Failed to load image:', event);
    // this.alertService.error('Failed to load document preview');
    
    // Hide the broken image
    event.target.style.display = 'none';
  }

}