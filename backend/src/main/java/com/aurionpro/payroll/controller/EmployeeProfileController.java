package com.aurionpro.payroll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.ProfilePictureRequest;
import com.aurionpro.payroll.dto.response.EmployeeProfile;
import com.aurionpro.payroll.dto.response.EmployeeResponse;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeProfileController {

	@Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @GetMapping("/profile")
    public ResponseEntity<EmployeeProfile> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        
        EmployeeProfile profile = employeeService.getEmployeeProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/profile/picture")
    public ResponseEntity<?> updateProfilePicture(
            @Valid @RequestBody ProfilePictureRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        employeeService.updateProfilePicture(employee.getId(), request.getProfilePictureUrl());
        
        return ResponseEntity.ok().body(
            new java.util.HashMap<String, String>() {{
                put("message", "Profile picture updated successfully");
                put("profilePictureUrl", request.getProfilePictureUrl());
            }}
        );
    }
    
    
    @PostMapping("/upload-account-proof")
    public ResponseEntity<EmployeeResponse> uploadAccountProof(
    		@RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        EmployeeResponse response = employeeService.uploadAccountProof(employee.getId(), file);
        return ResponseEntity.ok(response);
    }
}
