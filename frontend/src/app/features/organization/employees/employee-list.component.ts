import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { EmployeeList, EmployeeStatus } from '../../../shared/models/employee.model';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent,FormsModule, SidebarComponent],
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.css']
})
export class EmployeeListComponent implements OnInit {
  private http = inject(HttpClient);
  private alertService = inject(AlertService);

  employees: EmployeeList[] = [];
  loading = true;
  searchTerm = '';

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading = true;
    this.http.get<EmployeeList[]>(`${environment.apiUrl}/org/employees`).subscribe({
      next: (data) => {
        this.employees = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading employees:', error);
        this.loading = false;
      }
    });
  }

  getStatusClass(status: EmployeeStatus): string {
    switch(status) {
      case EmployeeStatus.ACTIVE: return 'badge-success';
      case EmployeeStatus.INACTIVE: return 'badge-secondary';
      case EmployeeStatus.TERMINATED: return 'badge-danger';
      case EmployeeStatus.ON_LEAVE: return 'badge-warning';
      default: return 'badge-secondary';
    }
  }

  deleteEmployee(id: number, name: string): void {
    if (confirm(`Are you sure you want to delete employee "${name}"?`)) {
      this.http.delete(`${environment.apiUrl}/org/employees/${id}`).subscribe({
        next: () => {
          this.alertService.success('Employee deleted successfully');
          this.loadEmployees();
        },
        error: (error) => {
          console.error('Error deleting employee:', error);
        }
      });
    }
  }

  get filteredEmployees() {
    if (!this.searchTerm) {
      return this.employees;
    }
    return this.employees.filter(emp => 
      emp.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      emp.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      emp.department.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }
}