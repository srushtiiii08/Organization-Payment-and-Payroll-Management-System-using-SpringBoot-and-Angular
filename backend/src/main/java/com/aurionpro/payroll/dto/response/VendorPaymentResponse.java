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
public class VendorPaymentResponse {

	private Long id;
    private Long vendorId;
    private String vendorName;
    private BigDecimal amount;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String transactionId;
    private String description;
    private String invoiceDocumentUrl;
    private LocalDateTime createdAt;
    
}
