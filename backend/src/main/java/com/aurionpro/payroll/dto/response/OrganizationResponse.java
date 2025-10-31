package com.aurionpro.payroll.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {

	private Long id;
    private Long userId;
    private String name;
    private String registrationNumber;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private Boolean verified;
    private String verificationDocumentsUrl;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer employeeCount;
    private Integer vendorCount;
    
}
