package com.aurionpro.payroll.dto.response;

import java.time.LocalDateTime;

import com.aurionpro.payroll.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationList {

	private Long id;
    private String name;
    private String registrationNumber;
    private Boolean verified;
    private String contactPhone;
    private LocalDateTime createdAt;
    private Integer employeeCount;
    private UserStatus userStatus;
    
}
