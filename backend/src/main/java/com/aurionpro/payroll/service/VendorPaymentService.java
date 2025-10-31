package com.aurionpro.payroll.service;

import java.util.List;

import com.aurionpro.payroll.dto.request.VendorPaymentRequest;
import com.aurionpro.payroll.dto.response.VendorPaymentResponse;

public interface VendorPaymentService {

	VendorPaymentResponse createVendorPayment(VendorPaymentRequest request, Long vendorId, Long paymentRequestId);
    
    VendorPaymentResponse getVendorPaymentById(Long id);
    
    List<VendorPaymentResponse> getVendorPaymentsByVendor(Long vendorId);
    
    List<VendorPaymentResponse> getVendorPaymentsByOrganization(Long organizationId);
    
    VendorPaymentResponse updatePaymentStatus(Long id, com.aurionpro.payroll.enums.PaymentStatus status);

}
