package com.aurionpro.payroll.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.aurionpro.payroll.dto.response.ConcernList;
import com.aurionpro.payroll.dto.response.ConcernResponse;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.enums.ConcernStatus;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.ConcernService;
import com.aurionpro.payroll.service.OrganizationService;

@RestController
@RequestMapping("/api/org/concerns")
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrganizationConcernController {

	@Autowired
    private ConcernService concernService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @GetMapping
    public ResponseEntity<List<ConcernList>> getAllConcerns(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<ConcernList> concerns = concernService.getConcernsByOrganization(org.getId());
        return ResponseEntity.ok(concerns);
    }
    
    
    @GetMapping("/status")
    public ResponseEntity<List<ConcernList>> getConcernsByStatus(
            @RequestParam ConcernStatus status,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<ConcernList> concerns = concernService.getConcernsByStatus(org.getId(), status);
        return ResponseEntity.ok(concerns);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ConcernResponse> getConcernById(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Verify concern belongs to this organization
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        ConcernResponse response = concernService.getConcernById(id);
        
        // Check if concern belongs to this organization
        if (!response.getOrganizationId().equals(org.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/upload-attachment")
    public ResponseEntity<ConcernResponse> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        // Verify concern belongs to this user of organization
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        ConcernResponse concern = concernService.getConcernById(id);
        
        // Check if concern belongs to this organization
        if (!concern.getOrganizationId().equals(org.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        ConcernResponse response = concernService.uploadAttachment(id, file);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/{id}/respond")
    public ResponseEntity<ConcernResponse> respondToConcern(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        
        // Verify concern belongs to this organization
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        ConcernResponse concern = concernService.getConcernById(id);
        
        if (!concern.getOrganizationId().equals(org.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        String response = request.get("response");
        ConcernResponse concernResponse = concernService.respondToConcern(id, response, userId);
        
        return ResponseEntity.ok(concernResponse);
    }
    
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ConcernResponse> updateConcernStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        
        // Verify concern belongs to this organization
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        ConcernResponse concern = concernService.getConcernById(id);
        
        if (!concern.getOrganizationId().equals(org.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        String statusStr = request.get("status");
        ConcernStatus status = ConcernStatus.valueOf(statusStr);
        
        ConcernResponse updatedConcern = concernService.updateConcernStatus(id, status);
        
        return ResponseEntity.ok(updatedConcern);
    }
    
    
    @PostMapping("/{id}/close")
    public ResponseEntity<ConcernResponse> closeConcern(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Verify concern belongs to this organization
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        ConcernResponse concern = concernService.getConcernById(id);
        
        if (!concern.getOrganizationId().equals(org.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        ConcernResponse response = concernService.closeConcern(id);
        return ResponseEntity.ok(response);
    }
}
