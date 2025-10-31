import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { Concern, ConcernStatus, ConcernPriority } from '../../../shared/models/concern.model';

@Component({
  selector: 'app-org-concern-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, NavbarComponent, SidebarComponent, LoaderComponent],
  templateUrl: './org-concern-detail.component.html',
  styleUrls: ['./org-concern-detail.component.css']
})
export class OrgConcernDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private alertService = inject(AlertService);

  concern: Concern | null = null;
  loading = true;
  processing = false;
  concernId!: number;
  responseForm!: FormGroup;

  ConcernStatus = ConcernStatus;

  ngOnInit(): void {
    this.concernId = Number(this.route.snapshot.paramMap.get('id'));
    
    this.responseForm = this.fb.group({
      response: ['', [Validators.required, Validators.minLength(20)]]
    });

    this.loadConcern();
  }

  loadConcern(): void {
    this.http.get<Concern>(`${environment.apiUrl}/org/concerns/${this.concernId}`).subscribe({
      next: (data) => {
        this.concern = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading concern:', error);
        this.loading = false;
        this.router.navigate(['/organization/concerns']);
      }
    });
  }

  
  updateStatus(status: ConcernStatus): void {
  this.processing = true;

  // ✅ FIXED: Send status as string, not enum
  const payload = { status: status.toString() };
  console.log('📤 Updating concern status:', payload);

  this.http.put(`${environment.apiUrl}/org/concerns/${this.concernId}/status`, payload).subscribe({
    next: (response) => {
      this.processing = false;
      console.log('✅ Status updated successfully:', response);
      this.alertService.success(`Status updated to ${status}`);
      this.loadConcern();
    },
    error: (error) => {
      this.processing = false;
      console.error('❌ Error updating status:', error);
      this.alertService.error(error.error?.message || 'Failed to update status');
    }
    });
  }

  submitResponse(): void {
    if (this.responseForm.invalid) {
      this.responseForm.get('response')?.markAsTouched();
      return;
    }

    this.processing = true;

    this.http.post(`${environment.apiUrl}/org/concerns/${this.concernId}/respond`, 
      this.responseForm.value
    ).subscribe({
      next: () => {
        this.processing = false;
        this.alertService.success('Response submitted successfully');
        this.responseForm.reset();
        this.loadConcern();
      },
      error: (error) => {
        this.processing = false;
        console.error('Error submitting response:', error);
      }
    });
  }

  getStatusClass(status: ConcernStatus): string {
    switch(status) {
      case ConcernStatus.OPEN: return 'badge-warning';
      case ConcernStatus.IN_PROGRESS: return 'badge-info';
      case ConcernStatus.RESOLVED: return 'badge-success';
      case ConcernStatus.CLOSED: return 'badge-secondary';
      default: return 'badge-secondary';
    }
  }

  getPriorityClass(priority: ConcernPriority): string {
    switch(priority) {
      case ConcernPriority.LOW: return 'priority-low';
      case ConcernPriority.MEDIUM: return 'priority-medium';
      case ConcernPriority.HIGH: return 'priority-high';
      case ConcernPriority.CRITICAL: return 'priority-critical';
      default: return 'priority-low';
    }
  }
}