package com.aurionpro.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRejectRequest {

	@NotBlank(message = "Action is required (APPROVE or REJECT)")
    private String action; // "APPROVE" or "REJECT"
    
    private String remarks;
    
    private String rejectionReason; // Required if action is REJECT

}
