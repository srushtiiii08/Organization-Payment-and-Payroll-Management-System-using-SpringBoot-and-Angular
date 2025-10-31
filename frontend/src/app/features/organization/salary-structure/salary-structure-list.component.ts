import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertService } from '../../../core/services/alert.service';
import { SalaryStructure } from '../../../shared/models/salary.model';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-salary-structure-list',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  templateUrl: './salary-structure-list.component.html',
  styleUrls: ['./salary-structure-list.component.css']
})
export class SalaryStructureListComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private alertService = inject(AlertService);

  employeeId!: number;
  salaryStructures: SalaryStructure[] = [];
  activeSalary: SalaryStructure | null = null;
  loading = true;
  employeeName = '';
  expandedSalaryId: number | null = null;

  ngOnInit(): void {
    this.employeeId = Number(this.route.snapshot.paramMap.get('employeeId'));
    this.loadSalaryStructures();
  }

  loadSalaryStructures(): void {
    this.loading = true;

    // Load employee details
    this.http.get<any>(`${environment.apiUrl}/org/employees/${this.employeeId}`).subscribe({
      next: (employee) => {
        this.employeeName = employee.name;
      }
    });

    // Load salary history
    this.http.get<SalaryStructure[]>(
      `${environment.apiUrl}/org/salary-structure/employee/${this.employeeId}/history`
    ).subscribe({
      next: (data) => {
        this.salaryStructures = data;
        this.activeSalary = data.find(s => s.isActive) || null;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading salary structures:', error);
        this.loading = false;
      }
    });
  }

  deactivateSalary(id: number): void {
    if (confirm('Are you sure you want to deactivate this salary structure?')) {
      this.http.delete(`${environment.apiUrl}/org/salary-structure/${id}`).subscribe({
        next: () => {
          this.alertService.success('Salary structure deactivated successfully');
          this.loadSalaryStructures();
        },
        error: (error) => {
          console.error('Error deactivating salary:', error);
        }
      });
    }
  }

  //ADD THIS NEW METHOD
  toggleDetails(salaryId: number): void {
    if (this.expandedSalaryId === salaryId) {
      this.expandedSalaryId = null;  // Collapse if already expanded
    } else {
      this.expandedSalaryId = salaryId;  // Expand this one
    }
  }

  // ADD THIS NEW METHOD
  isExpanded(salaryId: number): boolean {
    return this.expandedSalaryId === salaryId;
  }
}