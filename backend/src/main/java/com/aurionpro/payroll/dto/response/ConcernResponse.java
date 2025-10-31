package com.aurionpro.payroll.dto.response;

import java.time.LocalDateTime;

import com.aurionpro.payroll.enums.ConcernStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcernResponse {

	private Long id;
    private Long employeeId;
    private String employeeName;
    private Long organizationId;
    private String subject;
    private String description;
    private String attachmentUrl;
    private ConcernStatus status;
    private String response;
    private Long respondedBy;
    private String respondedByName;
    private LocalDateTime raisedAt;
    private LocalDateTime respondedAt;
    
}
