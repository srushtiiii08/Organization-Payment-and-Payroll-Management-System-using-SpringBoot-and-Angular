package com.aurionpro.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aurionpro.payroll.enums.AccountVerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfile {

	private Long id;
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
    private AccountVerificationStatus accountVerificationStatus;
    private BigDecimal currentSalary;
    private String organizationName;
    private String profilePictureUrl;
    
}
