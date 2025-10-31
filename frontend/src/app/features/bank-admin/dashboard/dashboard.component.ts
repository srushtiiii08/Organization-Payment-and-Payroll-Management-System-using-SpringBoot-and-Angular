import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { HttpClient } from '@angular/common/http';
import { OrganizationList } from '../../../shared/models/organization.model';
import { environment } from '../../../../environments/environment';
import { PaymentRequest } from '../../../shared/models/payment-request.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);

  organizations: OrganizationList[] = [];
  paymentRequests: PaymentRequest[] = [];
  processedPaymentsCount = 0; // 🆕 NEW PROPERTY
  
  // Loading states
  loading = true;

  searchTerm = '';
  selectedFilter = 'ALL';
  filterOptions = ['ALL', 'VERIFIED', 'PENDING'];


  ngOnInit(): void {
    this.loadDashboardData();
  }

  /**
   * STEP 1: Load all dashboard data
   * Uses forkJoin to make parallel API calls for better performance
   */
  loadDashboardData(): void {
    this.loading = true;
    
    // 🆕 UPDATED: Added processed payments count to forkJoin
    forkJoin({
      organizations: this.http.get<OrganizationList[]>(`${environment.apiUrl}/admin/organizations`),
      paymentRequests: this.http.get<PaymentRequest[]>(`${environment.apiUrl}/admin/payment-requests`),
      processedPayments: this.http.get<{count: number}>(`${environment.apiUrl}/admin/payment-requests/processed/count`) 
    }).subscribe({
      next: (data) => {
        // Store the data
        this.organizations = data.organizations;
        this.paymentRequests = data.paymentRequests;
        this.processedPaymentsCount = data.processedPayments.count; 
        
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Error loading dashboard data:', error);
        this.loading = false;
      }
    });
  }

  /**
   * STEP 2: Computed properties for stats
   */
  
  // Count of pending organization verifications
  get pendingCount(): number {
    return this.organizations.filter(
      org => !org.verified && org.userStatus === 'PENDING')
      .length;
  }

  // Count of verified organizations
  get verifiedCount(): number {
    return this.organizations.filter(org => org.verified).length;
  }

  // Count of pending payment requests
  get pendingPaymentsCount(): number {
    return this.paymentRequests.filter(payment => payment.status === 'PENDING').length;
  }

  /**
   * Helper method for filtered organizations (for future use)
   */
  get filteredOrganizations(): OrganizationList[] {
    let filtered = this.organizations;

    // Filter by verification status
    if (this.selectedFilter === 'VERIFIED') {
      filtered = filtered.filter(org => org.verified);
    } else if (this.selectedFilter === 'PENDING') {
      filtered = filtered.filter(org => !org.verified);
    }

    // Filter by search term
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(org =>
        org.name.toLowerCase().includes(term) ||
        org.email.toLowerCase().includes(term) ||
        org.registrationNumber.toLowerCase().includes(term)
      );
    }

    return filtered;
  }
}