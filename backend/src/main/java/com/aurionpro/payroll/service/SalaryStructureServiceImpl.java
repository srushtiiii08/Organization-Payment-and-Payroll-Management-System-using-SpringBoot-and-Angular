package com.aurionpro.payroll.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.dto.request.SalaryStructureRequest;
import com.aurionpro.payroll.dto.response.SalaryStructureResponse;
import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.SalaryStructure;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.EmployeeRepo;
import com.aurionpro.payroll.repo.SalaryStructureRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SalaryStructureServiceImpl implements SalaryStructureService {

	
	@Autowired
    private SalaryStructureRepo salaryStructureRepo;
    
    @Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private ModelMapper modelMapper;
    
    
    
    
    @Override
    @Transactional
    public SalaryStructureResponse createSalaryStructure(SalaryStructureRequest request, Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("💰 CREATING SALARY STRUCTURE");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("Employee: " + employee.getName());
        System.out.println("Basic Salary: ₹" + request.getBasicSalary());
        System.out.println("Effective From: " + request.getEffectiveFrom());
        
        // ⭐ USE findAllByEmployeeAndIsActive (returns List)
        List<SalaryStructure> existingStructures = 
            salaryStructureRepo.findAllByEmployeeAndIsActive(employee, true);
        
        if (!existingStructures.isEmpty()) {
            System.out.println("⚠️  Found " + existingStructures.size() + " active salary structure(s)");
            System.out.println("   Deactivating old structures...");
            
            for (SalaryStructure oldStructure : existingStructures) {
                oldStructure.setIsActive(false);
                salaryStructureRepo.save(oldStructure);
                System.out.println("   ✅ Deactivated salary structure ID: " + oldStructure.getId());
            }
        } else {
            System.out.println("✅ No existing active salary structures found");
        }
        
        // ⭐ STEP 2: CREATE NEW SALARY STRUCTURE
        SalaryStructure salaryStructure = modelMapper.map(request, SalaryStructure.class);
        salaryStructure.setEmployee(employee);
        salaryStructure.setIsActive(true);
        
        // Calculate gross and net salary
        BigDecimal grossSalary = salaryStructure.getBasicSalary()
            .add(salaryStructure.getHra())
            .add(salaryStructure.getDearnessAllowance())
            .add(salaryStructure.getOtherAllowances());
        
        BigDecimal netSalary = grossSalary.subtract(salaryStructure.getProvidentFund());
        
        salaryStructure.setGrossSalary(grossSalary);
        salaryStructure.setNetSalary(netSalary);
        
        SalaryStructure savedStructure = salaryStructureRepo.save(salaryStructure);
        
        System.out.println("✅ Salary structure created successfully!");
        System.out.println("   Structure ID: " + savedStructure.getId());
        System.out.println("   Gross Salary: ₹" + grossSalary);
        System.out.println("   Net Salary: ₹" + netSalary);
        System.out.println("═══════════════════════════════════════════════════");
        
        return modelMapper.map(savedStructure, SalaryStructureResponse.class);
    }

    @Override
    public SalaryStructureResponse getActiveSalaryStructureByEmployee(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        // ⭐ USE findFirstByEmployeeAndIsActiveOrderByEffectiveFromDesc (returns Optional)
        SalaryStructure salaryStructure = salaryStructureRepo
            .findFirstByEmployeeAndIsActiveOrderByEffectiveFromDesc(employee, true)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Active SalaryStructure", "employeeId", employeeId));
        
        return modelMapper.map(salaryStructure, SalaryStructureResponse.class);
    }
    
        
    @Override
    public SalaryStructureResponse getSalaryStructureById(Long id) {
            SalaryStructure salaryStructure = salaryStructureRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));
            
            return modelMapper.map(salaryStructure, SalaryStructureResponse.class);
    }
    
    
    
    @Override
    public List<SalaryStructureResponse> getSalaryStructureHistoryByEmployee(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        List<SalaryStructure> salaryStructures = salaryStructureRepo.findByEmployeeOrderByEffectiveFromDesc(employee);
        
        return salaryStructures.stream()
            .map(salary -> modelMapper.map(salary, SalaryStructureResponse.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public SalaryStructureResponse updateSalaryStructure(Long id, SalaryStructureRequest request) {
    	SalaryStructure salaryStructure = salaryStructureRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));
            
        if (!salaryStructure.getIsActive()) {
                throw new BadRequestException("Cannot update inactive salary structure");
        }
        
        salaryStructure.setBasicSalary(request.getBasicSalary());
        salaryStructure.setHra(request.getHra());
        salaryStructure.setDearnessAllowance(request.getDearnessAllowance());
        salaryStructure.setOtherAllowances(request.getOtherAllowances());
        salaryStructure.setProvidentFund(request.getProvidentFund());
        salaryStructure.setEffectiveFrom(request.getEffectiveFrom());
        
        BigDecimal grossSalary = request.getBasicSalary()
            .add(request.getHra())
            .add(request.getDearnessAllowance())
            .add(request.getOtherAllowances());
        salaryStructure.setGrossSalary(grossSalary);
        
        BigDecimal netSalary = grossSalary.subtract(request.getProvidentFund());
        salaryStructure.setNetSalary(netSalary);
        
        SalaryStructure updatedSalaryStructure = salaryStructureRepo.save(salaryStructure);

        return modelMapper.map(updatedSalaryStructure, SalaryStructureResponse.class);
    	
    }
    
    
    @Override
    public void deactivateSalaryStructure(Long id) {
        SalaryStructure salaryStructure = salaryStructureRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));
        
        salaryStructure.setIsActive(false);
        salaryStructureRepo.save(salaryStructure);
    }
        
        
}
