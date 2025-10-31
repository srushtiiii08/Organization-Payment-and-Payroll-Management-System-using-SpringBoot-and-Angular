package com.aurionpro.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStructureResponse {

	private Long id;
    private Long employeeId;
    private String employeeName;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal dearnessAllowance;
    private BigDecimal providentFund;
    private BigDecimal otherAllowances;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private LocalDate effectiveFrom;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
}
