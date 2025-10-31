package com.aurionpro.payroll.dto.request;

import java.time.LocalDate;

import com.aurionpro.payroll.validator.FutureDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

	@NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;
    
    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotBlank(message = "Designation is required")
    private String designation;
    
    @NotNull(message = "Date of joining is required")
//    @PastOrPresent(message = "Date of joining cannot be in the future")
    @FutureDate(maxDays = 30, message = "Date of joining cannot be more than 30 days in the future")
    private LocalDate dateOfJoining;
    
    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Bank account number must be between 9 and 18 digits")
    private String bankAccountNumber;
    
    @NotBlank(message = "Bank name is required")
    private String bankName;
    
    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String ifscCode;
    
}
