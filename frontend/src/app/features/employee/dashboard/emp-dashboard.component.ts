import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { EmployeeProfile } from '../../../shared/models/employee.model';

@Component({
  selector: 'app-emp-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  templateUrl: './emp-dashboard.component.html',
  styleUrls: ['./emp-dashboard.component.css']
})
export class EmpDashboardComponent implements OnInit {
  private http = inject(HttpClient);

  employeeProfile: EmployeeProfile | null = null;
  loading = true;

  recentSalaries: any[] = [];
  
  currentDate = new Date();


  ngOnInit(): void {
    this.loadEmployeeProfile();
    this.loadRecentSalaries();
  }

  loadEmployeeProfile(): void {
    this.http.get<EmployeeProfile>(`${environment.apiUrl}/employee/profile`).subscribe({
      next: (data) => {
        this.employeeProfile = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.loading = false;
      }
    });
  }

  loadRecentSalaries(): void {
    this.http.get<any[]>(`${environment.apiUrl}/salary-payments/my-history`).subscribe({
      next: (data) => {
        this.recentSalaries = data.slice(0, 3); // Last 3 salaries
      },
      error: (error) => {
        console.error('Error loading salaries:', error);
      }
    });
  }

  getVerificationClass(status: string): string {
    switch(status) {
      case 'VERIFIED': return 'badge-success';
      case 'PENDING': return 'badge-warning';
      case 'REJECTED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  getPaymentStatusClass(status: string): string {
    switch(status) {
      case 'COMPLETED': return 'badge-success';
      case 'PENDING': return 'badge-warning';
      case 'PROCESSING': return 'badge-info';
      case 'FAILED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }
}