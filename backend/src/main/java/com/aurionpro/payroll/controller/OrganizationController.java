package com.aurionpro.payroll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.OrganizationRequest;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.OrganizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/org")
public class OrganizationController {

	@Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationResponse> getMyOrganization(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        
        OrganizationResponse response = organizationService.getOrganizationByUserId(userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse currentOrg = organizationService.getOrganizationByUserId(userId);
        
        if (!currentOrg.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        OrganizationResponse response = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/documents")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationResponse> uploadDocuments(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse currentOrg = organizationService.getOrganizationByUserId(userId);
        
        if (!currentOrg.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        OrganizationResponse response = organizationService.uploadVerificationDocuments(id, file);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BANK_ADMIN', 'ORGANIZATION')")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(response);
    }
}
