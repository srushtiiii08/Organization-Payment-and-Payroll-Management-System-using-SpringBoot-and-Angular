package com.aurionpro.payroll.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.dto.request.PaymentRequestRequest;
import com.aurionpro.payroll.dto.response.PaymentRequestList;
import com.aurionpro.payroll.dto.response.PaymentRequestResponse;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.enums.PaymentRequestStatus;
import com.aurionpro.payroll.enums.PaymentRequestType;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.PaymentRequestRepo;
import com.aurionpro.payroll.repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PaymentRequestServiceImpl implements PaymentRequestService{

	@Autowired
    private PaymentRequestRepo paymentRequestRepo;
    
    @Autowired
    private OrganizationRepo organizationRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailService emailService;
    
    
    @Override
    public PaymentRequestResponse createPaymentRequest(PaymentRequestRequest request, Long organizationId) {
    	Organization organization = organizationRepo.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
    	
    	//check if org exists
        if (!organization.getVerified()) {
                throw new BadRequestException("Cannot create payment request for unverified organization");
        }
        
     
        //CHECK FOR DUPLICATE BASED ON REQUEST TYPE
        if (request.getRequestType() == PaymentRequestType.SALARY_DISBURSEMENT) {
            // ⭐ RULE 1: Only ONE salary disbursement per month (any status except REJECTED)
            List<com.aurionpro.payroll.entity.PaymentRequest> existingSalaryRequests = 
                paymentRequestRepo.findByOrganizationAndMonthAndYear(organization, request.getMonth(), request.getYear());
            
            boolean hasSalaryRequest = existingSalaryRequests.stream()
                .anyMatch(pr -> pr.getRequestType() == PaymentRequestType.SALARY_DISBURSEMENT && 
                               pr.getStatus() != PaymentRequestStatus.REJECTED);
            
            if (hasSalaryRequest) {
                throw new BadRequestException(
                    "A salary disbursement request already exists for " + request.getMonth() + " " + request.getYear() + 
                    ". You can only create one salary disbursement per month.");
            }
        } else if (request.getRequestType() == PaymentRequestType.VENDOR_PAYMENT) {
            // ⭐ RULE 2: Vendor payments are allowed - no duplicate check needed
            // Each vendor can have their own payment request per month
            System.out.println("✅ Vendor payment - no duplicate check needed");
        }
     
            
        //dto to entity using mm
        com.aurionpro.payroll.entity.PaymentRequest paymentRequest = 
                modelMapper.map(request, com.aurionpro.payroll.entity.PaymentRequest.class);
            
        //set sys controlled fields
        paymentRequest.setOrganization(organization);
        paymentRequest.setStatus(PaymentRequestStatus.PENDING);
            
        //save to db
        com.aurionpro.payroll.entity.PaymentRequest savedRequest = paymentRequestRepo.save(paymentRequest);
            
        //entity to resp dto
        return modelMapper.map(savedRequest, PaymentRequestResponse.class);
    }
    
    @Override
    public PaymentRequestResponse getPaymentRequestById(Long id) {
        com.aurionpro.payroll.entity.PaymentRequest paymentRequest = paymentRequestRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", id));
        
        return modelMapper.map(paymentRequest, PaymentRequestResponse.class);
    }
    
    @Override
    public List<PaymentRequestResponse> getAllPaymentRequestsByOrganization(Long organizationId) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<com.aurionpro.payroll.entity.PaymentRequest> requests = 
            paymentRequestRepo.findByOrganizationOrderByCreatedAtDesc(organization);
        
        return requests.stream()
            .map(req -> modelMapper.map(req, PaymentRequestResponse.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentRequestList> getAllPaymentRequests() {
        List<com.aurionpro.payroll.entity.PaymentRequest> requests = paymentRequestRepo.findAll();
        
        return requests.stream()
            .map(req -> modelMapper.map(req, PaymentRequestList.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentRequestList> getPaymentRequestsByStatus(PaymentRequestStatus status) {
        List<com.aurionpro.payroll.entity.PaymentRequest> requests = 
            paymentRequestRepo.findByStatus(status);
        
        return requests.stream()
            .map(req -> modelMapper.map(req, PaymentRequestList.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public PaymentRequestResponse approvePaymentRequest(Long id, Long bankAdminId) {
        com.aurionpro.payroll.entity.PaymentRequest paymentRequest = paymentRequestRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", id));
        
        if (paymentRequest.getStatus() != PaymentRequestStatus.PENDING) {
            throw new BadRequestException("Payment request has already been processed");
        }
        
        User bankAdmin = userRepo.findById(bankAdminId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", bankAdminId));
        
        paymentRequest.setStatus(PaymentRequestStatus.APPROVED);
        paymentRequest.setApprovedBy(bankAdmin);
        paymentRequest.setApprovedAt(LocalDateTime.now());
        
        com.aurionpro.payroll.entity.PaymentRequest approvedRequest = paymentRequestRepo.save(paymentRequest);

        // Send email to organization
        try {
            Organization organization = paymentRequest.getOrganization();
            emailService.sendPaymentRequestStatusEmail(
                organization.getUser().getEmail(),
                organization.getName(),
                "APPROVED"
            );
            System.out.println("✅ Payment approval email sent to: " + organization.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send payment approval email: " + e.getMessage());
        }
        
        return modelMapper.map(approvedRequest, PaymentRequestResponse.class);
    }
    
    @Override
    public PaymentRequestResponse rejectPaymentRequest(Long id, Long bankAdminId, String remarks) {
        com.aurionpro.payroll.entity.PaymentRequest paymentRequest = paymentRequestRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", id));
        
        if (paymentRequest.getStatus() != PaymentRequestStatus.PENDING) {
            throw new BadRequestException("Payment request has already been processed");
        }
        
        User bankAdmin = userRepo.findById(bankAdminId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", bankAdminId));
        
        paymentRequest.setStatus(PaymentRequestStatus.REJECTED);
        paymentRequest.setApprovedBy(bankAdmin);
        paymentRequest.setApprovedAt(LocalDateTime.now());
        paymentRequest.setRemarks(remarks);
        
        com.aurionpro.payroll.entity.PaymentRequest rejectedRequest = paymentRequestRepo.save(paymentRequest);

        // Send email to organization
        try {
            Organization organization = paymentRequest.getOrganization();
            emailService.sendPaymentRequestStatusEmail(
                organization.getUser().getEmail(),
                organization.getName(),
                "REJECTED"
            );
            System.out.println("✅ Payment rejection email sent to: " + organization.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send payment rejection email: " + e.getMessage());
        }
        
        return modelMapper.map(rejectedRequest, PaymentRequestResponse.class);
    }
    
    @Override
    public void deletePaymentRequest(Long id) {
    	com.aurionpro.payroll.entity.PaymentRequest paymentRequest = paymentRequestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", id));
            
        if (paymentRequest.getStatus() == PaymentRequestStatus.APPROVED) {
                throw new BadRequestException("Cannot delete approved payment request");
        }
            
        paymentRequestRepo.delete(paymentRequest);
    }

	
        
}
