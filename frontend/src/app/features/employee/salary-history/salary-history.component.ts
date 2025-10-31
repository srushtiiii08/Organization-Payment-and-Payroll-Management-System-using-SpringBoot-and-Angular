import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-salary-history',
  standalone: true,
  imports: [CommonModule,FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './salary-history.component.html',
  styleUrls: ['./salary-history.component.css']
})
export class SalaryHistoryComponent implements OnInit {
  private http = inject(HttpClient);

  salaryPayments: any[] = [];
  loading = true;
  selectedYear: number = new Date().getFullYear();
  years: number[] = [];

  ngOnInit(): void {
    // Generate last 5 years
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 5; i++) {
      this.years.push(currentYear - i);
    }

    this.loadSalaryHistory();
  }

  // loadSalaryHistory(): void {
  //   this.loading = true;
  //   this.http.get<any[]>(`${environment.apiUrl}/salary-payments/my-history?year=${this.selectedYear}`).subscribe({
  //     next: (data) => {
  //       this.salaryPayments = data;
  //       this.loading = false;
  //     },
  //     error: (error) => {
  //       console.error('Error loading salary history:', error);
  //       this.loading = false;
  //     }
  //   });
  // }

  loadSalaryHistory(): void {
  this.loading = true;
  console.log('🔍 Loading salary history for year:', this.selectedYear);
  this.http.get<any[]>(`${environment.apiUrl}/salary-payments/my-history?year=${this.selectedYear}`).subscribe({
    next: (data) => {
      console.log('💰 Salary History Response:', data); 
      if (data.length > 0) {
        console.log('📋 First payment structure:', data[0]); 
      }
       const years = data.map(p => p.year);
      console.log('📅 Years in response:', years);
      this.salaryPayments = data;
      this.loading = false;
    },
    error: (error) => {
      console.error('Error loading salary history:', error);
      this.loading = false;
    }
  });
}

  onYearChange(): void {
    this.loadSalaryHistory();
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'COMPLETED': return 'badge-success';
      case 'PENDING': return 'badge-warning';
      case 'PROCESSING': return 'badge-info';
      case 'FAILED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  downloadSalarySlip(salaryPayment: any): void {
    if (salaryPayment.salarySlipUrl) {
      window.open(salaryPayment.salarySlipUrl, '_blank');
    }
  }

  get totalEarned(): number {
    return this.salaryPayments
      .filter(s => s.status === 'COMPLETED')
      .reduce((sum, s) => sum + s.netSalary, 0);
  }
}