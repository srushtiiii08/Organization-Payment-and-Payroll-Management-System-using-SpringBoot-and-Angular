package com.aurionpro.payroll.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.OrganizationRequest;
import com.aurionpro.payroll.dto.response.OrganizationList;
import com.aurionpro.payroll.dto.response.OrganizationResponse;

public interface OrganizationService {

    OrganizationResponse createOrganization(OrganizationRequest request, Long userId);
    
    OrganizationResponse getOrganizationById(Long id);
    
    OrganizationResponse getOrganizationByUserId(Long userId);
    
    OrganizationResponse updateOrganization(Long id, OrganizationRequest request);
    
    List<OrganizationList> getAllOrganizations();
    
    List<OrganizationList> getOrganizationsByVerificationStatus(Boolean verified);
    
    OrganizationResponse verifyOrganization(Long id, String remarks);
    
    OrganizationResponse rejectOrganization(Long id, String remarks);
    
    OrganizationResponse uploadVerificationDocuments(Long id, MultipartFile file);
    
    void deleteOrganization(Long id);
}