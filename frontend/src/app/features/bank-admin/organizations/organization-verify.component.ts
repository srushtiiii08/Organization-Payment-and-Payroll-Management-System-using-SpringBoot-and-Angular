import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { AlertService } from '../../../core/services/alert.service';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { Organization } from '../../../shared/models/organization.model';

@Component({
  selector: 'app-organization-verify',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, NavbarComponent, SidebarComponent, LoaderComponent],
  templateUrl: './organization-verify.component.html',
  styleUrls: ['./organization-verify.component.css']
})
export class OrganizationVerifyComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private alertService = inject(AlertService);

  organization: Organization | null = null;
  loading = true;
  processing = false;
  organizationId!: number;
  remarksForm!: FormGroup;

  ngOnInit(): void {
    this.organizationId = Number(this.route.snapshot.paramMap.get('id'));
    
    this.remarksForm = this.fb.group({
      remarks: ['']
    });

    this.loadOrganization();
  }

  loadOrganization(): void {
    this.http.get<Organization>(`${environment.apiUrl}/admin/organizations/${this.organizationId}`).subscribe({
      next: (data) => {
        this.organization = data;
        this.loading = false;
        
        if (data.verificationDocumentsUrl) {
          console.log('File Type:', this.getFileType(data.verificationDocumentsUrl));
          console.log('Is PDF:', this.isPDF(data.verificationDocumentsUrl));
          console.log('Is Image:', this.isImage(data.verificationDocumentsUrl));
        }
        console.log('═══════════════════════════════════════');
      },
      error: (error) => {
        console.error('❌ Error loading organization:', error);
        this.loading = false;
        this.alertService.error('Failed to load organization details');
        this.router.navigate(['/admin/organizations']);
      }
    });
  }

  // ✅ CHECK IF URL IS A PDF
  isPDF(url: string): boolean {
    if (!url) return false;
    const lowerUrl = url.toLowerCase();
    return lowerUrl.endsWith('.pdf') || lowerUrl.includes('.pdf');
  }

  // ✅ CHECK IF URL IS AN IMAGE
  isImage(url: string): boolean {
    if (!url) return false;
    const lowerUrl = url.toLowerCase();
    return lowerUrl.endsWith('.jpg') || 
           lowerUrl.endsWith('.jpeg') || 
           lowerUrl.endsWith('.png') ||
           lowerUrl.includes('/image/') ||
           lowerUrl.includes('image%2F');
  }

  // ✅ GET FILE TYPE DISPLAY NAME
  getFileType(url: string): string {
    if (!url) return 'Unknown';
    
    if (this.isPDF(url)) return 'PDF Document';
    if (this.isImage(url)) {
      if (url.toLowerCase().includes('.jpg') || url.toLowerCase().includes('.jpeg')) {
        return 'JPEG Image';
      }
      if (url.toLowerCase().includes('.png')) {
        return 'PNG Image';
      }
      return 'Image File';
    }
    
    return 'Document';
  }

  // ✅ GET FILENAME FROM URL
  getFileName(url: string): string {
    if (!url) return 'document';
    
    try {
      // Extract filename from Cloudinary URL
      const parts = url.split('/');
      const filenamePart = parts[parts.length - 1];
      
      // Remove any query parameters
      const filename = filenamePart.split('?')[0];
      
      return filename || 'verification_document';
    } catch (error) {
      return 'verification_document';
    }
  }

  // ✅ HANDLE IMAGE LOAD ERROR
  onImageError(event: any): void {
    // console.error('❌ Failed to load image:', event);
    // this.alertService.error('Failed to load document preview');
    
    // Hide the broken image
    event.target.style.display = 'none';
  }

  verifyOrganization(): void {
    if (!confirm('Are you sure you want to verify this organization? This will allow them to access the system.')) {
      return;
    }

    this.processing = true;

    const payload = {
      verified: true,
      remarks: this.remarksForm.value.remarks || null
    };

    this.http.post(`${environment.apiUrl}/admin/organizations/${this.organizationId}/verify`, payload).subscribe({
      next: () => {
        this.processing = false;
        this.alertService.success('Organization verified successfully');
        this.loadOrganization();
      },
      error: (error) => {
        this.processing = false;
        console.error('❌ Error verifying organization:', error);
      }
    });
  }

  rejectOrganization(): void {
    if (!confirm('Are you sure you want to reject this organization verification?')) {
      return;
    }

    this.processing = true;

    const payload = {
      verified: false,
      remarks: this.remarksForm.value.remarks || null
    };

    this.http.post(`${environment.apiUrl}/admin/organizations/${this.organizationId}/verify`, payload).subscribe({
      next: () => {
        this.processing = false;
        this.alertService.success('Organization verification rejected');
        this.router.navigate(['/admin/organizations']);
      },
      error: (error) => {
        this.processing = false;
        console.error('❌ Error rejecting organization:', error);
      }
    });
  }
}