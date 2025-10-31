package com.aurionpro.payroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.exception.EmailSendException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Override
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Payroll Management System");
            message.setText("Dear " + name + ",\n\n" +
                "Welcome to the Payroll Management System!\n\n" +
                "Your account has been created successfully.\n\n" +
                "Best Regards,\n" +
                "Payroll System Team");
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send welcome email: " + e.getMessage());
        }
    }
    
    @Override
    public void sendOrganizationVerificationEmail(String toEmail, String organizationName, boolean approved) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Organization Verification Status");
            
            if (approved) {
                message.setText("Dear " + organizationName + ",\n\n" +
                    "Congratulations! Your organization has been verified by the bank admin.\n\n" +
                    "You can now access all features of the Payroll Management System.\n\n" +
                    "Best Regards,\n" +
                    "Payroll System Team");
            } else {
                message.setText("Dear " + organizationName + ",\n\n" +
                    "We regret to inform you that your organization verification has been rejected.\n\n" +
                    "Please contact the administrator for more details.\n\n" +
                    "Best Regards,\n" +
                    "Payroll System Team");
            }
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send verification email: " + e.getMessage());
        }
    }
    
    @Override
    public void sendEmployeeAccountVerificationEmail(String toEmail, String employeeName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Employee Account Verified");
            message.setText("Dear " + employeeName + ",\n\n" +
                "Your employee account has been verified successfully!\n\n" +
                "You can now access the Payroll Management System and view your salary information.\n\n" +
                "Best Regards,\n" +
                "Payroll System Team");
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send employee verification email: " + e.getMessage());
        }
    }
    

    // ⭐ UPDATED - Now sends PDF attachment
    @Override
    public void sendSalaryPaymentNotification(String toEmail, String employeeName, String month, Integer year, byte[] pdfBytes) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Salary Payment - " + month + " " + year);
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your salary for %s %d has been processed successfully!\n\n" +
                "Please find your salary slip attached to this email.\n\n" +
                "You can also download it from the system dashboard.\n\n" +
                "Best Regards,\n" +
                "Payroll System Team",
                employeeName, month, year
            );
            
            helper.setText(emailBody);
            
            //ATTACH PDF
            if (pdfBytes != null && pdfBytes.length > 0) {
                String fileName = String.format("Salary_Slip_%s_%s_%d.pdf", 
                    employeeName.replace(" ", "_"), month, year);
                
                ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes);
                helper.addAttachment(fileName, pdfResource, "application/pdf");
                
            } else {
                System.out.println("⚠️  No PDF to attach");
            }
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send salary notification: " + e.getMessage());
            e.printStackTrace();
            throw new EmailSendException("Failed to send salary payment notification: " + e.getMessage());
        }
    }
    
    
    
    @Override
    public void sendConcernResponseEmail(String toEmail, String employeeName, String concernSubject) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Response to Your Concern: " + concernSubject);
            message.setText("Dear " + employeeName + ",\n\n" +
                "Your organization has responded to your concern regarding: " + concernSubject + "\n\n" +
                "Please log in to the system to view the response.\n\n" +
                "Best Regards,\n" +
                "Payroll System Team");
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send concern response email: " + e.getMessage());
        }
    }
    
    @Override
    public void sendPaymentRequestStatusEmail(String toEmail, String organizationName, String status) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Payment Request Status Update");
            message.setText("Dear " + organizationName + ",\n\n" +
                "Your payment request has been " + status + " by the bank admin.\n\n" +
                "Please log in to the system for more details.\n\n" +
                "Best Regards,\n" +
                "Payroll System Team");
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send payment request status email: " + e.getMessage());
        }
    }
    
    
    
    @Override
    public void sendEmployeeInvitation(String email, String employeeName, String organizationName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Welcome to " + organizationName + " - Complete Your Registration");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "You have been added as an employee to %s.\n\n" +
                "Please complete your registration by setting your password:\n" +
                "1. Go to the registration page\n" +
                "2. Enter your email: %s\n" +
                "3. Set your password\n\n" +
                "Registration URL: http://localhost:4200/employee/register\n\n" +
                "Best regards,\n" +
                "Payroll Management System",
                employeeName, organizationName, email
            ));
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send employee invitation: " + e.getMessage());
        }
    }

    
    @Override
    public void sendEmployeeRegistrationConfirmation(String email, String employeeName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Registration Completed Successfully");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your registration has been completed successfully!\n\n" +
                "You can now login using your email and password.\n\n" +
                "Login URL: http://localhost:4200/login\n\n" +
                "Best regards,\n" +
                "Payroll Management System",
                employeeName
            ));
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send registration confirmation: " + e.getMessage());
        }
    }
    
    
    @Override
    public void sendPasswordResetOtp(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply@payrollsystem.com");
            helper.setTo(email);
            helper.setSubject("Password Reset OTP - Payroll Management System");
            
            String emailContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                 color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .otp-box { background: white; border: 3px dashed #667eea; padding: 20px; 
                                  text-align: center; margin: 20px 0; border-radius: 10px; }
                        .otp-code { font-size: 32px; font-weight: bold; color: #667eea; 
                                   letter-spacing: 8px; font-family: monospace; }
                        .warning { background: #fff3cd; border-left: 4px solid #ffc107; 
                                  padding: 15px; margin: 20px 0; border-radius: 5px; }
                        .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔒 Password Reset Request</h1>
                        </div>
                        <div class="content">
                            <p>Hi there,</p>
                            <p>We received a request to reset your password for the <strong>Payroll Management System</strong>.</p>
                            
                            <p>Your One-Time Password (OTP) is:</p>
                            
                            <div class="otp-box">
                                <div class="otp-code">%s</div>
                            </div>
                            
                            <div class="warning">
                                <strong>⏰ Important:</strong> This OTP will expire in <strong>10 minutes</strong>.
                            </div>
                            
                            <div class="tips">
            <h3>Security Tips:</h3>
            <ul>
            <li>Never share this OTP with anyone</li>
            <li>Our team will never ask for your OTP</li>
            <li>If you didn't request this, please ignore this email</li>
            </ul>
            </div>
                            
                            <div class="footer">
                                <p>This is an automated email. Please do not reply.</p>
                                <p>&copy; 2025 Payroll Management System. All rights reserved.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, otp);
            
            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }
    
    
}