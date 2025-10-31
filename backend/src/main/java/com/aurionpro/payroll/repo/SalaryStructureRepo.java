package com.aurionpro.payroll.repo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.SalaryStructure;

@Repository
public interface SalaryStructureRepo extends JpaRepository<SalaryStructure, Long>{
	   
    // Find by employee id
    Optional<SalaryStructure> findByEmployeeId(Long employeeId);
    
    // Find active salary structure by employee
    Optional<SalaryStructure> findByEmployeeIdAndIsActive(Long employeeId, Boolean isActive);
    
    // Find all salary structures by organization
    @Query("SELECT s FROM SalaryStructure s WHERE s.employee.organization.id = :orgId")
    List<SalaryStructure> findByOrganizationId(@Param("orgId") Long orgId);
    
    // Find active salary structures by organization
    @Query("SELECT s FROM SalaryStructure s WHERE s.employee.organization.id = :orgId AND s.isActive = true")
    List<SalaryStructure> findActiveByOrganizationId(@Param("orgId") Long orgId);
    
    // Find employees with salary greater than amount
    @Query("SELECT s FROM SalaryStructure s WHERE s.employee.organization.id = :orgId " +
           "AND s.netSalary > :amount AND s.isActive = true")
    List<SalaryStructure> findByOrganizationIdAndNetSalaryGreaterThan(
        @Param("orgId") Long orgId, 
        @Param("amount") BigDecimal amount
    );
    
    // Calculate total monthly payroll by organization
    @Query("SELECT SUM(s.netSalary) FROM SalaryStructure s " +
           "WHERE s.employee.organization.id = :orgId AND s.isActive = true")
    BigDecimal calculateTotalPayrollByOrganization(@Param("orgId") Long orgId);
    
    // Find all salary structures for an employee
    List<SalaryStructure> findByEmployee(Employee employee);
    
    // Find salary structures ordered by effective date
    List<SalaryStructure> findByEmployeeOrderByEffectiveFromDesc(Employee employee);
    
    // Find by active status
    List<SalaryStructure> findByIsActive(Boolean isActive);
    
    // ⭐ NEW: Find ALL active salary structures for an employee (returns List)
    List<SalaryStructure> findAllByEmployeeAndIsActive(Employee employee, Boolean isActive);
    
    // ⭐ NEW: Find ONE active salary structure (most recent) - returns Optional
    Optional<SalaryStructure> findFirstByEmployeeAndIsActiveOrderByEffectiveFromDesc(
        Employee employee, Boolean isActive);
    
    // ⭐ Helper default method
    default Optional<SalaryStructure> findActiveByEmployee(Employee employee) {
        return findFirstByEmployeeAndIsActiveOrderByEffectiveFromDesc(employee, true);
    }
}
