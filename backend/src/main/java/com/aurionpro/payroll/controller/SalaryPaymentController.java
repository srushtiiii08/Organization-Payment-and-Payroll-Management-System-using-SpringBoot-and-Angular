package com.aurionpro.payroll.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.payroll.dto.response.EmployeeResponse;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.dto.response.SalaryPaymentHistory;
import com.aurionpro.payroll.dto.response.SalaryPaymentResponse;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.EmployeeService;
import com.aurionpro.payroll.service.OrganizationService;
import com.aurionpro.payroll.service.SalaryPaymentService;

@RestController
@RequestMapping("/api/salary-payments")
public class SalaryPaymentController {

	@Autowired
    private SalaryPaymentService salaryPaymentService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    
    // Organization processes salary payments
    @PostMapping("/process/{paymentRequestId}")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<List<SalaryPaymentResponse>> processSalaryPayments(
            @PathVariable Long paymentRequestId) {
        
        List<SalaryPaymentResponse> responses = salaryPaymentService.processSalaryPayments(paymentRequestId);
        return ResponseEntity.ok(responses);
    }
    
    
    // Employee views own salary history
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<SalaryPaymentHistory>> getMyPaymentHistory( @RequestParam(required = false) Integer year,Authentication authentication) {
    	System.out.println("🔍 Received year parameter: " + year);
    	String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        List<SalaryPaymentHistory> history;
        if (year != null) {
            System.out.println("✅ Filtering by year: " + year); 
            history = salaryPaymentService.getSalaryPaymentHistoryByEmployeeAndYear(employee.getId(), year);
        } else {
            System.out.println("⚠️ No year filter, returning all"); 
            history = salaryPaymentService.getSalaryPaymentHistoryByEmployee(employee.getId());
        }
        
        System.out.println("📊 Returning " + history.size() + " payments"); 
        return ResponseEntity.ok(history);
    }
    
    
    // Employee downloads salary slip
    @GetMapping("/download-slip/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<byte[]> downloadSalarySlip(@PathVariable Long id) {
        byte[] pdfBytes = salaryPaymentService.generateSalarySlipPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "salary_slip_" + id + ".pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
    
    
 // Organization views salary payments by month/year
    @GetMapping("/organization")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<List<SalaryPaymentResponse>> getOrganizationPayments(
            @RequestParam String month,
            @RequestParam Integer year,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<SalaryPaymentResponse> payments = 
            salaryPaymentService.getSalaryPaymentsByOrganization(org.getId(), month, year);
        return ResponseEntity.ok(payments);
    }
    
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZATION', 'EMPLOYEE', 'BANK_ADMIN')")
    public ResponseEntity<SalaryPaymentResponse> getSalaryPaymentById(@PathVariable Long id) {
    	SalaryPaymentResponse response = salaryPaymentService.getSalaryPaymentById(id);
        
    	return ResponseEntity.ok(response);
    }
    
    
}
