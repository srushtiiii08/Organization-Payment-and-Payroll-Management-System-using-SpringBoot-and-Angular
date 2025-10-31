export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  
  // Cloudinary Configuration
  cloudinary: {
    cloudName: 'dhf6btiqm',
    uploadPreset: 'Payroll_Cloud',
    apiKey: '886888634596359'                // Only needed for admin operations
  },
  
  // Email Configuration (Backend will handle actual sending)
  email: {
    supportEmail: 'support@payrollsystem.com',
    noReplyEmail: 'noreply@payrollsystem.com'
  },
  
  // App Configuration
  app: {
    name: 'Payroll Management System',
    version: '1.0.0',
    maxFileUploadSize: 5, // MB
    allowedFileTypes: ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'],
    paginationPageSize: 10
  }
};