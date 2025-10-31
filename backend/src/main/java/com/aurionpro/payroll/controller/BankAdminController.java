package com.aurionpro.payroll.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.payroll.dto.response.OrganizationList;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.dto.response.PaymentRequestList;
import com.aurionpro.payroll.dto.response.PaymentRequestResponse;
import com.aurionpro.payroll.enums.PaymentRequestStatus;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.OrganizationService;
import com.aurionpro.payroll.service.PaymentRequestService;

@RestController  
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('BANK_ADMIN')")
public class BankAdminController {

	@Autowired
    private OrganizationService organizationService;
    
	@Autowired
    private PaymentRequestService paymentRequestService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
	
	// ORGANIZATION MANAGEMENT
    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationList>> getAllOrganizations() {
        List<OrganizationList> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }
    
    @GetMapping("/organizations/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/organizations/status")
    public ResponseEntity<List<OrganizationList>> getOrganizationsByStatus(
            @RequestParam Boolean verified) {
        List<OrganizationList> organizations = 
            organizationService.getOrganizationsByVerificationStatus(verified);
        return ResponseEntity.ok(organizations);
    }
    
    @GetMapping("/organizations/pending")
    public ResponseEntity<List<OrganizationList>> getPendingVerifications() {
        List<OrganizationList> organizations = organizationService.getAllOrganizations()
            .stream()
            .filter(org -> !org.getVerified() && 
                          org.getUserStatus() == com.aurionpro.payroll.enums.UserStatus.PENDING)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(organizations);
    }
    
    @GetMapping("/organizations/rejected")
    public ResponseEntity<List<OrganizationList>> getRejectedOrganizations() {
        List<OrganizationList> organizations = organizationService.getAllOrganizations()
            .stream()
            .filter(org -> !org.getVerified() && 
                          org.getUserStatus() == com.aurionpro.payroll.enums.UserStatus.INACTIVE)
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(organizations);
    }
    
    @PostMapping("/organizations/{id}/verify")
    public ResponseEntity<OrganizationResponse> verifyOrganization(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        
        String remarks = body != null ? body.get("remarks") : "Approved by admin";
        OrganizationResponse response = organizationService.verifyOrganization(id, remarks);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/organizations/{id}/reject")
    public ResponseEntity<OrganizationResponse> rejectOrganization(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        
        String remarks = body != null ? body.get("remarks") : "Rejected by admin";
        OrganizationResponse response = organizationService.rejectOrganization(id, remarks);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/organizations/{id}")
    public ResponseEntity<Map<String, String>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(Map.of("message", "Organization deleted successfully"));
    }
	
	
    // PAYMENT REQUEST MANAGEMENT
    @GetMapping("/payment-requests")
    public ResponseEntity<List<PaymentRequestList>> getAllPaymentRequests() {
        List<PaymentRequestList> requests = paymentRequestService.getAllPaymentRequests();
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/payment-requests/status")
    public ResponseEntity<List<PaymentRequestList>> getPaymentRequestsByStatus(
            @RequestParam PaymentRequestStatus status) {
        List<PaymentRequestList> requests = paymentRequestService.getPaymentRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/payment-requests/pending")
    public ResponseEntity<List<PaymentRequestList>> getPendingPaymentRequests() {
        List<PaymentRequestList> requests = 
            paymentRequestService.getPaymentRequestsByStatus(PaymentRequestStatus.PENDING);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/payment-requests/{id}")
    public ResponseEntity<PaymentRequestResponse> getPaymentRequestById(@PathVariable Long id) {
        PaymentRequestResponse response = paymentRequestService.getPaymentRequestById(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/payment-requests/{id}/approve")
    public ResponseEntity<PaymentRequestResponse> approvePaymentRequest(
            @PathVariable Long id,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long bankAdminId = userDetailsService.loadUserEntityByEmail(email).getId();
        
        PaymentRequestResponse response = paymentRequestService.approvePaymentRequest(id, bankAdminId);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/payment-requests/{id}/reject")
    public ResponseEntity<PaymentRequestResponse> rejectPaymentRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long bankAdminId = userDetailsService.loadUserEntityByEmail(email).getId();
        String remarks = body != null ? body.get("remarks") : "Rejected by bank admin";
        
        PaymentRequestResponse response = 
            paymentRequestService.rejectPaymentRequest(id, bankAdminId, remarks);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/payment-requests/processed/count")
    public ResponseEntity<Map<String, Long>> getProcessedPaymentsCount() {
        long count = paymentRequestService.getPaymentRequestsByStatus(PaymentRequestStatus.COMPLETED).size();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
