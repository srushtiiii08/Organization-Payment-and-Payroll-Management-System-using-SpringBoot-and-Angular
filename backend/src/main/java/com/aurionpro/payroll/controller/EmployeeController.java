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

import com.aurionpro.payroll.dto.request.EmployeeRequest;
import com.aurionpro.payroll.dto.response.EmployeeList;
import com.aurionpro.payroll.dto.response.EmployeeResponse;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.enums.EmployeeStatus;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.EmployeeService;
import com.aurionpro.payroll.service.OrganizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/org/employees")
@PreAuthorize("hasRole('ORGANIZATION')")
public class EmployeeController {

	@Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        EmployeeResponse response = employeeService.createEmployee(request, org.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeList>> getAllEmployees(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<EmployeeList> employees = employeeService.getAllEmployeesByOrganization(org.getId());
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/status")
    public ResponseEntity<List<EmployeeList>> getEmployeesByStatus(
            @RequestParam EmployeeStatus status,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        List<EmployeeList> employees = employeeService.getEmployeesByStatus(org.getId(), status);
        return ResponseEntity.ok(employees);
    }
    
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/verify-account")
    public ResponseEntity<EmployeeResponse> verifyEmployeeAccount(@PathVariable Long id) {
        EmployeeResponse response = employeeService.verifyEmployeeAccount(id);
        return ResponseEntity.ok(response);
    }
    
    
    @PutMapping("/{id}/status")
    public ResponseEntity<EmployeeResponse> updateEmployeeStatus(
            @PathVariable Long id,
            @RequestParam EmployeeStatus status) {
        
        EmployeeResponse response = employeeService.updateEmployeeStatus(id, status);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
