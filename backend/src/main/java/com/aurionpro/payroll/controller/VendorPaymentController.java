package com.aurionpro.payroll.controller;

import java.util.List;

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

import com.aurionpro.payroll.dto.request.VendorPaymentRequest;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.dto.response.VendorPaymentResponse;
import com.aurionpro.payroll.enums.PaymentStatus;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.OrganizationService;
import com.aurionpro.payroll.service.VendorPaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendor-payments")
@PreAuthorize("hasRole('ORGANIZATION')")
public class VendorPaymentController {

	@Autowired
    private VendorPaymentService vendorPaymentService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @PostMapping("/vendor/{vendorId}/payment-request/{paymentRequestId}")
    public ResponseEntity<VendorPaymentResponse> createVendorPayment(
            @PathVariable Long vendorId,
            @PathVariable Long paymentRequestId,
            @Valid @RequestBody VendorPaymentRequest request) {
        
        VendorPaymentResponse response = 
            vendorPaymentService.createVendorPayment(request, vendorId, paymentRequestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VendorPaymentResponse> getVendorPaymentById(@PathVariable Long id) {
        VendorPaymentResponse response = vendorPaymentService.getVendorPaymentById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<VendorPaymentResponse>> getVendorPayments(@PathVariable Long vendorId) {
        List<VendorPaymentResponse> payments = vendorPaymentService.getVendorPaymentsByVendor(vendorId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/organization")
    public ResponseEntity<List<VendorPaymentResponse>> getOrganizationVendorPayments(
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<VendorPaymentResponse> payments = 
            vendorPaymentService.getVendorPaymentsByOrganization(org.getId());
        return ResponseEntity.ok(payments);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<VendorPaymentResponse> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {
        
        VendorPaymentResponse response = vendorPaymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(response);
    }
    
    
    
}
