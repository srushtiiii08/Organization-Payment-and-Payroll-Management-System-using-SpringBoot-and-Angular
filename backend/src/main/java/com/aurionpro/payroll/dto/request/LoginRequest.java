package com.aurionpro.payroll.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	
	@NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
 //  CAPTCHA FIELDS (Required for login)
    @NotBlank(message = "CAPTCHA session is required")
    private String captchaSessionId;  // Session ID from captcha generation
    
    @NotBlank(message = "CAPTCHA answer is required")
    private String captchaAnswer; 
}
