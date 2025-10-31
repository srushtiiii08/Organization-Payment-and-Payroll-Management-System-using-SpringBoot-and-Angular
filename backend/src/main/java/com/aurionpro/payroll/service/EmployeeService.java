package com.aurionpro.payroll.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.EmployeeRequest;
import com.aurionpro.payroll.dto.response.EmployeeList;
import com.aurionpro.payroll.dto.response.EmployeeProfile;
import com.aurionpro.payroll.dto.response.EmployeeResponse;
import com.aurionpro.payroll.entity.SalaryStructure;
import com.aurionpro.payroll.enums.EmployeeStatus;

public interface EmployeeService {

	EmployeeResponse createEmployee(EmployeeRequest request, Long organizationId);
    
    EmployeeResponse getEmployeeById(Long id);
    
    EmployeeResponse getEmployeeByUserId(Long userId);
    
    EmployeeProfile getEmployeeProfile(Long userId);
    
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    
    List<EmployeeList> getAllEmployeesByOrganization(Long organizationId);
    
    List<EmployeeList> getEmployeesByStatus(Long organizationId, EmployeeStatus status);
    
    EmployeeResponse uploadAccountProof(Long id, MultipartFile file);
    
    EmployeeResponse verifyEmployeeAccount(Long id);
    
    EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status);
    
    EmployeeResponse addOrUpdateSalaryStructure(Long employeeId, SalaryStructure newStructure);
    
    void deleteEmployee(Long id);
    
    void updateProfilePicture(Long employeeId, String profilePictureUrl);

}
