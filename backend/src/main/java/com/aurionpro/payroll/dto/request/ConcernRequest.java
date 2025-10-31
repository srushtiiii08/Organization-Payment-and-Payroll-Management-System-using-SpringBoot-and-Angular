package com.aurionpro.payroll.dto.request;

import com.aurionpro.payroll.enums.ConcernPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcernRequest {

	@NotBlank(message = "Subject is required")
    @Size(min = 5, max = 255, message = "Subject must be between 5 and 255 characters")
    private String subject;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    @NotNull(message = "Priority is required")
    private ConcernPriority priority;
    
    private String attachmentUrl; // Cloudinary URLs (comma-separated)

}
