package com.aurionpro.payroll.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.OrganizationRequest;
import com.aurionpro.payroll.dto.response.OrganizationList;
import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.enums.UserStatus;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.DuplicateResourceException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.UserRepo;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService{

	@Autowired
    private OrganizationRepo organizationRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    
    @Override
    public OrganizationResponse createOrganization(OrganizationRequest request, Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        if (organizationRepo.existsByUser(user)) {
            throw new DuplicateResourceException("Organization", "user", userId);
        }
        
        if (request.getRegistrationNumber() != null && 
            organizationRepo.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Organization", "registrationNumber", 
                request.getRegistrationNumber());
        }
        
        Organization organization = modelMapper.map(request, Organization.class);
        organization.setUser(user);
        organization.setVerified(false);
        
        Organization savedOrganization = organizationRepo.save(organization);
        
        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), organization.getName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
        
        return modelMapper.map(savedOrganization, OrganizationResponse.class);
    }
    
    
    @Override
    public OrganizationResponse getOrganizationById(Long id) {
        Organization organization = organizationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        return modelMapper.map(organization, OrganizationResponse.class);
    }
    
    @Override
    public OrganizationResponse getOrganizationByUserId(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Organization organization = organizationRepo.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "userId", userId));
        
        return modelMapper.map(organization, OrganizationResponse.class);
    }
    
    
    @Override
    public OrganizationResponse updateOrganization(Long id, OrganizationRequest request) {
        Organization organization = organizationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        if (request.getRegistrationNumber() != null && 
            !request.getRegistrationNumber().equals(organization.getRegistrationNumber()) &&
            organizationRepo.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Organization", "registrationNumber", 
                request.getRegistrationNumber());
        }
        
        organization.setName(request.getName());
        organization.setRegistrationNumber(request.getRegistrationNumber());
        organization.setAddress(request.getAddress());
        organization.setContactPhone(request.getContactPhone());
        
        Organization updatedOrganization = organizationRepo.save(organization);
        
        return modelMapper.map(updatedOrganization, OrganizationResponse.class);
    }
    
    
    @Override
    public List<OrganizationList> getAllOrganizations() {
        List<Organization> organizations = organizationRepo.findAll();
        
        return organizations.stream()
            .map(org -> {
                OrganizationList dto = modelMapper.map(org, OrganizationList.class);
                dto.setUserStatus(org.getUser().getStatus()); 
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public List<OrganizationList> getOrganizationsByVerificationStatus(Boolean verified) {
        List<Organization> organizations = organizationRepo.findByVerified(verified);
        
        return organizations.stream()
            .map(org -> modelMapper.map(org, OrganizationList.class))
            .collect(Collectors.toList());
    }
    
    
    @Override
    public OrganizationResponse verifyOrganization(Long id, String remarks) {
        Organization organization = organizationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        if (organization.getVerified()) {
            throw new BadRequestException("Organization is already verified");
        }
        
        organization.setVerified(true);
        organization.setRemarks(remarks);
        
        User user = organization.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
        
        Organization verifiedOrganization = organizationRepo.save(organization);

        // Send approval email
        try {
            emailService.sendOrganizationVerificationEmail(
                user.getEmail(),
                organization.getName(),
                true
            );
            System.out.println("✅ Approval email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send approval email: " + e.getMessage());
        }
        
        return modelMapper.map(verifiedOrganization, OrganizationResponse.class);
    }
    
    
    
    @Override
    public OrganizationResponse rejectOrganization(Long id, String remarks) {
        Organization organization = organizationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        if (organization.getVerified()) {
            throw new BadRequestException("Cannot reject an already verified organization");
        }
        
        organization.setVerified(false);
        organization.setRemarks(remarks != null ? remarks : "Verification rejected by admin");
        
        User user = organization.getUser();
        user.setStatus(UserStatus.INACTIVE);
        userRepo.save(user);
        
        Organization rejectedOrganization = organizationRepo.save(organization);

        // Send rejection email
        try {
            emailService.sendOrganizationVerificationEmail(
                user.getEmail(),
                organization.getName(),
                false
            );
            System.out.println("✅ Rejection email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send rejection email: " + e.getMessage());
        }
        
        return modelMapper.map(rejectedOrganization, OrganizationResponse.class);
    }
    

    @Override
    public OrganizationResponse uploadVerificationDocuments(Long id, MultipartFile file) {
        Organization organization = organizationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        // Log file details
        System.out.println("=== UPLOADING ORGANIZATION DOCUMENT ===");
        System.out.println("File Name: " + file.getOriginalFilename());
        System.out.println("Content Type: " + file.getContentType());
        System.out.println("File Size: " + String.format("%.2f KB", file.getSize() / 1024.0));
        System.out.println("Organization ID: " + id);
        System.out.println("========================================");
        
        try {
            // Upload to Cloudinary
            String documentUrl = cloudinaryService.uploadFile(file, "organization_documents");
            
            organization.setVerificationDocumentsUrl(documentUrl);
            Organization updatedOrganization = organizationRepo.save(organization);
            
//            System.out.println("✅ File uploaded successfully!");
//            System.out.println("🔗 URL: " + documentUrl);
            
            return modelMapper.map(updatedOrganization, OrganizationResponse.class);
            
        } catch (Exception e) {
//            System.err.println("❌ File upload failed: " + e.getMessage());
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
    }
    
    
    @Override
    public void deleteOrganization(Long id) {
        Organization organization = organizationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        User user = organization.getUser();
        user.setStatus(UserStatus.INACTIVE);
        userRepo.save(user);
        
        organization.setVerified(false);
        organization.setRemarks("Organization deleted by admin");
        organizationRepo.save(organization);
    }
    
    
    
    
}
