package com.aurionpro.payroll.service;

import java.util.List;

import com.aurionpro.payroll.dto.request.SalaryStructureRequest;
import com.aurionpro.payroll.dto.response.SalaryStructureResponse;

public interface SalaryStructureService {

	SalaryStructureResponse createSalaryStructure(SalaryStructureRequest request, Long employeeId);
    
    SalaryStructureResponse getSalaryStructureById(Long id);
    
    SalaryStructureResponse getActiveSalaryStructureByEmployee(Long employeeId);
    
    List<SalaryStructureResponse> getSalaryStructureHistoryByEmployee(Long employeeId);
    
    SalaryStructureResponse updateSalaryStructure(Long id, SalaryStructureRequest request);
    
    void deactivateSalaryStructure(Long id);
    
}
