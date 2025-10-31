import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { AlertService } from '../../../core/services/alert.service';
import { VendorStatus } from '../../../shared/models/vendor.model';

@Component({
  selector: 'app-vendor-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  templateUrl: './vendor-detail.component.html',
  styleUrls: ['./vendor-detail.component.css']
})
export class VendorDetailComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private alertService = inject(AlertService);

  vendor: any = null;
  loading = true;
  VendorStatus = VendorStatus;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadVendor(Number(id));
    }
  }

  loadVendor(id: number): void {
    this.loading = true;
    this.http.get(`${environment.apiUrl}/org/vendors/${id}`).subscribe({
      next: (data) => {
        this.vendor = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading vendor:', error);
        this.alertService.error('Failed to load vendor details');
        this.loading = false;
      }
    });
  }

  updateStatus(newStatus: VendorStatus): void {
    if (!confirm(`Change vendor status to ${newStatus}?`)) {
      return;
    }

    this.http.put(
      `${environment.apiUrl}/org/vendors/${this.vendor.id}/status?status=${newStatus}`,
      {}
    ).subscribe({
      next: () => {
        this.alertService.success('Vendor status updated successfully');
        this.loadVendor(this.vendor.id);
      },
      error: (error) => {
        console.error('Error updating status:', error);
        this.alertService.error('Failed to update vendor status');
      }
    });
  }

  deleteVendor(): void {
    if (!confirm(`Are you sure you want to delete vendor "${this.vendor.name}"?`)) {
      return;
    }

    this.http.delete(`${environment.apiUrl}/org/vendors/${this.vendor.id}`).subscribe({
      next: () => {
        this.alertService.success('Vendor deleted successfully');
        this.router.navigate(['/organization/vendors']);
      },
      error: (error) => {
        console.error('Error deleting vendor:', error);
        this.alertService.error('Failed to delete vendor');
      }
    });
  }

  getStatusClass(status: VendorStatus): string {
    switch(status) {
      case VendorStatus.ACTIVE: return 'badge-success';
      case VendorStatus.INACTIVE: return 'badge-secondary';
      case VendorStatus.BLACKLISTED: return 'badge-danger';
      default: return 'badge-secondary';
    }
  }
}