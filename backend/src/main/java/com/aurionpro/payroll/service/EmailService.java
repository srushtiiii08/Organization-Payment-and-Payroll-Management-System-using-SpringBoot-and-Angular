package com.aurionpro.payroll.service;

public interface EmailService {
    
    void sendWelcomeEmail(String toEmail, String name);
    
    void sendOrganizationVerificationEmail(String toEmail, String organizationName, boolean approved);
    
    void sendEmployeeAccountVerificationEmail(String toEmail, String employeeName);
    
    void sendSalaryPaymentNotification(String toEmail, String employeeName, String month, Integer year, byte[] pdfBytes);
    
    void sendConcernResponseEmail(String toEmail, String employeeName, String concernSubject);
    
    void sendPaymentRequestStatusEmail(String toEmail, String organizationName, String status);

    void sendEmployeeInvitation(String email, String employeeName, String organizationName);
    
    void sendEmployeeRegistrationConfirmation(String email, String employeeName);
    
    void sendPasswordResetOtp(String email, String otp);
}