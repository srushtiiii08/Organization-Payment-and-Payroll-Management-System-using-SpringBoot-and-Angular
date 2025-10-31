package com.aurionpro.payroll.dto.response;

import java.time.LocalDateTime;

import com.aurionpro.payroll.enums.VendorStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {

	private Long id;
    private Long organizationId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String serviceType;
    private String bankAccountNumber;
    private String ifscCode;
    private String gstNumber;
    private VendorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
