package com.aurionpro.payroll.service;

public interface ExcelGenerationService {
    
    byte[] generateEmployeeListExcel(Long organizationId);
    
    byte[] generateSalaryReportExcel(Long organizationId, String month, Integer year);
}