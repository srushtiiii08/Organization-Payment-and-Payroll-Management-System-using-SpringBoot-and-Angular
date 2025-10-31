package com.aurionpro.payroll.service;

import com.aurionpro.payroll.entity.SalaryPayment;

public interface PdfGenerationService {
    
    byte[] generateSalarySlip(SalaryPayment salaryPayment);
    

    //For payment reports
    byte[] generateSalaryReportPdf(Long organizationId, String month, Integer year);

}