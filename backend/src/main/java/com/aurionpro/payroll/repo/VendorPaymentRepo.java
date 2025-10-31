package com.aurionpro.payroll.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.PaymentRequest;
import com.aurionpro.payroll.entity.Vendor;
import com.aurionpro.payroll.entity.VendorPayment;
import com.aurionpro.payroll.enums.PaymentStatus;

@Repository
public interface VendorPaymentRepo extends JpaRepository<VendorPayment, Long> {

	// Find by vendor
    List<VendorPayment> findByVendorId(Long vendorId);
    
    // Find by vendor ordered by date
    List<VendorPayment> findByVendorIdOrderByPaymentDateDesc(Long vendorId);
    
    // Find by payment request
    List<VendorPayment> findByPaymentRequestId(Long paymentRequestId);
    
    // Find by invoice number
    Optional<VendorPayment> findByInvoiceNumber(String invoiceNumber);
    
    // Check if invoice number exists
    boolean existsByInvoiceNumber(String invoiceNumber);
    
    // Find by status
    List<VendorPayment> findByStatus(PaymentStatus status);
    
    // Find payments by organization (through vendor)
    @Query("SELECT vp FROM VendorPayment vp WHERE vp.vendor.organization.id = :orgId")
    List<VendorPayment> findByOrganizationId(@Param("orgId") Long orgId);
    
    // Find payments by date range
    List<VendorPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find by invoice date range
    List<VendorPayment> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);

    List<VendorPayment> findByVendor(Vendor vendor);
    
    List<VendorPayment> findByVendorOrderByPaymentDateDesc(Vendor vendor);
    
    List<VendorPayment> findByPaymentRequest(PaymentRequest paymentRequest);
    
    @Query("SELECT vp FROM VendorPayment vp WHERE vp.vendor.organization.id = :organizationId AND vp.status = :status")
    List<VendorPayment> findByOrganizationIdAndStatus(
        @Param("organizationId") Long organizationId, 
        @Param("status") PaymentStatus status);
    
}
