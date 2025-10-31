package com.aurionpro.payroll.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.payroll.dto.request.SalaryStructureRequest;
import com.aurionpro.payroll.dto.response.SalaryStructureResponse;
import com.aurionpro.payroll.service.SalaryStructureService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/org/salary-structure")
@PreAuthorize("hasRole('ORGANIZATION')")
public class SalaryStructureController {

	@Autowired
    private SalaryStructureService salaryStructureService;
    
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<SalaryStructureResponse> createSalaryStructure(
            @PathVariable Long employeeId,
            @Valid @RequestBody SalaryStructureRequest request) {
        
        SalaryStructureResponse response = salaryStructureService.createSalaryStructure(request, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SalaryStructureResponse> getSalaryStructureById(@PathVariable Long id) {
        SalaryStructureResponse response = salaryStructureService.getSalaryStructureById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/employee/{employeeId}/active")
    public ResponseEntity<SalaryStructureResponse> getActiveSalaryStructure(@PathVariable Long employeeId) {
        SalaryStructureResponse response = salaryStructureService.getActiveSalaryStructureByEmployee(employeeId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<List<SalaryStructureResponse>> getSalaryStructureHistory(@PathVariable Long employeeId) {
        List<SalaryStructureResponse> history = salaryStructureService.getSalaryStructureHistoryByEmployee(employeeId);
        return ResponseEntity.ok(history);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SalaryStructureResponse> updateSalaryStructure(
            @PathVariable Long id,
            @Valid @RequestBody SalaryStructureRequest request) {
        
        SalaryStructureResponse response = salaryStructureService.updateSalaryStructure(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateSalaryStructure(@PathVariable Long id) {
        salaryStructureService.deactivateSalaryStructure(id);
        return ResponseEntity.noContent().build();
    }
}
