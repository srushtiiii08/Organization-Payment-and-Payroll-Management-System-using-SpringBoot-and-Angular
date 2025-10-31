package com.aurionpro.payroll.service;

import java.util.List;

import com.aurionpro.payroll.dto.response.SalaryPaymentHistory;
import com.aurionpro.payroll.dto.response.SalaryPaymentResponse;

public interface SalaryPaymentService {

	List<SalaryPaymentResponse> processSalaryPayments(Long paymentRequestId);
    
    SalaryPaymentResponse getSalaryPaymentById(Long id);
    
    List<SalaryPaymentHistory> getSalaryPaymentHistoryByEmployee(Long employeeId);
    
    List<SalaryPaymentHistory> getSalaryPaymentHistoryByEmployeeAndYear(Long employeeId, Integer year);
    
    List<SalaryPaymentResponse> getSalaryPaymentsByOrganization(Long organizationId, String month, Integer year);
    
    byte[] generateSalarySlipPdf(Long salaryPaymentId);
}
