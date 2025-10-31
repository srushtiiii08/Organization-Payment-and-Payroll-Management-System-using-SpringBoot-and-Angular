package com.aurionpro.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBankDetails {

	@NotBlank(message = "Bank account number is required")
    @Size(min = 9, max = 18, message = "Account number must be between 9 and 18 digits")
    private String bankAccountNumber;
    
    @NotBlank(message = "IFSC code is required")
    @Size(min = 11, max = 11, message = "IFSC code must be 11 characters")
    private String ifscCode;
    
    private String accountProofUrl; // Cloudinary URL
    
}
