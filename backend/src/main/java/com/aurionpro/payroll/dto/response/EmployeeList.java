package com.aurionpro.payroll.dto.response;

import java.time.LocalDate;

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
public class EmployeeList {

	private Long id;
    private String name;
    private String email;
    private String department;
    private String designation;
    private LocalDate dateOfJoining;
    private EmployeeStatus status;
    private AccountVerificationStatus accountVerificationStatus;  
    private Double currentSalary;
    
}
