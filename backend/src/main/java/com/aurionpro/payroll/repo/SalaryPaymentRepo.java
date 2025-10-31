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
import com.aurionpro.payroll.entity.SalaryPayment;
import com.aurionpro.payroll.enums.PaymentStatus;

@Repository
public interface SalaryPaymentRepo extends JpaRepository<SalaryPayment, Long>{

	// Find by employee
    List<SalaryPayment> findByEmployeeId(Long employeeId);
    
    // Find by employee ordered by date (latest first)
    List<SalaryPayment> findByEmployeeIdOrderByPaymentDateDesc(Long employeeId);
    
    List<SalaryPayment> findByEmployeeAndYearOrderByPaymentDateDesc(Employee employee, Integer year);
    
    // Find by payment request
    List<SalaryPayment> findByPaymentRequestId(Long paymentRequestId);
    
    // Find by month and year
    List<SalaryPayment> findByMonthAndYear(String month, Integer year);
    
    // Find by employee, month and year (unique payment)
    Optional<SalaryPayment> findByEmployeeIdAndMonthAndYear(Long employeeId, String month, Integer year);
    
    // Check if payment exists for employee in month/year
    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, String month, Integer year);
    
    // Find by status
    List<SalaryPayment> findByStatus(PaymentStatus status);
    
    // Find by employee and status
    List<SalaryPayment> findByEmployeeIdAndStatus(Long employeeId, PaymentStatus status);
    
    // Find payments by organization (through employee)
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee.organization.id = :orgId")
    List<SalaryPayment> findByOrganizationId(@Param("orgId") Long orgId);
    
    // Find payments by date range
    List<SalaryPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find recent payments by employee (last N months)
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee.id = :employeeId " +
           "ORDER BY sp.year DESC, sp.month DESC")
    List<SalaryPayment> findRecentPaymentsByEmployee(@Param("employeeId") Long employeeId);

    List<SalaryPayment> findByEmployee(Employee employee);
    
    List<SalaryPayment> findByEmployeeOrderByPaymentDateDesc(Employee employee);
    
    
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee.organization = :organization")
    List<SalaryPayment> findByOrganization(@Param("organization") Organization organization);
    
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee.organization = :organization AND sp.status = :status")
    List<SalaryPayment> findByOrganizationAndStatus(
        @Param("organization") Organization organization, 
        @Param("status") PaymentStatus status);
    
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee = :employee AND sp.month = :month AND sp.year = :year")
    List<SalaryPayment> findByEmployeeAndMonthAndYear(
        @Param("employee") Employee employee, 
        @Param("month") String month, 
        @Param("year") Integer year);
    
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee.organization = :organization AND sp.month = :month AND sp.year = :year")
    List<SalaryPayment> findByOrganizationAndMonthAndYear(
        @Param("organization") Organization organization,
        @Param("month") String month,
        @Param("year") Integer year);
    
    boolean existsByEmployeeAndMonthAndYear(Employee employee, String month, Integer year);
}

