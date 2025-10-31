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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.ConcernRequest;
import com.aurionpro.payroll.dto.response.ConcernList;
import com.aurionpro.payroll.dto.response.ConcernResponse;
import com.aurionpro.payroll.dto.response.EmployeeResponse;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.ConcernService;
import com.aurionpro.payroll.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employee/concerns")
@PreAuthorize("hasRole('EMPLOYEE')")
public class ConcernController {

	@Autowired
    private ConcernService concernService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @PostMapping
    public ResponseEntity<ConcernResponse> createConcern(
            @Valid @RequestBody ConcernRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        ConcernResponse response = concernService.createConcern(request, employee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/{id}/upload-attachment")
    public ResponseEntity<ConcernResponse> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        // Verify employee owns this concern
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        ConcernResponse concern = concernService.getConcernById(id);
        
        // Check if this concern belongs to the authenticated employee
        if (!concern.getEmployeeId().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ConcernResponse response = concernService.uploadAttachment(id, file);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ConcernList>> getMyConcerns(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        List<ConcernList> concerns = concernService.getConcernsByEmployee(employee.getId());
        return ResponseEntity.ok(concerns);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ConcernResponse> getConcernById(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Verify employee owns this concern
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        ConcernResponse response = concernService.getConcernById(id);
        
        // Check if this concern belongs to the authenticated employee
        if (!response.getEmployeeId().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcern(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Verify employee owns this concern
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        
        ConcernResponse concern = concernService.getConcernById(id);
        
        // Check if this concern belongs to the authenticated employee
        if (!concern.getEmployeeId().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        concernService.deleteConcern(id);
        return ResponseEntity.noContent().build();
    }
    
    
}
