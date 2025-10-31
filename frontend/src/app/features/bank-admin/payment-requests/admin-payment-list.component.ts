import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { PaymentRequestList, PaymentRequestStatus } from '../../../shared/models/payment-request.model';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-admin-payment-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './admin-payment-list.component.html',
  styleUrls: ['./admin-payment-list.component.css']
})
export class AdminPaymentListComponent implements OnInit {
  private http = inject(HttpClient);

  paymentRequests: PaymentRequestList[] = [];
  loading = true;
  
  PaymentRequestStatus = PaymentRequestStatus;

  // Filter options
  selectedStatus: string = 'PENDING';
  statusOptions = [
    'ALL',
    PaymentRequestStatus.PENDING,
    PaymentRequestStatus.APPROVED,
    PaymentRequestStatus.REJECTED,
    PaymentRequestStatus.PROCESSING,
    PaymentRequestStatus.COMPLETED
  ];

  ngOnInit(): void {
    this.loadPaymentRequests();
  }

  loadPaymentRequests(): void {
    this.loading = true;
    
    // Use different endpoint for bank admin to get all organizations' requests
    this.http.get<PaymentRequestList[]>(`${environment.apiUrl}/admin/payment-requests`).subscribe({
      next: (data) => {
        this.paymentRequests = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading payment requests:', error);
        this.loading = false;
      }
    });
  }

  get filteredRequests(): PaymentRequestList[] {
    if (this.selectedStatus === 'ALL') {
      return this.paymentRequests;
    }

    // ⭐ IMPROVED: Use proper enum comparison
    return this.paymentRequests.filter(req => {
    return req.status.toString() === this.selectedStatus;
    });
  }

  get pendingCount(): number {
    return this.paymentRequests.filter(req => req.status === PaymentRequestStatus.PENDING).length;
  }

  get totalPendingAmount(): number {
    return this.paymentRequests
      .filter(req => req.status === PaymentRequestStatus.PENDING)
      .reduce((sum, req) => sum + req.totalAmount, 0);
  }

  getStatusClass(status: PaymentRequestStatus): string {
    switch(status) {
      case PaymentRequestStatus.PENDING: return 'badge-warning';
      case PaymentRequestStatus.APPROVED: return 'badge-info';
      case PaymentRequestStatus.REJECTED: return 'badge-danger';
      case PaymentRequestStatus.PROCESSING: return 'badge-primary';
      case PaymentRequestStatus.COMPLETED: return 'badge-success';
      default: return 'badge-secondary';
    }
  }
}