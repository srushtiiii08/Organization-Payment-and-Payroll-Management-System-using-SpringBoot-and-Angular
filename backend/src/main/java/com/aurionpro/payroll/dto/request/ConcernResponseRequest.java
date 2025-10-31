package com.aurionpro.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcernResponseRequest {

	@NotBlank(message = "Response is required")
    @Size(min = 10, max = 2000, message = "Response must be between 10 and 2000 characters")
    private String response;
	
}
