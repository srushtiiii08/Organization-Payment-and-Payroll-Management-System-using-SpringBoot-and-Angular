import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { PaymentRequest, PaymentRequestStatus } from '../../../shared/models/payment-request.model';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-payment-request-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  templateUrl: './payment-request-detail.component.html',
  styleUrls: ['./payment-request-detail.component.css']
})
export class PaymentRequestDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);

  paymentRequest: PaymentRequest | null = null;
  loading = true;
  requestId!: number;

  // ✅ Make enum available in template
  PaymentRequestStatus = PaymentRequestStatus;

  ngOnInit(): void {
    this.requestId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPaymentRequest();
  }

  loadPaymentRequest(): void {
    this.http.get<PaymentRequest>(`${environment.apiUrl}/org/payment-requests/${this.requestId}`).subscribe({
      next: (data) => {
        this.paymentRequest = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading payment request:', error);
        this.loading = false;
        this.router.navigate(['/organization/payment-requests']);
      }
    });
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

  getStatusIcon(status: PaymentRequestStatus): string {
    switch(status) {
      case PaymentRequestStatus.PENDING: return '⏳';
      case PaymentRequestStatus.APPROVED: return '✓';
      case PaymentRequestStatus.REJECTED: return '✗';
      case PaymentRequestStatus.PROCESSING: return '⚙️';
      case PaymentRequestStatus.COMPLETED: return '✓✓';
      default: return '•';
    }
  }
}