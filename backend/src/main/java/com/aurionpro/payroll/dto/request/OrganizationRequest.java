package com.aurionpro.payroll.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationRequest {

	@NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;
    
    @NotBlank(message = "Registration number is required")
    private String registrationNumber;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "Contact phone is required")
    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 digits")
    private String contactPhone;
    
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    private String verificationDocumentsUrl; // Cloudinary URLs

}
