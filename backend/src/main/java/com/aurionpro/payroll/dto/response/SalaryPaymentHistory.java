package com.aurionpro.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aurionpro.payroll.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryPaymentHistory {

	private Long id;
    private String month;
    private Integer year;
    private BigDecimal amount;
    private BigDecimal netSalary;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String salarySlipUrl;
    
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal dearnessAllowance;
    private BigDecimal providentFund;
    private BigDecimal otherAllowances;
    private BigDecimal grossSalary;
}
