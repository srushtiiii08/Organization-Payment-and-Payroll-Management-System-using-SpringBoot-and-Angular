package com.aurionpro.payroll.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.Concern;
import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.enums.ConcernStatus;

@Repository
public interface ConcernRepo extends JpaRepository<Concern, Long>{

	// Find by employee
    List<Concern> findByEmployeeId(Long employeeId);
    
    // Find by employee ordered by date (latest first)
    List<Concern> findByEmployeeIdOrderByRaisedAtDesc(Long employeeId);
    
    // Find by organization
    List<Concern> findByOrganizationId(Long organizationId);
    
    // Find by organization ordered by date
    List<Concern> findByOrganizationIdOrderByRaisedAtDesc(Long organizationId);
    
    // Find by status
    List<Concern> findByStatus(ConcernStatus status);
    
    // Find by organization and status
    List<Concern> findByOrganizationIdAndStatus(Long organizationId, ConcernStatus status);
    
    // Find open concerns by organization
    @Query("SELECT c FROM Concern c WHERE c.organization.id = :orgId " +
           "AND c.status IN ('OPEN', 'IN_PROGRESS') ORDER BY c.raisedAt DESC")
    List<Concern> findOpenConcernsByOrganization(@Param("orgId") Long orgId);
    
 // Find concerns by date range
    List<Concern> findByRaisedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by responded by (user who responded)
    List<Concern> findByRespondedById(Long userId);
    
    // Count open concerns by organization
    long countByOrganizationIdAndStatus(Long organizationId, ConcernStatus status);
    
    // Count unresolved concerns by employee
    @Query("SELECT COUNT(c) FROM Concern c WHERE c.employee.id = :employeeId " +
           "AND c.status IN ('OPEN', 'IN_PROGRESS')")
    long countUnresolvedByEmployee(@Param("employeeId") Long employeeId);

    List<Concern> findByEmployee(Employee employee);
    
    List<Concern> findByEmployeeOrderByRaisedAtDesc(Employee employee);
    
    List<Concern> findByOrganization(Organization organization);
    
    List<Concern> findByOrganizationOrderByRaisedAtDesc(Organization organization);
    
    List<Concern> findByEmployeeAndStatus(Employee employee, ConcernStatus status);
    
    List<Concern> findByOrganizationAndStatus(Organization organization, ConcernStatus status);
    
    @Query("SELECT c FROM Concern c WHERE c.organization = :organization AND c.status = 'OPEN' ORDER BY c.raisedAt ASC")
    List<Concern> findOpenConcernsByOrganization(@Param("organization") Organization organization);
    
    long countByOrganizationAndStatus(Organization organization, ConcernStatus status);
    
    long countByEmployeeAndStatus(Employee employee, ConcernStatus status);

    
    
}
