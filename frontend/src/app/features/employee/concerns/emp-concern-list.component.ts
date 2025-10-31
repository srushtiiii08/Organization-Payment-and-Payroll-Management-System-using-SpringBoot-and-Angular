import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { ConcernList, ConcernStatus, ConcernPriority } from '../../../shared/models/concern.model';

@Component({
  selector: 'app-emp-concern-list',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  templateUrl: './emp-concern-list.component.html',
  styleUrls: ['./emp-concern-list.component.css']
})
export class EmpConcernListComponent implements OnInit {
  private http = inject(HttpClient);

  concerns: ConcernList[] = [];
  loading = true;

  ConcernStatus = ConcernStatus;

  ngOnInit(): void {
    this.loadConcerns();
  }

  loadConcerns(): void {
    this.loading = true;
    this.http.get<ConcernList[]>(`${environment.apiUrl}/employee/concerns`).subscribe({
      next: (data) => {
        this.concerns = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading concerns:', error);
        this.loading = false;
      }
    });
  }

  getStatusClass(status: ConcernStatus | string): string {
  const statusStr: string = String(status);
  switch (statusStr) {
    case 'OPEN': return 'badge-warning';
    case 'IN_PROGRESS': return 'badge-info';
    case 'RESOLVED': return 'badge-success';
    case 'CLOSED': return 'badge-secondary';
    default: return 'badge-secondary';
  }
  }


  getPriorityClass(priority: ConcernPriority | string): string {
  const priorityStr: string = String(priority);
  switch (priorityStr) {
    case 'LOW': return 'priority-low';
    case 'MEDIUM': return 'priority-medium';
    case 'HIGH': return 'priority-high';
    case 'CRITICAL': return 'priority-critical';
    default: return 'priority-low';
  }
  }

  //more robust to handle counts robust when status maybe str
  get openCount(): number {
    return this.concerns.filter(c => String(c.status) === ConcernStatus.OPEN).length;
  }

  get resolvedCount(): number {
    return this.concerns.filter(c => String(c.status) === ConcernStatus.RESOLVED).length;
  }

  
}