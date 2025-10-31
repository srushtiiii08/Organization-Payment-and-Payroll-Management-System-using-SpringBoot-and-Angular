package com.aurionpro.payroll.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.enums.AccountVerificationStatus;
import com.aurionpro.payroll.enums.EmployeeStatus;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long>{

	// Find by user id
    Optional<Employee> findByUserId(Long userId);
    
    // Find by email
    Optional<Employee> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find by organization and status
    List<Employee> findByOrganizationIdAndStatus(Long organizationId, EmployeeStatus status);
    
    // Find active employees by organization
    @Query("SELECT e FROM Employee e WHERE e.organization.id = :orgId AND e.status = 'ACTIVE'")
    List<Employee> findActiveEmployeesByOrganization(@Param("orgId") Long orgId);
    
    // Find by department
    List<Employee> findByOrganizationIdAndDepartment(Long organizationId, String department);
    
    // Find employees with pending bank account verification
    List<Employee> findByOrganizationIdAndAccountVerificationStatus(
        Long organizationId, 
        AccountVerificationStatus status
    );
    

    // Fetch employee with all salary structures
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.salaryStructures s WHERE e.id = :id")
    Optional<Employee> findByIdWithSalaryStructures(@Param("id") Long id);

    // (Optional) Fetch employee with only active salary structure
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.salaryStructures s WHERE e.id = :id AND s.isActive = true")
    Optional<Employee> findByIdWithActiveSalaryStructure(@Param("id") Long id);

    
    
    // Find employees joining in date range
    List<Employee> findByOrganizationIdAndDateOfJoiningBetween(
            Long organizationId, 
            LocalDate startDate, 
            LocalDate endDate
        );
    
    // Count employees by organization
    long countByOrganizationId(Long organizationId);
    
    // Count active employees by organization
    long countByOrganizationIdAndStatus(Long organizationId, EmployeeStatus status);
    
    Optional<Employee> findByUser(User user);
    
    
    
    boolean existsByBankAccountNumber(String bankAccountNumber);
    
    List<Employee> findByOrganization(Organization organization);
    
    List<Employee> findByOrganizationAndStatus(Organization organization, EmployeeStatus status);
    
    List<Employee> findByOrganizationAndAccountVerificationStatus(
        Organization organization, AccountVerificationStatus status);
    
    List<Employee> findByStatus(EmployeeStatus status);
    
    long countByOrganization(Organization organization);
    
    long countByOrganizationAndStatus(Organization organization, EmployeeStatus status);

}
