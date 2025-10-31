package com.aurionpro.payroll.dto.response;

import java.time.LocalDateTime;

import com.aurionpro.payroll.enums.ConcernPriority;
import com.aurionpro.payroll.enums.ConcernStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcernList {

	private Long id;
	private String subject;
    private String title; // Same as subject, for compatibility
    private String description;
    private String category;
    private String employeeName;
    private String reportedBy; // Employee name
    private ConcernStatus status;
    private ConcernPriority priority;
    private LocalDateTime createdAt;
}
