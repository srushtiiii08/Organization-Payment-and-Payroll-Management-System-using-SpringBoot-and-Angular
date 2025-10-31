package com.aurionpro.payroll.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aurionpro.payroll.dto.request.ConcernRequest;
import com.aurionpro.payroll.dto.request.EmployeeRequest;
import com.aurionpro.payroll.dto.request.OrganizationRequest;
import com.aurionpro.payroll.dto.request.SalaryStructureRequest;
import com.aurionpro.payroll.dto.request.VendorPaymentRequest;
import com.aurionpro.payroll.dto.request.VendorRequest;
import com.aurionpro.payroll.dto.response.ConcernList;
import com.aurionpro.payroll.dto.response.ConcernResponse;
import com.aurionpro.payroll.dto.response.EmployeeList;
import com.aurionpro.payroll.dto.response.EmployeeProfile;
import com.aurionpro.payroll.dto.response.EmployeeResponse;
import com.aurionpro.payroll.dto.response.LoginResponse;
import com.aurionpro.payroll.dto.response.OrganizationList;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.dto.response.PaymentRequestList;
import com.aurionpro.payroll.dto.response.PaymentRequestResponse;
import com.aurionpro.payroll.dto.response.SalaryPaymentHistory;
import com.aurionpro.payroll.dto.response.SalaryPaymentResponse;
import com.aurionpro.payroll.dto.response.SalaryStructureResponse;
import com.aurionpro.payroll.dto.response.VendorList;
import com.aurionpro.payroll.dto.response.VendorPaymentResponse;
import com.aurionpro.payroll.dto.response.VendorResponse;
import com.aurionpro.payroll.entity.Concern;
import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.PaymentRequest;
import com.aurionpro.payroll.entity.SalaryPayment;
import com.aurionpro.payroll.entity.SalaryStructure;
import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.entity.Vendor;
import com.aurionpro.payroll.entity.VendorPayment;

