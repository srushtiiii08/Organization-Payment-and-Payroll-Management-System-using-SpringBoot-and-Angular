import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { EmployeeList, EmployeeStatus } from '../../../shared/models/employee.model';
import { PaymentRequestList, PaymentRequestStatus } from '../../../shared/models/payment-request.model';
import { ConcernList } from '../../../shared/models/concern.model';

interface DashboardStats {
  totalEmployees: number;
  activeEmployees: number;
  pendingPayments: number;
  openConcerns: number;
}

@Component({
  selector: 'app-org-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  templateUrl: './org-dashboard.component.html',
  styleUrls: ['./org-dashboard.component.css']
})
export class OrgDashboardComponent implements OnInit {
  private http = inject(HttpClient);
  
  // Data arrays
  employees: EmployeeList[] = [];
  paymentRequests: PaymentRequestList[] = [];
  concerns: ConcernList[] = [];
  
  // Stats object
  stats: DashboardStats = {
    totalEmployees: 0,
    activeEmployees: 0,
    pendingPayments: 0,
    openConcerns: 0
  };

  loading = true;
  organizationInfo: any = null;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  /**
   * Load all dashboard data using forkJoin for parallel API calls
   * Following the same pattern as Bank Admin Dashboard
   */
  loadDashboardData(): void {
    this.loading = true;
    
    // ✅ Make parallel API calls using forkJoin
    forkJoin({
      profile: this.http.get(`${environment.apiUrl}/org/profile`),
      employees: this.http.get<EmployeeList[]>(`${environment.apiUrl}/org/employees`),
      paymentRequests: this.http.get<PaymentRequestList[]>(`${environment.apiUrl}/org/payment-requests`),
      concerns: this.http.get<ConcernList[]>(`${environment.apiUrl}/org/concerns`)
    }).subscribe({
      next: (data) => {
        // Store organization profile
        this.organizationInfo = data.profile;
        
        // Store the arrays
        this.employees = data.employees;
        this.paymentRequests = data.paymentRequests;
        this.concerns = data.concerns;
        
        // ✅ Calculate real-time statistics
        this.calculateStats();
        
        this.loading = false;
        
        console.log('✅ Dashboard loaded:', {
          employees: this.employees.length,
          payments: this.paymentRequests.length,
          concerns: this.concerns.length,
          stats: this.stats
        });
      },
      error: (error) => {
        console.error('❌ Error loading dashboard data:', error);
        this.loading = false;
        
        // Show friendly error message if needed
        if (error.status === 404) {
          console.warn('⚠️ Some endpoints not found - using available data');
        }
      }
    });
  }

  /**
   * Calculate real-time statistics from loaded data
   * Following the same pattern as Concern List Component
   */
  calculateStats(): void {
    // Total Employees - Only ACTIVE and ON_LEAVE
    // this.stats.totalEmployees = this.employees.length;
    this.stats.totalEmployees = this.employees.filter(
      emp => emp.status === EmployeeStatus.ACTIVE || emp.status === EmployeeStatus.ON_LEAVE
    ).length;
    
    
    // Active Employees (only those with ACTIVE status)
    this.stats.activeEmployees = this.employees.filter(
      emp => emp.status === EmployeeStatus.ACTIVE
    ).length;
    
    // Pending Payment Requests
    this.stats.pendingPayments = this.paymentRequests.filter(
      payment => payment.status === PaymentRequestStatus.PENDING
    ).length;
    
    // Open Concerns (OPEN + IN_PROGRESS)
    this.stats.openConcerns = this.concerns.filter(
      concern => concern.status === 'OPEN' || concern.status === 'IN_PROGRESS'
    ).length;
    
    console.log('📊 Stats calculated:', this.stats);
  }

  /**
   * Computed property: Total verified employees
   */
  get verifiedEmployees(): number {
    return this.employees.filter(
      emp => emp.accountVerificationStatus === 'VERIFIED'
    ).length;
  }

  /**
   * Computed property: Pending verifications
   */
  get pendingVerifications(): number {
    return this.employees.filter(
      emp => emp.accountVerificationStatus === 'PENDING'
    ).length;
  }

  /**
   * Computed property: Completed payment requests
   */
  get completedPayments(): number {
    return this.paymentRequests.filter(
      payment => payment.status === PaymentRequestStatus.COMPLETED
    ).length;
  }

  /**
   * Computed property: Critical concerns
   */
  get criticalConcerns(): number {
    return this.concerns.filter(
      concern => concern.priority === 'CRITICAL'
    ).length;
  }
}