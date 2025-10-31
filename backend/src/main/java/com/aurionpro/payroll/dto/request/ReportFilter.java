package com.aurionpro.payroll.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilter {

private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String month;
    
    private Integer year;
    
    @NotBlank(message = "Format is required (PDF or EXCEL)")
    private String format; // "PDF" or "EXCEL"
    
    private String department;
    
    private Long employeeId;
    
    private String status;
    
}
