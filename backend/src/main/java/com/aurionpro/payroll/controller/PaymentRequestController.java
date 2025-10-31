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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.payroll.dto.request.PaymentRequestRequest;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.dto.response.PaymentRequestResponse;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.OrganizationService;
import com.aurionpro.payroll.service.PaymentRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/org/payment-requests")
@PreAuthorize("hasRole('ORGANIZATION')")
public class PaymentRequestController {

	@Autowired
    private PaymentRequestService paymentRequestService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @PostMapping
    public ResponseEntity<PaymentRequestResponse> createPaymentRequest(
            @Valid @RequestBody PaymentRequestRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        PaymentRequestResponse response = paymentRequestService.createPaymentRequest(request, org.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PaymentRequestResponse>> getAllMyPaymentRequests(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<PaymentRequestResponse> requests = 
            paymentRequestService.getAllPaymentRequestsByOrganization(org.getId());
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PaymentRequestResponse> getPaymentRequestById(@PathVariable Long id, Authentication authentication) {

        // Verify payment request belongs to authenticated organization
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        PaymentRequestResponse response = paymentRequestService.getPaymentRequestById(id);
        
        // Security check
        if (!response.getOrganizationId().equals(org.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentRequest(@PathVariable Long id, Authentication authentication) {
        paymentRequestService.deletePaymentRequest(id);
        

        // Verify payment request belongs to authenticated organization
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        PaymentRequestResponse paymentRequest = paymentRequestService.getPaymentRequestById(id);
        
        paymentRequestService.deletePaymentRequest(id);
        return ResponseEntity.noContent().build();
    }
     
}
