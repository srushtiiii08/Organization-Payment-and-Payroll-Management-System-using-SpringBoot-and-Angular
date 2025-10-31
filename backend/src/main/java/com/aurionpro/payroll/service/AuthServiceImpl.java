package com.aurionpro.payroll.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.EmployeeRegisterRequest;
import com.aurionpro.payroll.dto.request.LoginRequest;
import com.aurionpro.payroll.dto.request.OrganizationRegisterRequest;
import com.aurionpro.payroll.dto.request.OrganizationRequest;
import com.aurionpro.payroll.dto.request.ResetPasswordRequest;
import com.aurionpro.payroll.dto.response.LoginResponse;
import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.enums.Role;
import com.aurionpro.payroll.enums.UserStatus;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.DuplicateResourceException;
import com.aurionpro.payroll.exception.InvalidCredentialsException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.EmployeeRepo;
import com.aurionpro.payroll.repo.UserRepo;
import com.aurionpro.payroll.security.JwtTokenProvider;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService{

	@Autowired
    private UserRepo userRepo;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private InMemoryCaptchaService captchaService;
    
    
    
    @Override
    public LoginResponse login(LoginRequest request) {
    	 
    	//validate captacha first 
    	if (!captchaService.validateCaptcha(request.getCaptchaSessionId(), request.getCaptchaAnswer())) {
            throw new BadRequestException("Incorrect CAPTCHA answer. Please try again.");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));
            
            if (user.getStatus() == UserStatus.INACTIVE) {
                throw new BadRequestException("Account is inactive. Please contact administrator.");
            }
            

            //CHECK IF EMPLOYEE NEEDS TO SET PASSWORD
            if (user.getRole() == Role.EMPLOYEE && user.getStatus() == UserStatus.PENDING) {
                throw new BadRequestException("Please complete your registration by setting a password first. Visit /api/auth/register/employee");
            }
            
            
            String token = jwtUtil.generateToken(user.getEmail());
            
            LoginResponse response = modelMapper.map(user, LoginResponse.class);
            response.setToken(token);
            response.setMessage("Login successful");
            
            return response;
            
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
    
    
    
    @Override
    public LoginResponse registerOrganization(OrganizationRegisterRequest request, MultipartFile file) {
        
        // 🔍 CHECK IF EMAIL ALREADY EXISTS
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        
     // 📤 UPLOAD DOCUMENT TO CLOUDINARY FIRST
        String documentUrl = cloudinaryService.uploadFile(file, "organization_documents");
        
        // 👤 CREATE USER ACCOUNT
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.ORGANIZATION)
            .status(UserStatus.PENDING)
            .build();
        
        User savedUser = userRepo.save(user);
        
        // 🏢 CREATE ORGANIZATION PROFILE
        OrganizationRequest orgRequest = OrganizationRequest.builder()
            .name(request.getName())
            .registrationNumber(request.getRegistrationNumber())
            .address(request.getAddress())
            .contactPhone(request.getContactPhone())
            .verificationDocumentsUrl(documentUrl)  // 🆕 SET DOCUMENT URL
            .build();
        
        organizationService.createOrganization(orgRequest, savedUser.getId());
        
        // GENERATE JWT TOKEN
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        // SEND WELCOME EMAIL (async - doesn't block registration)
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), request.getName());
            System.out.println("📧 Welcome email sent");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send welcome email: " + e.getMessage());
        }
        
        // 📦 PREPARE RESPONSE
        LoginResponse response = modelMapper.map(savedUser, LoginResponse.class);
        response.setToken(token);
        response.setMessage("Registration successful. Pending admin verification.");
        
        return response;
    }
    
    
    

    //Employee completes registration
    @Override
    public String registerEmployee(EmployeeRegisterRequest request) {
        // Find user by email
        User user = userRepo.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No employee account found with this email. Please contact your organization."));
        
        // Verify it's an employee account
        if (user.getRole() != Role.EMPLOYEE) {
            throw new BadRequestException("This email is not registered as an employee");
        }
        
        // Check if already registered
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("Account already registered. Please login.");
        }
        
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }
        
        // Find employee
        Employee employee = employeeRepo.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
        
        // Update employee with document proof URL
        employee.setAccountProofUrl(request.getDocumentProofUrl());
        employeeRepo.save(employee);
        
        // Set password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        
        // Send confirmation email
        try {
            emailService.sendEmployeeRegistrationConfirmation(
                user.getEmail(),
                employee.getName()
            );
        } catch (Exception e) {
            System.err.println("❌ Failed to send confirmation email: " + e.getMessage());
        }
        
        return "Registration completed successfully. Please login with your credentials.";
    }
    
    
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
    
    
    //SEND PASSWORD RESET OTP
    @Override
    public String sendPasswordResetOtp(String email) {
        // Find user by email
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> {
                System.out.println("❌ User not found");
                return new ResourceNotFoundException("No account found with this email address");
            });
        
        // Generate 6-digit OTP
        String otp = generateOtp();   //..method defined below
        
        // Set OTP and expiry (10 minutes from now)
        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepo.save(user);
        
        // Send OTP via email
        try {
            emailService.sendPasswordResetOtp(email, otp);
        } catch (Exception e) {
            throw new BadRequestException("Failed to send OTP email. Please try again.");
        }
        
        return "OTP sent successfully to your email. Please check your inbox.";
    }

    //VERIFY OTP AND RESET PASSWORD
    @Override
    public String verifyOtpAndResetPassword(ResetPasswordRequest request) {
        // Find user
        User user = userRepo.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                System.out.println("❌ User not found");
                return new ResourceNotFoundException("No account found with this email address");
            });
        
        // Check if OTP exists
        if (user.getResetOtp() == null || user.getOtpExpiry() == null) {
            throw new BadRequestException("No OTP request found. Please request a new OTP.");
        }
        
        // Check if OTP has expired
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            // Clear expired OTP
            user.setResetOtp(null);
            user.setOtpExpiry(null);
            userRepo.save(user);
            
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }
        
        // Verify OTP matches
        if (!user.getResetOtp().equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP. Please check and try again.");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Clear OTP (one-time use)
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        
        userRepo.save(user);
        return "Password reset successfully. You can now login with your new password.";
    }

    //GENERATE RANDOM 6-DIGIT OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // Generates number between 100000-999999
        return String.valueOf(otp);
    }

}
