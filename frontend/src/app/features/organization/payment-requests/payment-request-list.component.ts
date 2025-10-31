import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { PaymentRequestList, PaymentRequestStatus } from '../../../shared/models/payment-request.model';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-payment-request-list',
  standalone: true,
  imports: [CommonModule, RouterLink,FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './payment-request-list.component.html',
  styleUrls: ['./payment-request-list.component.css']
})
export class PaymentRequestListComponent implements OnInit {
  private http = inject(HttpClient);
  private alertService = inject(AlertService);

  paymentRequests: PaymentRequestList[] = [];
  loading = true;
  
  // ✅ Make enum available in template
  PaymentRequestStatus = PaymentRequestStatus;

  // Filter options
  selectedStatus: string = 'ALL';
  statusOptions = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'PROCESSING', 'COMPLETED'];

  ngOnInit(): void {
    this.loadPaymentRequests();
  }

  loadPaymentRequests(): void {
    this.loading = true;
    this.http.get<PaymentRequestList[]>(`${environment.apiUrl}/org/payment-requests`).subscribe({
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
    return this.paymentRequests.filter(req => req.status === this.selectedStatus);
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

  deleteRequest(id: number): void {
    if (confirm('Are you sure you want to delete this payment request?')) {
      this.http.delete(`${environment.apiUrl}/org/payment-requests/${id}`).subscribe({
        next: () => {
          this.alertService.success('Payment request deleted successfully');
          this.loadPaymentRequests();
        },
        error: (error) => {
          console.error('Error deleting payment request:', error);
        }
      });
    }
  }
}