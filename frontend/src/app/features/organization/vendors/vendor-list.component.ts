import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { AlertService } from '../../../core/services/alert.service';
import { VendorList, VendorStatus } from '../../../shared/models/vendor.model';

@Component({
  selector: 'app-vendor-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './vendor-list.component.html',
  styleUrls: ['./vendor-list.component.css']
})
export class VendorListComponent implements OnInit {
  private http = inject(HttpClient);
  private alertService = inject(AlertService);

  vendors: VendorList[] = [];
  loading = true;
  searchTerm = '';

  VendorStatus = VendorStatus;

  ngOnInit(): void {
    this.loadVendors();
  }

  loadVendors(): void {
    this.loading = true;
    this.http.get<VendorList[]>(`${environment.apiUrl}/org/vendors`).subscribe({
      next: (data) => {
        console.log('VENDORS RAW RESPONSE:', data); // -> open browser console & inspect
        
        this.vendors = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading vendors:', error);
        this.loading = false;
      }
    });
  }

  get filteredVendors(): VendorList[] {
    if (!this.searchTerm) {
      return this.vendors;
    }
    return this.vendors.filter(v =>
      v.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      v.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      v.serviceType.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  deleteVendor(id: number, name: string): void {
    if (confirm(`Are you sure you want to delete vendor "${name}"?`)) {
      this.http.delete(`${environment.apiUrl}/org/vendors/${id}`).subscribe({
        next: () => {
          this.alertService.success('Vendor deleted successfully');
          this.loadVendors();
        },
        error: (error) => {
          console.error('Error deleting vendor:', error);
        }
      });
    }
  }

  getStatusClass(status: VendorStatus): string {
    switch(status) {
      case VendorStatus.ACTIVE: return 'badge-success';
      case VendorStatus.INACTIVE: return 'badge-secondary';
      case VendorStatus.BLACKLISTED: return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  get activeCount(): number {
    return this.vendors.filter(v => v.status === VendorStatus.ACTIVE).length;
  }
}