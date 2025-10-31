import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

interface EmployeeSalaryOverview {
  employeeId: number;
  employeeName: string;
  department: string;
  designation: string;
  status: string;  // ⭐ ADD THIS
  currentSalary: number;
  hasActiveSalary: boolean;
}

@Component({
  selector: 'app-salary-overview',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent, FormsModule],
  templateUrl: './salary-overview.component.html',
  styleUrls: ['./salary-overview.component.css']
})
export class SalaryOverviewComponent implements OnInit {
  private http = inject(HttpClient);

  employees: EmployeeSalaryOverview[] = [];
  loading = true;
  searchTerm = '';

  ngOnInit(): void {
    this.loadEmployeesWithSalary();
  }

  loadEmployeesWithSalary(): void {
    this.loading = true;
    
    // Load all employees
    this.http.get<any[]>(`${environment.apiUrl}/org/employees`).subscribe({
      next: (employees) => {
        this.employees = employees.map(emp => ({
          employeeId: emp.id,
          employeeName: emp.name,
          department: emp.department,
          designation: emp.designation,
          status: emp.status,  // ⭐ ADD THIS
          currentSalary: 0,
          hasActiveSalary: false
        }));
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employees:', error);
        this.loading = false;
      }
    });
  }

  get filteredEmployees() {
    if (!this.searchTerm) {
      return this.employees;
    }
    return this.employees.filter(emp =>
      emp.employeeName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      emp.department.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  // ⭐ NEW METHOD: Get status badge class
  getStatusClass(status: string): string {
    switch(status) {
      case 'ACTIVE': return 'badge-success';
      case 'INACTIVE': return 'badge-secondary';
      case 'TERMINATED': return 'badge-danger';
      case 'ON_LEAVE': return 'badge-warning';
      default: return 'badge-secondary';
    }
  }

  // ⭐ NEW METHOD: Check if employee is terminated
  isTerminated(status: string): boolean {
    return status === 'TERMINATED';
  }
}