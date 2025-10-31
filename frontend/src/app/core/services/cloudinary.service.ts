import { Injectable } from '@angular/core';
import { Observable, from } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CloudinaryService {
  
  private cloudName = 'dhf6btiqm';
  private uploadPreset = 'Payroll_Cloud';
  private apiUrl = `https://api.cloudinary.com/v1_1/${this.cloudName}/upload`;

  /**
   * Upload file to Cloudinary using native fetch (bypasses HTTP interceptors)
   * @param file - File to upload
   * @param folder - Optional folder name in Cloudinary
   * @returns Observable with upload response
   */
  uploadFile(file: File, folder: string = 'payroll'): Observable<CloudinaryResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.uploadPreset);
    formData.append('folder', folder);

    // Use native fetch instead of HttpClient to bypass Angular interceptors
    const uploadPromise = fetch(this.apiUrl, {
      method: 'POST',
      body: formData
      // No headers needed - let the browser set Content-Type with boundary
    }).then(response => {
      if (!response.ok) {
        throw new Error(`Upload failed: ${response.statusText}`);
      }
      return response.json();
    });

    return from(uploadPromise);
  }

  /**
   * Upload image with specific transformations
   * @param file - Image file to upload
   * @param folder - Optional folder name
   * @returns Observable with secure URL
   */
  uploadImage(file: File, folder: string = 'payroll/images'): Observable<string> {
    return this.uploadFile(file, folder).pipe(
      map(response => response.secure_url)
    );
  }

  /**
   * Upload document (PDF, DOCX, etc.)
   * @param file - Document file to upload
   * @param folder - Optional folder name
   * @returns Observable with secure URL
   */
  uploadDocument(file: File, folder: string = 'payroll/documents'): Observable<string> {
    return this.uploadFile(file, folder).pipe(
      map(response => response.secure_url)
    );
  }

  /**
   * Delete file from Cloudinary
   * @param publicId - Public ID of the file to delete
   * @returns Observable with deletion response
   */
  deleteFile(publicId: string): Observable<any> {
    // Note: Deletion requires server-side implementation due to signature requirements
    // This is a placeholder - implement on your backend
    console.warn('File deletion should be handled on the backend');
    return new Observable(observer => {
      observer.error('Deletion must be implemented on backend');
    });
  }

  /**
   * Validate file before upload
   * @param file - File to validate
   * @param maxSize - Maximum file size in MB (default: 5MB)
   * @param allowedTypes - Array of allowed MIME types
   * @returns Validation result
   */
  validateFile(
    file: File,
    maxSize: number = 5,
    allowedTypes: string[] = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf']
  ): FileValidationResult {
    const maxSizeBytes = maxSize * 1024 * 1024;

    if (file.size > maxSizeBytes) {
      return {
        valid: false,
        error: `File size exceeds ${maxSize}MB limit`
      };
    }

    if (!allowedTypes.includes(file.type)) {
      return {
        valid: false,
        error: `File type ${file.type} is not allowed. Allowed types: ${allowedTypes.join(', ')}`
      };
    }

    return { valid: true };
  }
}

// Interfaces
export interface CloudinaryResponse {
  asset_id: string;
  public_id: string;
  version: number;
  version_id: string;
  signature: string;
  width: number;
  height: number;
  format: string;
  resource_type: string;
  created_at: string;
  tags: string[];
  bytes: number;
  type: string;
  etag: string;
  placeholder: boolean;
  url: string;
  secure_url: string;
  folder: string;
  original_filename: string;
}

export interface FileValidationResult {
  valid: boolean;
  error?: string;
}