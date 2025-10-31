import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { ConcernList, ConcernStatus, ConcernPriority } from '../../../shared/models/concern.model';

@Component({
  selector: 'app-org-concern-list',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './org-concern-list.component.html',
  styleUrls: ['./org-concern-list.component.css']
})
export class OrgConcernListComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);

  concerns: ConcernList[] = [];
  filteredConcerns: ConcernList[] = [];
  loading = true;
  selectedFilter = 'ALL';

  // Expose enums to template
  ConcernStatus = ConcernStatus;
  ConcernPriority = ConcernPriority;

  // Real-time statistics
  stats = {
    total: 0,
    pending: 0,
    inProgress: 0,
    resolved: 0,
    critical: 0,
    high: 0
  };

  ngOnInit(): void {
    this.loadConcerns();
  }

  loadConcerns(): void {
    this.loading = true;
    this.http.get<ConcernList[]>(`${environment.apiUrl}/org/concerns`).subscribe({
      next: (data) => {
        console.log('✅ Loaded concerns:', data);
        this.concerns = data;
        this.calculateStats(); // Calculate real stats
        this.applyFilter();
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Error loading concerns:', error);
        this.loading = false;
      }
    });
  }

  // ✅ Calculate real-time statistics from actual data
  calculateStats(): void {
    this.stats = {
      total: this.concerns.length,
      pending: this.concerns.filter(c => c.status === 'OPEN').length,
      inProgress: this.concerns.filter(c => c.status === 'IN_PROGRESS').length,
      resolved: this.concerns.filter(c => c.status === 'RESOLVED').length,
      critical: this.concerns.filter(c => c.priority === 'CRITICAL').length,
      high: this.concerns.filter(c => c.priority === 'HIGH').length
    };
  }

  // ✅ Filter and sort concerns by priority
  applyFilter(): void {
    // Apply filter
    if (this.selectedFilter === 'ALL') {
      this.filteredConcerns = [...this.concerns];
    } else if (this.selectedFilter === 'CRITICAL') {
      this.filteredConcerns = this.concerns.filter(c => c.priority === 'CRITICAL');
    } else if (this.selectedFilter === 'HIGH') {
      this.filteredConcerns = this.concerns.filter(c => c.priority === 'HIGH');
    } else if (this.selectedFilter === 'PENDING') {
      this.filteredConcerns = this.concerns.filter(c => c.status === 'OPEN');
    } else if (this.selectedFilter === 'IN_PROGRESS') {
      this.filteredConcerns = this.concerns.filter(c => c.status === 'IN_PROGRESS');
    } else if (this.selectedFilter === 'RESOLVED') {
      this.filteredConcerns = this.concerns.filter(c => c.status === 'RESOLVED');
    } else {
      this.filteredConcerns = [...this.concerns];
    }

    // ✅ SORT BY PRIORITY: Critical → High → Medium → Low
    this.filteredConcerns.sort((a, b) => {
      const priorityOrder: { [key: string]: number } = {
        'CRITICAL': 0,
        'HIGH': 1,
        'MEDIUM': 2,
        'LOW': 3
      };
      // Normalize to string safely (works for enum values or strings)
      const priorityA: string = String(a.priority);
      const priorityB: string = String(b.priority);
      return priorityOrder[priorityA] - priorityOrder[priorityB];
    });
  }

  onFilterChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.selectedFilter = target.value;
    this.applyFilter();
  }

  // ✅ Get CSS class for priority styling
  getPriorityClass(priority: ConcernPriority | string): string {
    const priorityStr: string = String(priority);
    return `priority-${priorityStr.toLowerCase()}`;
  }

  // ✅ Get indicator dot class
  getPriorityIndicatorClass(priority: ConcernPriority | string): string {
    const priorityStr: string = String(priority);
    return priorityStr.toLowerCase();
  }

  // ✅ Get status badge class
  getStatusBadgeClass(status: ConcernStatus | string): string {
  const statusStr: string = String(status);
  switch(statusStr) {
    case 'OPEN': return 'badge-warning';
    case 'IN_PROGRESS': return 'badge-info';
    case 'RESOLVED': return 'badge-success';
    case 'CLOSED': return 'badge-secondary';
    default: return 'badge-secondary';
  }
  }

  // ✅ Format date for display
  formatDate(dateString: string | Date): string {
    const date = typeof dateString === 'string' ? new Date(dateString) : dateString;
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;

    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  // ✅ Format status for display
  formatStatus(status: ConcernStatus | string): string {
    const statusStr: string = String(status);
    if (statusStr === 'IN_PROGRESS') return 'In Progress';
    return statusStr.charAt(0) + statusStr.slice(1).toLowerCase();
  }

  // ✅ Format priority for display
  formatPriority(priority: ConcernPriority | string): string {
    const priorityStr: string = String(priority);
    return priorityStr.charAt(0) + priorityStr.slice(1).toLowerCase();
  }

  // ✅ Navigate to concern detail
  viewConcernDetail(concernId: number): void {
    this.router.navigate(['/organization/concerns', concernId]);
  }
}