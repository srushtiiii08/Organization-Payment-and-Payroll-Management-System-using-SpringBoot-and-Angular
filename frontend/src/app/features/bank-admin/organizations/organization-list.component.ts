import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { OrganizationList } from '../../../shared/models/organization.model';

@Component({
  selector: 'app-organization-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './organization-list.component.html',
  styleUrls: ['./organization-list.component.css']
})
export class OrganizationListComponent implements OnInit {
  private http = inject(HttpClient);

  organizations: OrganizationList[] = [];
  loading = true;
  searchTerm = '';
  selectedFilter = 'ALL';
  filterOptions = ['ALL', 'VERIFIED', 'PENDING', 'REJECTED']; // ✅ ADDED REJECTED

  ngOnInit(): void {
    this.loadOrganizations();
  }

  loadOrganizations(): void {
    this.loading = true;
    
    let url = `${environment.apiUrl}/admin/organizations`;
    
    // ✅ Filter by status using backend endpoints
    if (this.selectedFilter === 'VERIFIED') {
      url += '/status?verified=true';
    } else if (this.selectedFilter === 'PENDING') {
      url += '/pending';
    } else if (this.selectedFilter === 'REJECTED') {
      url += '/rejected';
    }
    
    this.http.get<OrganizationList[]>(url).subscribe({
      next: (data) => {
        this.organizations = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading organizations:', error);
        this.loading = false;
      }
    });
  }

  // ✅ NEW METHOD: Called when filter dropdown changes
  onFilterChange(): void {
    this.loadOrganizations();
  }

  get filteredOrganizations(): OrganizationList[] {
    let filtered = this.organizations;

    // Filter by search term only (status filtering is done by backend)
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(org =>
        org.name.toLowerCase().includes(term) ||
        (org.email && org.email.toLowerCase().includes(term)) ||
        org.registrationNumber.toLowerCase().includes(term)
      );
    }

    return filtered;
  }

  // ✅ UPDATED: Get pending count from all organizations
  get pendingCount(): number {
    // For accurate count, we need to fetch all and filter
    // This will be displayed in stats card
    return this.organizations.filter(org => 
      !org.verified && org.userStatus === 'PENDING'
    ).length;
  }

  get verifiedCount(): number {
    return this.organizations.filter(org => org.verified).length;
  }

  // ✅ NEW GETTER: Get rejected count
  get rejectedCount(): number {
    return this.organizations.filter(org => 
      !org.verified && org.userStatus === 'INACTIVE'
    ).length;
  }
}