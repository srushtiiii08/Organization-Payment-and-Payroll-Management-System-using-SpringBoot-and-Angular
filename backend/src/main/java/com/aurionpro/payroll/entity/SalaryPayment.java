package com.aurionpro.payroll.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aurionpro.payroll.enums.PaymentStatus;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salary_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_request_id")
    private PaymentRequest paymentRequest;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String month;
    
    @Column(nullable = false)
    private Integer year;
    
    private LocalDate paymentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private String transactionId;
    
    private String salarySlipUrl; // PDF URL from Cloudinary
    
    // Salary breakdown (for record keeping)
    @Column(precision = 10, scale = 2)
    private BigDecimal basicSalary;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal hra;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal dearnessAllowance;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal providentFund;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal otherAllowances;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal grossSalary;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal netSalary;
    
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}