import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { EmployeeProfile } from '../../../shared/models/employee.model';
import { CloudinaryService } from '../../../core/services/cloudinary.service';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule,NavbarComponent, SidebarComponent],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  private http = inject(HttpClient);
  private cloudinaryService = inject(CloudinaryService);
  private alertService = inject(AlertService);

  employeeProfile: EmployeeProfile | null = null;
  loading = true;
  uploading = false;

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
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

  onFileSelect(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file
    const validation = this.cloudinaryService.validateFile(file, 2, ['image/jpeg', 'image/png', 'image/jpg']);
    if (!validation.valid) {
      this.alertService.error(validation.error || 'Invalid file');
      return;
    }

    this.uploadProfilePicture(file);
  }

  uploadProfilePicture(file: File): void {
    this.uploading = true;

    this.cloudinaryService.uploadImage(file, 'payroll/profiles').subscribe({
      next: (url) => {
        // Update profile with new image URL
        this.http.put(`${environment.apiUrl}/employee/profile/picture`, { profilePictureUrl: url }).subscribe({
          next: () => {
            this.uploading = false;
            this.alertService.success('Profile picture updated successfully');
            this.loadProfile();
          },
          error: (error) => {
            this.uploading = false;
            console.error('Error updating profile picture:', error);
          }
        });
      },
      error: (error) => {
        this.uploading = false;
        console.error('Error uploading image:', error);
        this.alertService.error('Failed to upload image');
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
}