package com.aurionpro.payroll.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.PaymentRequest;
import com.aurionpro.payroll.enums.PaymentRequestStatus;
import com.aurionpro.payroll.enums.PaymentRequestType;

@Repository
public interface PaymentRequestRepo extends JpaRepository<PaymentRequest, Long> {
    
    // Find by organization
    List<PaymentRequest> findByOrganizationId(Long organizationId);
    
    // Find by status
    List<PaymentRequest> findByStatus(PaymentRequestStatus status);
    
    // Find by organization and status
    List<PaymentRequest> findByOrganizationIdAndStatus(Long organizationId, PaymentRequestStatus status);
    
    // Find by type
    List<PaymentRequest> findByRequestType(PaymentRequestType requestType);
    
    // Find Pending Requests (for Bank Admin)
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = 'PENDING' ORDER BY pr.createdAt DESC")
    List<PaymentRequest> findAllPendingRequests();
    
    // Find by month and year
    List<PaymentRequest> findByOrganizationIdAndMonthAndYear(Long organizationId, String month, Integer year);
    
    // Find Request by data range
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentRequest> findByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find by approved by (Bank Admin user id)
    List<PaymentRequest> findByApprovedById(Long adminUserId);
    
    // Count pending requests by organization
    long countByOrganizationIdAndStatus(Long organizationId, PaymentRequestStatus status);
    
    // Find latest req by organization
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.organization.id = :orgId " +
           "ORDER BY pr.createdAt DESC")
    List<PaymentRequest> findLatestByOrganizationId(@Param("orgId") Long orgId);
    
    List<PaymentRequest> findByOrganization(Organization organization);
    
    List<PaymentRequest> findByOrganizationOrderByCreatedAtDesc(Organization organization);
    
    List<PaymentRequest> findByOrganizationAndStatus(Organization organization, PaymentRequestStatus status);
    
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.organization = :organization AND pr.month = :month AND pr.year = :year")
    List<PaymentRequest> findByOrganizationAndMonthAndYear(
        @Param("organization") Organization organization,
        @Param("month") String month,
        @Param("year") Integer year
    );
    
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = :status ORDER BY pr.createdAt ASC")
    List<PaymentRequest> findPendingRequestsOrderByDate(@Param("status") PaymentRequestStatus status);
    
    boolean existsByOrganizationAndMonthAndYearAndStatus(
        Organization organization, 
        String month, 
        Integer year, 
        PaymentRequestStatus status
    );
}