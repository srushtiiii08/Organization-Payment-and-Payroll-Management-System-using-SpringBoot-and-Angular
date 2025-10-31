import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { ConcernPriority } from '../../../shared/models/concern.model';

@Component({
  selector: 'app-emp-concern-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent, SidebarComponent, LoaderComponent],
  templateUrl: './emp-concern-create.component.html',
  styleUrls: ['./emp-concern-create.component.css']
})
export class EmpConcernCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private alertService = inject(AlertService);

  concernForm!: FormGroup;
  loading = false;
  uploading = false;
  selectedFile: File | null = null;
  uploadedFileUrl: string | null = null;

  priorities = [
    { value: ConcernPriority.LOW, label: 'Low' },
    { value: ConcernPriority.MEDIUM, label: 'Medium' },
    { value: ConcernPriority.HIGH, label: 'High' },
    { value: ConcernPriority.CRITICAL, label: 'Critical' }
  ];

  ngOnInit(): void {
    this.concernForm = this.fb.group({
      subject: ['', [Validators.required, Validators.minLength(5)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      priority: [ConcernPriority.MEDIUM, [Validators.required]]
    });
  }

  get f() {
    return this.concernForm.controls;
  }

  // ⭐ FIX: Proper file validation
  onFileSelect(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    // ✅ Validate file size (max 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB in bytes
    if (file.size > maxSize) {
      this.alertService.error('File size must be less than 5MB');
      return;
    }

    // ✅ Validate file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
    if (!allowedTypes.includes(file.type)) {
      this.alertService.error('Only JPG, PNG, and PDF files are allowed');
      return;
    }

    this.selectedFile = file;
    this.uploadFile();
  }

  // ⭐ FIX: Upload file to backend
  uploadFile(): void {
    if (!this.selectedFile) return;

    this.uploading = true;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    // ✅ CORRECT ENDPOINT: Backend expects this exact URL
    this.http.post<any>(`${environment.apiUrl}/files/upload/concern-attachment`, formData).subscribe({
      next: (response) => {
        this.uploadedFileUrl = response.fileUrl;
        this.uploading = false;
        this.alertService.success('File uploaded successfully');
        console.log('✅ File uploaded:', response);
      },
      error: (error) => {
        this.uploading = false;
        console.error('❌ Error uploading file:', error);
        this.alertService.error(error.error?.message || 'Failed to upload file');
      }
    });
  }

  removeFile(): void {
    this.selectedFile = null;
    this.uploadedFileUrl = null;
  }

  // ⭐ FIX: Submit concern to correct endpoint
  onSubmit(): void {
    if (this.concernForm.invalid) {
      Object.keys(this.concernForm.controls).forEach(key => {
        this.concernForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;

    const payload = {
      subject: this.concernForm.value.subject,
      description: this.concernForm.value.description,
      priority: this.concernForm.value.priority,
      attachmentUrl: this.uploadedFileUrl  // ✅ Include uploaded file URL
    };

    console.log('📤 Submitting concern:', payload);

    // ✅ CORRECT ENDPOINT: /api/employee/concerns
    this.http.post(`${environment.apiUrl}/employee/concerns`, payload).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('✅ Concern created:', response);
        this.alertService.success('Concern raised successfully');
        this.router.navigate(['/employee/concerns']);
      },
      error: (error) => {
        this.loading = false;
        console.error('❌ Error creating concern:', error);
        this.alertService.error(error.error?.message || 'Failed to create concern');
      }
    });
  }

  cancel(): void {
    if (confirm('Are you sure? Any unsaved changes will be lost.')) {
      this.router.navigate(['/employee/concerns']);
    }
  }
}