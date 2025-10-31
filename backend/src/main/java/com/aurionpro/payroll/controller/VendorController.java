package com.aurionpro.payroll.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.payroll.dto.request.VendorRequest;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.dto.response.VendorList;
import com.aurionpro.payroll.dto.response.VendorResponse;
import com.aurionpro.payroll.enums.VendorStatus;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.OrganizationService;
import com.aurionpro.payroll.service.VendorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/org/vendors")
@PreAuthorize("hasRole('ORGANIZATION')")
public class VendorController {

	@Autowired
    private VendorService vendorService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @PostMapping
    public ResponseEntity<VendorResponse> createVendor(
            @Valid @RequestBody VendorRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        VendorResponse response = vendorService.createVendor(request, org.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<VendorList>> getAllVendors(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<VendorList> vendors = vendorService.getAllVendorsByOrganization(org.getId());
        return ResponseEntity.ok(vendors);
    }
    
    @GetMapping("/status")
    public ResponseEntity<List<VendorList>> getVendorsByStatus(
            @RequestParam VendorStatus status,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<VendorList> vendors = vendorService.getVendorsByStatus(org.getId(), status);
        return ResponseEntity.ok(vendors);
    }
    
    
    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Long id) {
        VendorResponse response = vendorService.getVendorById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<VendorResponse> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody VendorRequest request) {
        
        VendorResponse response = vendorService.updateVendor(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<VendorResponse> updateVendorStatus(
            @PathVariable Long id,
            @RequestParam VendorStatus status) {
        
        VendorResponse response = vendorService.updateVendorStatus(id, status);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }
    
}
