package com.aurionpro.payroll.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aurionpro.payroll.enums.AccountVerificationStatus;
import com.aurionpro.payroll.enums.EmployeeStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

	private Long id;
    private Long organizationId;
    private String organizationName;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String department;
    private String designation;
    private LocalDate dateOfJoining;
    private String bankAccountNumber;
    private String bankName;
    private String ifscCode;
    private String accountProofUrl;
    private AccountVerificationStatus accountVerificationStatus;
    private EmployeeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
