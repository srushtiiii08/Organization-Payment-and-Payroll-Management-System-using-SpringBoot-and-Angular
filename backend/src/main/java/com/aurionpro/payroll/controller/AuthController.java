package com.aurionpro.payroll.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.EmployeeRegisterRequest;
import com.aurionpro.payroll.dto.request.ForgotPasswordRequest;
import com.aurionpro.payroll.dto.request.LoginRequest;
import com.aurionpro.payroll.dto.request.OrganizationRegisterRequest;
import com.aurionpro.payroll.dto.request.ResetPasswordRequest;
import com.aurionpro.payroll.dto.response.LoginResponse;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.AuthService;
import com.aurionpro.payroll.service.InMemoryCaptchaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
    private AuthService authService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private InMemoryCaptchaService captchaService;  
    
    
    
    
    @GetMapping("/captcha")
    public ResponseEntity<Map<String, String>> generateCaptcha() {
        // Generate captcha (returns "sessionId:data:image/png;base64,...")
        String captchaData = captchaService.generateCaptcha();
        String[] parts = captchaData.split(":", 2);
        
        return ResponseEntity.ok(Map.of(
            "sessionId", parts[0],        // UUID session ID
            "imageData", parts[1]          // data:image/png;base64,<image>
        ));
    }
    
//    @GetMapping("/captcha")
//    public ResponseEntity<Map<String, String>> generateCaptcha() {
//        
//        // Generate captcha (returns "sessionId:question")
//        String captchaData = captchaService.generateCaptcha();
//        String[] parts = captchaData.split(":");
//        
//        return ResponseEntity.ok(Map.of(
//            "sessionId", parts[0],           // UUID session ID
//            "question", parts[1] + " = ?"    // Math question (e.g., "5+3 = ?")
//        ));
//    }
    
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register/organization")   //@@RequestPart is for multipart/form-data
    public ResponseEntity<LoginResponse> registerOrganization(		//@RequestBody for normal json data
            @Valid @RequestPart("organization") OrganizationRegisterRequest request,
            @RequestPart("file") MultipartFile file) {
        
        // 🔍 VALIDATE FILE IS PROVIDED
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Verification document is required for registration");
        }
        
        LoginResponse response = authService.registerOrganization(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/employee")
    public ResponseEntity<Map<String, String>> registerEmployee(
            @Valid @RequestBody EmployeeRegisterRequest request) {
        String message = authService.registerEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of("message", message)
        );
    }
    
    
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        authService.changePassword(userId, oldPassword, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
    
 // EQUEST PASSWORD RESET OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        String message = authService.sendPasswordResetOtp(request.getEmail());
        
        return ResponseEntity.ok(Map.of(
            "message", message,
            "email", request.getEmail()
        ));
    }

    //RESET PASSWORD WITH OTP
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.verifyOtpAndResetPassword(request);
        
        return ResponseEntity.ok(Map.of(
            "message", message
        ));
    }
}
