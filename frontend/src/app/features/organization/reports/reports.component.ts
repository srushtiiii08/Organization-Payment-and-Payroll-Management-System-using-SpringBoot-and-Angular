import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';

interface ReportResponse {
  message: string;
  reportUrl: string;
  fileName: string;
  type: string;
  reportType: string;
  month?: string;
  year?: string;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, SidebarComponent],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  private http = inject(HttpClient);

  // Form data
  selectedYear: number = new Date().getFullYear();
  selectedMonth: string = '';
  
  // Loading states
  loadingEmployeeExcel = false;
  loadingSalaryExcel = false;
  loadingSalaryPdf = false;
  
  // Years and months
  years: number[] = [];
  months = [
    'JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE',
    'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER'
  ];

  // Generated report URLs
  generatedReports: ReportResponse[] = [];

  ngOnInit(): void {
    // Generate last 5 years
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 5; i++) {
      this.years.push(currentYear - i);
    }
  }

  // ==========================================
  // EMPLOYEE LIST REPORTS
  // ==========================================

  /**
   * Download Employee List Excel - Direct Download
   */
  downloadEmployeeListExcel(): void {
    this.loadingEmployeeExcel = true;
    
    this.http.get(`${environment.apiUrl}/reports/employees/excel/download`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        this.downloadBlob(blob, 'employee_list.xlsx');
        this.loadingEmployeeExcel = false;
        alert('✅ Employee list downloaded successfully!');
      },
      error: (error) => {
        console.error('Error downloading employee list:', error);
        alert('❌ Failed to download employee list');
        this.loadingEmployeeExcel = false;
      }
    });
  }

  /**
   * Generate Employee List Excel - Returns URL
   */
  generateEmployeeListExcel(): void {
    this.loadingEmployeeExcel = true;

    this.http.get<ReportResponse>(`${environment.apiUrl}/reports/employees/excel`).subscribe({
      next: (response) => {
        this.generatedReports.unshift(response); // Add to top of list
        this.loadingEmployeeExcel = false;
        alert('✅ Employee list generated successfully!');
      },
      error: (error) => {
        console.error('Error generating employee list:', error);
        alert('❌ Failed to generate employee list');
        this.loadingEmployeeExcel = false;
      }
    });
  }

  // ==========================================
  // SALARY REPORT - EXCEL
  // ==========================================

  /**
   * Download Salary Report Excel - Direct Download
   */
  downloadSalaryReportExcel(): void {
    if (!this.selectedMonth) {
      alert('⚠️ Please select a month');
      return;
    }

    this.loadingSalaryExcel = true;
    
    const params = {
      month: this.selectedMonth,
      year: this.selectedYear.toString()
    };

    this.http.get(`${environment.apiUrl}/reports/salary-report/excel/download`, {
      params,
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        this.downloadBlob(blob, `salary_report_${this.selectedMonth}_${this.selectedYear}.xlsx`);
        this.loadingSalaryExcel = false;
        alert('✅ Salary report downloaded successfully!');
      },
      error: (error) => {
        console.error('Error downloading salary report:', error);
        alert('❌ Failed to download salary report');
        this.loadingSalaryExcel = false;
      }
    });
  }

  /**
   * Generate Salary Report Excel - Returns URL
   */
  generateSalaryReportExcel(): void {
    if (!this.selectedMonth) {
      alert('⚠️ Please select a month');
      return;
    }

    this.loadingSalaryExcel = true;
    
    const params = {
      month: this.selectedMonth,
      year: this.selectedYear.toString()
    };

    this.http.get<ReportResponse>(`${environment.apiUrl}/reports/salary-report/excel`, { params }).subscribe({
      next: (response) => {
        this.generatedReports.unshift(response);
        this.loadingSalaryExcel = false;
        alert('✅ Salary report generated successfully!');
      },
      error: (error) => {
        console.error('Error generating salary report:', error);
        alert('❌ Failed to generate salary report');
        this.loadingSalaryExcel = false;
      }
    });
  }

  // ==========================================
  // SALARY REPORT - PDF
  // ==========================================

  /**
   * Download Salary Report PDF - Direct Download
   */
  downloadSalaryReportPdf(): void {
    if (!this.selectedMonth) {
      alert('⚠️ Please select a month');
      return;
    }

    this.loadingSalaryPdf = true;
    
    const params = {
      month: this.selectedMonth,
      year: this.selectedYear.toString()
    };

    this.http.get(`${environment.apiUrl}/reports/salary-report/pdf/download`, {
      params,
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        this.downloadBlob(blob, `salary_report_${this.selectedMonth}_${this.selectedYear}.pdf`);
        this.loadingSalaryPdf = false;
        alert('✅ Salary report PDF downloaded successfully!');
      },
      error: (error) => {
        console.error('Error downloading salary report PDF:', error);
        alert('❌ Failed to download salary report PDF');
        this.loadingSalaryPdf = false;
      }
    });
  }

  /**
   * Generate Salary Report PDF - Returns URL
   */
  generateSalaryReportPdf(): void {
    if (!this.selectedMonth) {
      alert('⚠️ Please select a month');
      return;
    }

    this.loadingSalaryPdf = true;
    
    const params = {
      month: this.selectedMonth,
      year: this.selectedYear.toString()
    };

    this.http.get<ReportResponse>(`${environment.apiUrl}/reports/salary-report/pdf`, { params }).subscribe({
      next: (response) => {
        this.generatedReports.unshift(response);
        this.loadingSalaryPdf = false;
        alert('✅ Salary report PDF generated successfully!');
      },
      error: (error) => {
        console.error('Error generating salary report PDF:', error);
        alert('❌ Failed to generate salary report PDF');
        this.loadingSalaryPdf = false;
      }
    });
  }

  // ==========================================
  // HELPER METHODS
  // ==========================================

  /**
   * Download blob as file
   */
  private downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  /**
   * Open report URL in new tab
   */
  openReport(url: string): void {
    window.open(url, '_blank');
  }

  /**
   * Delete report from list
   */
  deleteReport(index: number): void {
    if (confirm('Remove this report from the list?')) {
      this.generatedReports.splice(index, 1);
    }
  }

  /**
   * Get icon for report type
   */
  getReportIcon(type: string): string {
    return type === 'PDF' ? '📄' : '📊';
  }

  /**
   * Get badge color for report type
   */
  getReportBadgeClass(type: string): string {
    return type === 'PDF' ? 'badge-pdf' : 'badge-excel';
  }
}
