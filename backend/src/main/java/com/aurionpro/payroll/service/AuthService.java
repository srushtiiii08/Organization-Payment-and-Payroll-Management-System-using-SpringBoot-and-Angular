package com.aurionpro.payroll.service;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.EmployeeRegisterRequest;
import com.aurionpro.payroll.dto.request.LoginRequest;
import com.aurionpro.payroll.dto.request.OrganizationRegisterRequest;
import com.aurionpro.payroll.dto.request.ResetPasswordRequest;
import com.aurionpro.payroll.dto.response.LoginResponse;

public interface AuthService {

	 LoginResponse login(LoginRequest request);
	    
	 LoginResponse registerOrganization(OrganizationRegisterRequest request, MultipartFile file);

	 String registerEmployee(EmployeeRegisterRequest request);
	    
	 void changePassword(Long userId, String oldPassword, String newPassword);
	 
	 //METHODS FOR FORGOT PASSWORD
	 String sendPasswordResetOtp(String email);
	 String verifyOtpAndResetPassword(ResetPasswordRequest request);
}