@Configuration
public class ModelMapperConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        
        // LOOSE Configuration - No validation errors
        mm.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.LOOSE)
            .setFieldMatchingEnabled(true)
            .setSkipNullEnabled(true)
            .setAmbiguityIgnored(true)
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        
        // =====================================================
        // USER MAPPINGS
        // =====================================================
        
        // User -> LoginResponse (token and message set in service)
        mm.typeMap(User.class, LoginResponse.class)
            .addMappings(m -> {
                m.map(User::getId, LoginResponse::setUserId);
                m.map(User::getEmail, LoginResponse::setEmail);
                m.map(User::getRole, LoginResponse::setRole);
                m.skip(LoginResponse::setToken);
                m.skip(LoginResponse::setMessage);
            });
        
        // =====================================================
        // ORGANIZATION MAPPINGS
        // =====================================================
        
        // Organization -> OrganizationResponse
        mm.typeMap(Organization.class, OrganizationResponse.class)
        .addMappings(m -> {
            m.map(src -> src.getUser().getId(), OrganizationResponse::setUserId);
            m.map(src -> src.getUser().getEmail(), OrganizationResponse::setContactEmail);
            m.using(employeeCountConverter).map(src -> src, OrganizationResponse::setEmployeeCount);
            m.using(vendorCountConverter).map(src -> src, OrganizationResponse::setVendorCount);
        });
        
        // Organization -> OrganizationList
        mm.typeMap(Organization.class, OrganizationList.class)
            .addMappings(m -> {
                m.using(employeeCountConverter).map(src -> src, OrganizationList::setEmployeeCount);
            });
        
        // OrganizationRequest -> Organization
        // Use emptyTypeMap to avoid implicit mapping conflicts
        TypeMap<OrganizationRequest, Organization> organizationRequestMap = 
            mm.emptyTypeMap(OrganizationRequest.class, Organization.class);
        
        organizationRequestMap.addMappings(m -> {
            m.skip(Organization::setId);
            m.skip(Organization::setUser);
            m.skip(Organization::setVerified);
            m.skip(Organization::setRemarks);
            m.skip(Organization::setVendors);
            m.skip(Organization::setEmployees);
            m.skip(Organization::setPaymentRequests);
            m.skip(Organization::setConcerns);
            m.skip(Organization::setCreatedAt);
            m.skip(Organization::setUpdatedAt);
        });
        
        // Explicitly map the fields we want
        organizationRequestMap.implicitMappings();
        
        
        // =====================================================
        // EMPLOYEE MAPPINGS
        // =====================================================
        
        // Employee -> EmployeeResponse
        mm.typeMap(Employee.class, EmployeeResponse.class)
        .addMappings(m -> {
            m.map(src -> src.getOrganization().getId(), EmployeeResponse::setOrganizationId);
            m.map(src -> src.getOrganization().getName(), EmployeeResponse::setOrganizationName);
            m.map(src -> src.getUser().getId(), EmployeeResponse::setUserId);
        });
        
        // Employee -> EmployeeList
        mm.typeMap(Employee.class, EmployeeList.class);
        
        // Employee -> EmployeeProfile
        mm.typeMap(Employee.class, EmployeeProfile.class)
            .addMappings(m -> {
                m.map(src -> src.getOrganization().getName(), EmployeeProfile::setOrganizationName);
                m.using(currentSalaryConverter).map(src -> src, EmployeeProfile::setCurrentSalary);
            });
        
        // EmployeeRequest -> Employee
        // empty TypeMap to avoid implicit nested user mapping conflicts : Not able to skip user, because there are already nested properties are mapped
        TypeMap<EmployeeRequest, Employee> employeeRequestMap = 
                mm.emptyTypeMap(EmployeeRequest.class, Employee.class);
            
        employeeRequestMap.addMappings(m -> {
                m.skip(Employee::setId);
                m.skip(Employee::setOrganization);
                m.skip(Employee::setUser);
                m.skip(Employee::setAccountVerificationStatus);
                m.skip(Employee::setStatus);
                m.skip(Employee::setAccountProofUrl);
                m.skip(Employee::setSalaryStructures);
                m.skip(Employee::setSalaryPayments);
                m.skip(Employee::setConcerns);
                m.skip(Employee::setCreatedAt);
                m.skip(Employee::setUpdatedAt);
            });
            
         // Enable implicit mappings for remaining fields
         employeeRequestMap.implicitMappings();
        
         
        // =====================================================
        // SALARY STRUCTURE MAPPINGS
        // =====================================================
        
        // SalaryStructure -> SalaryStructureResponse
        mm.typeMap(SalaryStructure.class, SalaryStructureResponse.class)
            .addMappings(m -> {
                m.map(src -> src.getEmployee().getId(), SalaryStructureResponse::setEmployeeId);
                m.map(src -> src.getEmployee().getName(), SalaryStructureResponse::setEmployeeName);
            });
        
        // SalaryStructureRequest -> SalaryStructure
        // Use emptyTypeMap to prevent implicit nested mappings
        TypeMap<SalaryStructureRequest, SalaryStructure> salaryStructureRequestMap =
                mm.emptyTypeMap(SalaryStructureRequest.class, SalaryStructure.class);
            
        
        salaryStructureRequestMap.addMappings(m -> {
                m.skip(SalaryStructure::setId);
                m.skip(SalaryStructure::setEmployee);
                m.skip(SalaryStructure::setGrossSalary);
                m.skip(SalaryStructure::setNetSalary);
                m.skip(SalaryStructure::setIsActive);
                m.skip(SalaryStructure::setCreatedAt);
                m.skip(SalaryStructure::setUpdatedAt);
            });
            
        // Enable implicit mappings for remaining fields
        salaryStructureRequestMap.implicitMappings();
        
        
        // =====================================================
        // SALARY PAYMENT MAPPINGS
        // =====================================================
        
        // SalaryPayment -> SalaryPaymentResponse
        mm.typeMap(SalaryPayment.class, SalaryPaymentResponse.class)
            .addMappings(m -> {
                m.map(src -> src.getEmployee().getId(), SalaryPaymentResponse::setEmployeeId);
                m.map(src -> src.getEmployee().getName(), SalaryPaymentResponse::setEmployeeName);
            });
        
        // SalaryPayment -> SalaryPaymentHistory
        mm.typeMap(SalaryPayment.class, SalaryPaymentHistory.class);
        
        
        // =====================================================
        // PAYMENT REQUEST MAPPINGS
        // =====================================================
        
        // PaymentRequest -> PaymentRequestResponse
        mm.typeMap(PaymentRequest.class, PaymentRequestResponse.class)
            .addMappings(m -> {
                m.map(src -> src.getOrganization().getId(), PaymentRequestResponse::setOrganizationId);
                m.map(src -> src.getOrganization().getName(), PaymentRequestResponse::setOrganizationName);
                m.using(approvedByConverter).map(src -> src, PaymentRequestResponse::setApprovedBy);
                m.using(approvedByNameConverter).map(src -> src, PaymentRequestResponse::setApprovedByName);
            });
        
        // PaymentRequest -> PaymentRequestList
        mm.typeMap(PaymentRequest.class, PaymentRequestList.class)
            .addMappings(m -> {
                m.map(src -> src.getOrganization().getName(), PaymentRequestList::setOrganizationName);
            });
        
        // com.aurionpro.payroll.dto.request.PaymentRequest -> PaymentRequest entity
        // Use emptyTypeMap to prevent implicit nested mappings
        TypeMap<com.aurionpro.payroll.dto.request.PaymentRequestRequest, com.aurionpro.payroll.entity.PaymentRequest> paymentRequestMap =
        		mm.emptyTypeMap(com.aurionpro.payroll.dto.request.PaymentRequestRequest.class, com.aurionpro.payroll.entity.PaymentRequest.class);
        
        paymentRequestMap.addMappings(m -> {
            m.skip(com.aurionpro.payroll.entity.PaymentRequest::setId);
            m.skip(com.aurionpro.payroll.entity.PaymentRequest::setOrganization);
            m.skip(com.aurionpro.payroll.entity.PaymentRequest::setStatus);
            m.skip(com.aurionpro.payroll.entity.PaymentRequest::setApprovedBy);
        });
        
        // Enable implicit mappings for remaining fields
        paymentRequestMap.implicitMappings();
        
        
        // =====================================================
        // VENDOR MAPPINGS
        // =====================================================
        
        // Vendor -> VendorResponse
        mm.typeMap(Vendor.class, VendorResponse.class)
            .addMappings(m -> {
                m.map(src -> src.getOrganization().getId(), VendorResponse::setOrganizationId);
            });
        
        // Vendor -> VendorList
        mm.typeMap(Vendor.class, VendorList.class);
        
        // VendorRequest -> Vendor
        // Use emptyTypeMap to prevent implicit nested mappings
        TypeMap<VendorRequest, Vendor> vendorRequestMap =
                mm.emptyTypeMap(VendorRequest.class, Vendor.class);
        
        vendorRequestMap.addMappings(m -> {
            m.skip(Vendor::setId);
            m.skip(Vendor::setOrganization);
            m.skip(Vendor::setStatus);
            m.skip(Vendor::setVendorPayments);
            m.skip(Vendor::setCreatedAt);
            m.skip(Vendor::setUpdatedAt);
        });
        
        // Enable implicit mappings for remaining fields
        vendorRequestMap.implicitMappings();
        
        
        // =====================================================
        // VENDOR PAYMENT MAPPINGS
        // =====================================================
        
        // VendorPayment -> VendorPaymentResponse
        mm.typeMap(VendorPayment.class, VendorPaymentResponse.class)
            .addMappings(m -> {
                m.map(src -> src.getVendor().getId(), VendorPaymentResponse::setVendorId);
                m.map(src -> src.getVendor().getName(), VendorPaymentResponse::setVendorName);
            });
        
        // VendorPaymentRequest -> VendorPayment
        // Use emptyTypeMap to prevent implicit nested mappings
        TypeMap<VendorPaymentRequest, VendorPayment> vendorPaymentRequestMap =
                mm.emptyTypeMap(VendorPaymentRequest.class, VendorPayment.class);
        
        vendorPaymentRequestMap.addMappings(m -> {
            m.skip(VendorPayment::setId);
            m.skip(VendorPayment::setVendor);
            m.skip(VendorPayment::setPaymentRequest);
            m.skip(VendorPayment::setStatus);
            m.skip(VendorPayment::setPaymentDate);
            m.skip(VendorPayment::setTransactionId);
            m.skip(VendorPayment::setCreatedAt);
            m.skip(VendorPayment::setUpdatedAt);
        });
        
        // Enable implicit mappings for remaining fields
        vendorPaymentRequestMap.implicitMappings();
        
        
        
        // =====================================================
        // CONCERN MAPPINGS
        // =====================================================
        
        // Concern -> ConcernResponse
        mm.typeMap(Concern.class, ConcernResponse.class)
            .addMappings(m -> {
                m.map(src -> src.getEmployee().getId(), ConcernResponse::setEmployeeId);
                m.map(src -> src.getEmployee().getName(), ConcernResponse::setEmployeeName);
                m.map(src -> src.getOrganization().getId(), ConcernResponse::setOrganizationId);
                m.using(respondedByConverter).map(src -> src, ConcernResponse::setRespondedBy);
                m.using(respondedByNameConverter).map(src -> src, ConcernResponse::setRespondedByName);
            });
        
        // Concern -> ConcernList
        mm.typeMap(Concern.class, ConcernList.class)
        .addMappings(m -> {
            // Map ID
            m.map(Concern::getId, ConcernList::setId);
            
            // Map subject (also set as title for frontend compatibility)
            m.map(Concern::getSubject, ConcernList::setSubject);
            m.map(Concern::getDescription, ConcernList::setDescription);
            
            // Map enums directly
            m.map(Concern::getPriority, ConcernList::setPriority);
            m.map(Concern::getStatus, ConcernList::setStatus);
            
            // Map timestamp - use getRaisedAt instead of getCreatedAt
            m.map(Concern::getRaisedAt, ConcernList::setCreatedAt);
            
            // Map employee name safely
            m.map(src -> src.getEmployee() != null ? src.getEmployee().getName() : "Unknown", 
                  ConcernList::setEmployeeName);
        });
        
        
        
        // ConcernRequest -> Concern
        // Use emptyTypeMap to prevent implicit nested mappings
        TypeMap<ConcernRequest, Concern> concernRequestMap =
                mm.emptyTypeMap(ConcernRequest.class, Concern.class);
        
        concernRequestMap.addMappings(m -> {
            m.skip(Concern::setId);
            m.skip(Concern::setEmployee);
            m.skip(Concern::setOrganization);
            m.skip(Concern::setStatus);
            m.skip(Concern::setResponse);
            m.skip(Concern::setRespondedBy);
            m.skip(Concern::setRespondedAt);
            m.skip(Concern::setRaisedAt);
            m.skip(Concern::setUpdatedAt);
        });
        
        // Enable implicit mappings for remaining fields
        concernRequestMap.implicitMappings();
        
        return mm;
    }
    
    // =====================================================
    // CUSTOM CONVERTERS
    // =====================================================
    
    private final Converter<Organization, Integer> employeeCountConverter = ctx -> {
        Organization organization = ctx.getSource();
        return (organization != null && organization.getEmployees() != null) 
            ? organization.getEmployees().size() : 0;
    };
    
    private final Converter<Organization, Integer> vendorCountConverter = ctx -> {
        Organization organization = ctx.getSource();
        return (organization != null && organization.getVendors() != null) 
            ? organization.getVendors().size() : 0;
    };
    
    private final Converter<Employee, java.math.BigDecimal> currentSalaryConverter = ctx -> {
        Employee employee = ctx.getSource();
        
        if (employee == null) return null;

        SalaryStructure activeStructure = employee.getActiveSalaryStructure(); // ✅ updated helper method
        return (activeStructure != null) ? activeStructure.getNetSalary() : null;
    };
    
    private final Converter<PaymentRequest, Long> approvedByConverter = ctx -> {
        PaymentRequest paymentRequest = ctx.getSource();
        return (paymentRequest != null && paymentRequest.getApprovedBy() != null) 
            ? paymentRequest.getApprovedBy().getId() : null;
    };
    
    private final Converter<PaymentRequest, String> approvedByNameConverter = ctx -> {
        PaymentRequest paymentRequest = ctx.getSource();
        return (paymentRequest != null && paymentRequest.getApprovedBy() != null) 
            ? paymentRequest.getApprovedBy().getEmail() : null;
    };
    
    private final Converter<Concern, Long> respondedByConverter = ctx -> {
        Concern concern = ctx.getSource();
        return (concern != null && concern.getRespondedBy() != null) 
            ? concern.getRespondedBy().getId() : null;
    };
    
    private final Converter<Concern, String> respondedByNameConverter = ctx -> {
        Concern concern = ctx.getSource();
        return (concern != null && concern.getRespondedBy() != null) 
            ? concern.getRespondedBy().getEmail() : null;
    };
}