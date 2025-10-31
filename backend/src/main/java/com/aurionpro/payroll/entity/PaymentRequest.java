package com.aurionpro.payroll.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aurionpro.payroll.enums.PaymentRequestStatus;
import com.aurionpro.payroll.enums.PaymentRequestType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentRequestType requestType; // SALARY_DISBURSEMENT, VENDOR_PAYMENT
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    
    private Integer employeeCount; // For salary disbursement
    
    private String month; // e.g., "January"
    
    private Integer year;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentRequestStatus status = PaymentRequestStatus.PENDING;
    
    @Column(length = 500)
    private String remarks;
    
    @Column(length = 500)
    private String rejectionReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Bank Admin who approved/rejected
    
    private LocalDateTime approvedAt; // ADDED - Service uses this field
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // CHANGED from requestDate for consistency
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    
    // Relationships
    @OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalaryPayment> salaryPayments;
    
    @OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorPayment> vendorPayments;
}