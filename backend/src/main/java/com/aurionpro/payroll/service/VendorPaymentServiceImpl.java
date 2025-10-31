package com.aurionpro.payroll.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.dto.request.VendorPaymentRequest;
import com.aurionpro.payroll.dto.response.VendorPaymentResponse;
import com.aurionpro.payroll.entity.Vendor;
import com.aurionpro.payroll.entity.VendorPayment;
import com.aurionpro.payroll.enums.PaymentStatus;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.PaymentRequestRepo;
import com.aurionpro.payroll.repo.VendorPaymentRepo;
import com.aurionpro.payroll.repo.VendorRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class VendorPaymentServiceImpl implements VendorPaymentService{

	@Autowired
    private VendorPaymentRepo vendorPaymentRepo;
    
    @Autowired
    private VendorRepo vendorRepo;
    
    @Autowired
    private PaymentRequestRepo paymentRequestRepo;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Override
    public VendorPaymentResponse createVendorPayment(VendorPaymentRequest request, Long vendorId, Long paymentRequestId) {
        Vendor vendor = vendorRepo.findById(vendorId)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));
        
        com.aurionpro.payroll.entity.PaymentRequest paymentRequest = paymentRequestRepo.findById(paymentRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", paymentRequestId));
        
        VendorPayment vendorPayment = modelMapper.map(request, VendorPayment.class);
        vendorPayment.setVendor(vendor);
        vendorPayment.setPaymentRequest(paymentRequest);
        vendorPayment.setStatus(PaymentStatus.PENDING);
        vendorPayment.setPaymentDate(LocalDate.now());
        vendorPayment.setTransactionId("VTX" + System.currentTimeMillis());
        
        VendorPayment savedPayment = vendorPaymentRepo.save(vendorPayment);
        
        return modelMapper.map(savedPayment, VendorPaymentResponse.class);
    }
    
    @Override
    public VendorPaymentResponse getVendorPaymentById(Long id) {
        VendorPayment vendorPayment = vendorPaymentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("VendorPayment", "id", id));
        
        return modelMapper.map(vendorPayment, VendorPaymentResponse.class);
    }
    
    
    @Override
    public List<VendorPaymentResponse> getVendorPaymentsByVendor(Long vendorId) {
        Vendor vendor = vendorRepo.findById(vendorId)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));
        
        List<VendorPayment> payments = vendorPaymentRepo.findByVendorOrderByPaymentDateDesc(vendor);
        
        return payments.stream()
            .map(payment -> modelMapper.map(payment, VendorPaymentResponse.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VendorPaymentResponse> getVendorPaymentsByOrganization(Long organizationId) {
        List<VendorPayment> payments = vendorPaymentRepo.findByOrganizationId(organizationId);
        
        return payments.stream()
            .map(payment -> modelMapper.map(payment, VendorPaymentResponse.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public VendorPaymentResponse updatePaymentStatus(Long id, PaymentStatus status) {
        VendorPayment vendorPayment = vendorPaymentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("VendorPayment", "id", id));
        
        vendorPayment.setStatus(status);
        
        VendorPayment updatedPayment = vendorPaymentRepo.save(vendorPayment);
        
        return modelMapper.map(updatedPayment, VendorPaymentResponse.class);
    }
}
