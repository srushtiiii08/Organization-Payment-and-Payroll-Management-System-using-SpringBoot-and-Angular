package com.aurionpro.payroll.dto.request;

import java.math.BigDecimal;

import com.aurionpro.payroll.enums.PaymentRequestType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestRequest {

	@NotNull(message = "Request type is required")
    private PaymentRequestType requestType;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal totalAmount;
    
    private Integer employeeCount; // For salary disbursement
    
    @NotBlank(message = "Month is required")
    private String month; // e.g., "January"
    
    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be 2020 or later")
    private Integer year;
    
    private String remarks;
    
}
