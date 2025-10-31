package com.aurionpro.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.aurionpro.payroll.enums.PaymentRequestStatus;
import com.aurionpro.payroll.enums.PaymentRequestType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PaymentRequestList {

	private Long id;
    private String organizationName;
    private PaymentRequestType requestType;
    private BigDecimal totalAmount;
    private String month;
    private Integer year;
    private PaymentRequestStatus status;
    private LocalDateTime createdAt;
    
}
