package com.aurionpro.payroll.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorPaymentRequest {

	@NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;
    
    @NotNull(message = "Invoice date is required")
    @PastOrPresent(message = "Invoice date cannot be in the future")
    private LocalDate invoiceDate;
    
    private String description;
    
    private String invoiceDocumentUrl; // Cloudinary URL
    
}
