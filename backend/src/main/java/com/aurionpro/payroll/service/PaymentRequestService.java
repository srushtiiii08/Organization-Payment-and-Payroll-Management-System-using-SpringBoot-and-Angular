package com.aurionpro.payroll.service;

import java.util.List;

import com.aurionpro.payroll.dto.request.PaymentRequestRequest;
import com.aurionpro.payroll.dto.response.PaymentRequestList;
import com.aurionpro.payroll.dto.response.PaymentRequestResponse;
import com.aurionpro.payroll.enums.PaymentRequestStatus;

public interface PaymentRequestService {

	PaymentRequestResponse createPaymentRequest(PaymentRequestRequest request, Long organizationId);
    
    PaymentRequestResponse getPaymentRequestById(Long id);
    
    List<PaymentRequestResponse> getAllPaymentRequestsByOrganization(Long organizationId);
    
    List<PaymentRequestList> getAllPaymentRequests();
    
    List<PaymentRequestList> getPaymentRequestsByStatus(PaymentRequestStatus status);
    
    PaymentRequestResponse approvePaymentRequest(Long id, Long bankAdminId);
    
    PaymentRequestResponse rejectPaymentRequest(Long id, Long bankAdminId, String remarks);
    
    void deletePaymentRequest(Long id);
}
