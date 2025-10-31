import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { Concern, ConcernStatus, ConcernPriority } from '../../../shared/models/concern.model';

@Component({
  selector: 'app-emp-concern-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, SidebarComponent],
  template: `
    <app-navbar></app-navbar>
    <app-sidebar></app-sidebar>

    <div class="main-content">
      <div class="container">
        <!-- Header -->
        <div class="page-header">
          <div>
            <h1>Concern Details</h1>
            <p>View your concern status and responses</p>
          </div>
          <a routerLink="/employee/concerns" class="btn btn-secondary">
            ← Back to List
          </a>
        </div>

        <!-- Loading -->
        <div *ngIf="loading" class="loading-state">
          <div class="spinner"></div>
          <p>Loading concern details...</p>
        </div>

        <!-- Concern Details -->
        <div *ngIf="!loading && concern" class="details-container">
          <!-- Status Card -->
          <div class="card status-card">
            <div class="status-header">
              <h3>Current Status</h3>
              <span class="badge large" [ngClass]="getStatusClass(concern.status)">
                {{ concern.status }}
              </span>
              <span class="priority-badge large" [ngClass]="getPriorityClass(concern.priority)">
                {{ concern.priority }} PRIORITY
              </span>
            </div>
          </div>

          <!-- Concern Information -->
          <div class="card">
            <div class="card-header">
              <h3>Concern Information</h3>
            </div>

            <div class="info-grid">
              <div class="info-item">
                <label>Concern ID</label>
                <p>#{{ concern.id }}</p>
              </div>

              <div class="info-item">
                <label>Date Raised</label>
                <p>{{ concern.createdAt | date: 'dd MMM yyyy, hh:mm a' }}</p>
              </div>

              <div class="info-item full-width">
                <label>Subject</label>
                <p class="subject-text">{{ concern.subject }}</p>
              </div>

              <div class="info-item full-width">
                <label>Description</label>
                <p class="description-text">{{ concern.description }}</p>
              </div>

              <div class="info-item full-width" *ngIf="concern.attachmentUrl">
                <label>Attachment</label>
                <p>
                  <a [href]="concern.attachmentUrl" target="_blank" class="attachment-link">
                    📎 View Attachment
                  </a>
                </p>
              </div>
            </div>
          </div>

          <!-- Response Section -->
          <div class="card" *ngIf="concern.response">
            <div class="card-header success">
              <h3>✓ Organization Response</h3>
            </div>

            <div class="response-content">
              <p>{{ concern.response }}</p>
              <div class="response-meta">
                <span *ngIf="concern.respondedByName">Responded by: {{ concern.respondedByName }}</span>
                <span *ngIf="concern.respondedAt">{{ concern.respondedAt | date: 'dd MMM yyyy, hh:mm a' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .main-content {
      margin-left: 0;
      padding: 20px;
      min-height: 100vh;
      background: #f8f9fa;
    }

    .container {
      max-width: 1000px;
      margin: 0 auto;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
      padding: 30px;
      margin-bottom: 20px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 25px;
    }

    .info-item.full-width {
      grid-column: 1 / -1;
    }

    .info-item label {
      font-size: 12px;
      font-weight: 600;
      color: #666;
      text-transform: uppercase;
      margin-bottom: 8px;
      display: block;
    }

    .info-item p {
      font-size: 16px;
      color: #333;
      margin: 0;
    }

    .subject-text {
      font-size: 18px !important;
      font-weight: 600 !important;
      color: #667eea !important;
    }

    .description-text {
      line-height: 1.6;
      padding: 15px;
      background: #f8f9fa;
      border-left: 3px solid #667eea;
      border-radius: 4px;
    }

    .badge {
      display: inline-block;
      padding: 6px 14px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
      margin-right: 10px;
    }

    .badge.large {
      padding: 8px 16px;
      font-size: 14px;
    }

    .badge-success { background-color: #d4edda; color: #155724; }
    .badge-warning { background-color: #fff3cd; color: #856404; }
    .badge-info { background-color: #d1ecf1; color: #0c5460; }
    .badge-secondary { background-color: #e2e3e5; color: #383d41; }

    .priority-badge {
      display: inline-block;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 11px;
      font-weight: 600;
      text-transform: uppercase;
    }

    .priority-badge.large {
      padding: 8px 16px;
      font-size: 12px;
    }

    .priority-low { background-color: #d1ecf1; color: #0c5460; }
    .priority-medium { background-color: #fff3cd; color: #856404; }
    .priority-high { background-color: #f8d7da; color: #721c24; }
    .priority-critical { background-color: #f5c6cb; color: #721c24; }

    .btn {
      padding: 10px 20px;
      border: none;
      border-radius: 8px;
      text-decoration: none;
      font-weight: 600;
      background-color: #6c757d;
      color: white;
    }

    .loading-state {
      text-align: center;
      padding: 60px 20px;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .response-content {
      padding: 20px;
      background: #e8f5e9;
      border-left: 4px solid #28a745;
      border-radius: 8px;
    }

    .response-meta {
      display: flex;
      justify-content: space-between;
      font-size: 13px;
      color: #666;
      padding-top: 15px;
      border-top: 1px solid rgba(40, 167, 69, 0.2);
    }

    .attachment-link {
      display: inline-block;
      padding: 8px 16px;
      background: #667eea;
      color: white;
      text-decoration: none;
      border-radius: 6px;
    }
  `]
})
export class EmpConcernDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);

  concern: Concern | null = null;
  loading = true;
  concernId!: number;

  ngOnInit(): void {
    this.concernId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadConcern();
  }

  loadConcern(): void {
    this.http.get<Concern>(`${environment.apiUrl}/employee/concerns/${this.concernId}`).subscribe({
      next: (data) => {
        this.concern = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading concern:', error);
        this.loading = false;
        this.router.navigate(['/employee/concerns']);
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