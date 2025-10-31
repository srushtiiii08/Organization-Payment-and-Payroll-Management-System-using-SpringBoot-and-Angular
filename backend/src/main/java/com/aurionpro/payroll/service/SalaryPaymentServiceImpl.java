package com.aurionpro.payroll.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.dto.response.SalaryPaymentHistory;
import com.aurionpro.payroll.dto.response.SalaryPaymentResponse;
import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.SalaryPayment;
import com.aurionpro.payroll.entity.SalaryStructure;
import com.aurionpro.payroll.enums.EmployeeStatus;
import com.aurionpro.payroll.enums.PaymentRequestStatus;
import com.aurionpro.payroll.enums.PaymentStatus;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.EmployeeRepo;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.PaymentRequestRepo;
import com.aurionpro.payroll.repo.SalaryPaymentRepo;
import com.aurionpro.payroll.repo.SalaryStructureRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SalaryPaymentServiceImpl implements SalaryPaymentService {
    
    @Autowired
    private SalaryPaymentRepo salaryPaymentRepo;
    
    @Autowired
    private PaymentRequestRepo paymentRequestRepo;

    @Autowired
    private SalaryStructureRepo salaryStructureRepo;
    
    @Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private OrganizationRepo organizationRepo;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private CloudinaryService cloudinaryService;  
    
    @Autowired
    private ModelMapper modelMapper;
    
    
    @Override
    public List<SalaryPaymentResponse> processSalaryPayments(Long paymentRequestId) {
    	// Check if paymentRequestId is null
        if (paymentRequestId == null) {
            throw new BadRequestException("Payment request ID cannot be null");
        }
        
        //Check if payment request exists
    	com.aurionpro.payroll.entity.PaymentRequest paymentRequest = paymentRequestRepo.findById(paymentRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentRequest", "id", paymentRequestId));
            
        if (paymentRequest.getStatus() != PaymentRequestStatus.APPROVED) {
                throw new BadRequestException("Payment request is not approved");
        }
        
        //Update status to PROCESSING at the start
        paymentRequest.setStatus(PaymentRequestStatus.PROCESSING);
        paymentRequestRepo.save(paymentRequest);
            
     // Check if organization exists
        Organization org = paymentRequest.getOrganization();
        if (org == null) {
            throw new BadRequestException("Organization not found for this payment request");
        }
        
        Organization organization = paymentRequest.getOrganization();
        
        //GET BOTH ACTIVE AND ON_LEAVE EMPLOYEES
        List<Employee> eligibleEmployees = employeeRepo.findByOrganizationAndStatus(organization, EmployeeStatus.ACTIVE);
        List<Employee> onLeaveEmployees = employeeRepo.findByOrganizationAndStatus(organization, EmployeeStatus.ON_LEAVE);

        //COMBINE BOTH LISTS
        List<Employee> allEmployees = new ArrayList<>();
        allEmployees.addAll(eligibleEmployees);
        allEmployees.addAll(onLeaveEmployees);
        
        List<SalaryPaymentResponse> responses = new ArrayList<>();
            
        for (Employee employee : allEmployees) {
        	
            if (salaryPaymentRepo.existsByEmployeeAndMonthAndYear(
                        employee, paymentRequest.getMonth(), paymentRequest.getYear())) {
                    continue;
            }
                
            SalaryStructure salaryStructure = employee.getActiveSalaryStructure();
            
            if (salaryStructure == null || !salaryStructure.getIsActive()) {
                    continue;
            }
                
            SalaryPayment salaryPayment = SalaryPayment.builder()
                        .employee(employee)
                        .paymentRequest(paymentRequest)
                        .month(paymentRequest.getMonth())
                        .year(paymentRequest.getYear())
                        .amount(salaryStructure.getNetSalary())    //netSalary = grossSalary - deductions
                        .basicSalary(salaryStructure.getBasicSalary())
                        .hra(salaryStructure.getHra())
                        .dearnessAllowance(salaryStructure.getDearnessAllowance())
                        .otherAllowances(salaryStructure.getOtherAllowances())
                        .grossSalary(salaryStructure.getGrossSalary())
                        .providentFund(salaryStructure.getProvidentFund())
                        .netSalary(salaryStructure.getNetSalary())
                        .paymentDate(LocalDate.now())
                        .status(PaymentStatus.COMPLETED)
                        .transactionId("TXN" + System.currentTimeMillis())
                        .build();
                    
            SalaryPayment savedPayment = salaryPaymentRepo.save(salaryPayment);


            //GENERATE PDF AND UPLOAD TO CLOUDINARY

            // GENERATE PDF
            byte[] pdfBytes = null;
            try {
                // Generate PDF
                pdfBytes = pdfGenerationService.generateSalarySlip(savedPayment);
                
                // Create filename
                String fileName = String.format("salary_slip_%d_%s_%s_%d",
                    savedPayment.getId(),
                    employee.getName().replace(" ", "_"),
                    paymentRequest.getMonth(),
                    paymentRequest.getYear()
                );
                
                // Upload to Cloudinary
                String pdfUrl = cloudinaryService.uploadPdf(pdfBytes, fileName, "salary-slips");
                
                // Save URL to database
                savedPayment.setSalarySlipUrl(pdfUrl);
                salaryPaymentRepo.save(savedPayment);
            } catch (Exception e) {
                System.err.println("❌ Failed to generate/upload PDF for: " + employee.getName());
                System.err.println("Error: " + e.getMessage());
            }
            
            // Send email notification w pdf
            try {
                emailService.sendSalaryPaymentNotification(
                    employee.getUser().getEmail(),
                    employee.getName(),
                    paymentRequest.getMonth(),
                    paymentRequest.getYear(),
                    pdfBytes	//passes the pdf bytes
                );
            } catch (Exception e) {
                System.err.println("❌ Failed to send salary notification: " + e.getMessage());
            }
            
            responses.add(modelMapper.map(savedPayment, SalaryPaymentResponse.class));
        }
        // Update status to COMPLETED after all payments processed
        paymentRequest.setStatus(PaymentRequestStatus.COMPLETED);
        paymentRequestRepo.save(paymentRequest);
        
        return responses;
    }
    
    
    @Override
    public SalaryPaymentResponse getSalaryPaymentById(Long id) {
        
    	// Validation 1: Check if ID is null
        if (id == null) {
            throw new BadRequestException("Salary payment ID cannot be null");
        }
        
    	//Check if salary payment exists
    	SalaryPayment salaryPayment = salaryPaymentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryPayment", "id", id));
        
        return modelMapper.map(salaryPayment, SalaryPaymentResponse.class);
    }
    
    @Override
    public List<SalaryPaymentHistory> getSalaryPaymentHistoryByEmployee(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        List<SalaryPayment> payments = salaryPaymentRepo.findByEmployeeOrderByPaymentDateDesc(employee);
        
     // Return empty list if no payments (not an error)
        if (payments.isEmpty()) {
            System.out.println("ℹ️ No salary payment history found for employee: " + employee.getName());
        }
        
        return payments.stream()
            .map(payment -> modelMapper.map(payment, SalaryPaymentHistory.class))
            .collect(Collectors.toList());
    }
    
    
    @Override
    public List<SalaryPaymentResponse> getSalaryPaymentsByOrganization(Long organizationId, String month, Integer year) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<SalaryPayment> payments = 
            salaryPaymentRepo.findByOrganizationAndMonthAndYear(organization, month, year);
        
        return payments.stream()
            .map(payment -> modelMapper.map(payment, SalaryPaymentResponse.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public byte[] generateSalarySlipPdf(Long salaryPaymentId) {
        SalaryPayment salaryPayment = salaryPaymentRepo.findById(salaryPaymentId)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryPayment", "id", salaryPaymentId));
        
        try {
            return pdfGenerationService.generateSalarySlip(salaryPayment);
        } catch (Exception e) {
            throw new BadRequestException("Failed to generate salary slip: " + e.getMessage());
        }
        
    }
    
    @Override
    public List<SalaryPaymentHistory> getSalaryPaymentHistoryByEmployeeAndYear(Long employeeId, Integer year) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        List<SalaryPayment> payments = salaryPaymentRepo.findByEmployeeAndYearOrderByPaymentDateDesc(employee, year);
        
        if (payments.isEmpty()) {
            System.out.println("ℹ️ No salary payment history found for employee: " + employee.getName() + " in year: " + year);
        }
        
        return payments.stream()
            .map(payment -> modelMapper.map(payment, SalaryPaymentHistory.class))
            .collect(Collectors.toList());
    }

}
