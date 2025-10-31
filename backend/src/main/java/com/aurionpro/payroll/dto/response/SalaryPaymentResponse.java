package com.aurionpro.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aurionpro.payroll.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryPaymentResponse {

	private Long id;
    private Long employeeId;
    private String employeeName;
    private BigDecimal amount;
    private String month;
    private Integer year;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String transactionId;
    private String salarySlipUrl;
    
    // Salary breakdown
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal dearnessAllowance;
    private BigDecimal providentFund;
    private BigDecimal otherAllowances;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    
    
    private LocalDateTime createdAt;
    
}
