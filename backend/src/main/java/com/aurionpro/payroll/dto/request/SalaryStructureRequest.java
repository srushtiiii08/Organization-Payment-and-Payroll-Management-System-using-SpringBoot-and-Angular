package com.aurionpro.payroll.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructureRequest {

	@NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Basic salary must be greater than 0")
    private BigDecimal basicSalary;
    
    @NotNull(message = "HRA is required")
    @DecimalMin(value = "0.0", message = "HRA must be greater than or equal to 0")
    private BigDecimal hra;
    
    @NotNull(message = "Dearness allowance is required")
    @DecimalMin(value = "0.0", message = "Dearness allowance must be greater than or equal to 0")
    private BigDecimal dearnessAllowance;
    
    @NotNull(message = "Provident fund is required")
    @DecimalMin(value = "0.0", message = "Provident fund must be greater than or equal to 0")
    private BigDecimal providentFund;
    
    @DecimalMin(value = "0.0", message = "Other allowances must be greater than or equal to 0")
    private BigDecimal otherAllowances;
    
    @NotNull(message = "Effective from date is required")
    @FutureOrPresent
    private LocalDate effectiveFrom;
    
}
