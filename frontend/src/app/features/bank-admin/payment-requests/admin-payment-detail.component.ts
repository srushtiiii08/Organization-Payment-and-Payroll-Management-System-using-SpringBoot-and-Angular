import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { PaymentRequest, PaymentRequestStatus } from '../../../shared/models/payment-request.model';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-admin-payment-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, NavbarComponent, LoaderComponent, SidebarComponent],
  templateUrl: './admin-payment-detail.component.html',
  styleUrls: ['./admin-payment-detail.component.css']
})
export class AdminPaymentDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private alertService = inject(AlertService);

  paymentRequest: PaymentRequest | null = null;
  loading = true;
  processing = false;
  requestId!: number;

  rejectionForm!: FormGroup;
  showRejectionModal = false;

  PaymentRequestStatus = PaymentRequestStatus;

  ngOnInit(): void {
    this.requestId = Number(this.route.snapshot.paramMap.get('id'));
    
    this.rejectionForm = this.fb.group({
      rejectionReason: ['', [Validators.required, Validators.minLength(10)]]
    });

    this.loadPaymentRequest();
  }

  loadPaymentRequest(): void {
    this.http.get<PaymentRequest>(`${environment.apiUrl}/admin/payment-requests/${this.requestId}`).subscribe({
      next: (data) => {
        this.paymentRequest = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading payment request:', error);
        this.loading = false;
        this.router.navigate(['/admin/payment-requests']);
      }
    });
  }

  approveRequest(): void {
    if (!confirm('Are you sure you want to APPROVE this payment request? This action cannot be undone.')) {
      return;
    }

    this.processing = true;

    this.http.post(`${environment.apiUrl}/admin/payment-requests/${this.requestId}/approve`, {}).subscribe({
      next: () => {
        this.processing = false;
        this.alertService.success('Payment request approved successfully');
        this.loadPaymentRequest();
      },
      error: (error) => {
        this.processing = false;
        console.error('Error approving request:', error);
      }
    });
  }

  openRejectionModal(): void {
    this.showRejectionModal = true;
  }

  closeRejectionModal(): void {
    this.showRejectionModal = false;
    this.rejectionForm.reset();
  }

  rejectRequest(): void {
    if (this.rejectionForm.invalid) {
      Object.keys(this.rejectionForm.controls).forEach(key => {
        this.rejectionForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.processing = true;

    const payload = {
      rejectionReason: this.rejectionForm.value.rejectionReason
    };

    this.http.post(`${environment.apiUrl}/admin/payment-requests/${this.requestId}/reject`, payload).subscribe({
      next: () => {
        this.processing = false;
        this.closeRejectionModal();
        this.alertService.success('Payment request rejected');
        this.loadPaymentRequest();
      },
      error: (error) => {
        this.processing = false;
        console.error('Error rejecting request:', error);
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

  get canApprove(): boolean {
    return this.paymentRequest?.status === PaymentRequestStatus.PENDING;
  }

  get canReject(): boolean {
    return this.paymentRequest?.status === PaymentRequestStatus.PENDING;
  }

  processPayment(): void {
  if (!confirm('⚠️ Are you sure you want to PROCESS this payment? This will disburse salaries to all employees and cannot be undone.')) {
    return;
  }

  this.processing = true;

  this.http.post(`${environment.apiUrl}/salary-payments/process/${this.requestId}`, {}).subscribe({
    next: (response: any) => {
      console.log('✅ Payment processed successfully:', response);
      this.processing = false;
      this.alertService.success(`Payment processed successfully for ${response.length} employees`);
      this.loadPaymentRequest(); // Reload to show updated status
    },
    error: (error) => {
      this.processing = false;
      console.error('❌ Error processing payment:', error);
      
      if (error.error?.message) {
        this.alertService.error(error.error.message);
      } else {
        this.alertService.error('Failed to process payment');
      }
    }
  });
}
}