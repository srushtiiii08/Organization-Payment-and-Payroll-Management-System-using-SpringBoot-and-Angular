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
public class PaymentRequestResponse {

	private Long id;
    private Long organizationId;
    private String organizationName;
    private PaymentRequestType requestType;
    private BigDecimal totalAmount;
    private Integer employeeCount;
    private String month;
    private Integer year;
    private PaymentRequestStatus status;
    private String remarks;
    private String rejectionReason;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt; 
    private LocalDateTime createdAt; 
    private LocalDateTime updatedAt;
}
