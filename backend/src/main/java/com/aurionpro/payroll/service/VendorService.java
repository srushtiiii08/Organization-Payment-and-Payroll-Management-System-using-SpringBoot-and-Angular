package com.aurionpro.payroll.service;

import java.util.List;

import com.aurionpro.payroll.dto.request.VendorRequest;
import com.aurionpro.payroll.dto.response.VendorList;
import com.aurionpro.payroll.dto.response.VendorResponse;
import com.aurionpro.payroll.enums.VendorStatus;

public interface VendorService {
    
	VendorResponse createVendor(VendorRequest request, Long organizationId);
    
    VendorResponse getVendorById(Long id);
    
    VendorResponse updateVendor(Long id, VendorRequest request);
    
    List<VendorList> getAllVendorsByOrganization(Long organizationId);
    
    List<VendorList> getVendorsByStatus(Long organizationId, VendorStatus status);
    
    VendorResponse updateVendorStatus(Long id, VendorStatus status);
    
    void deleteVendor(Long id);
}
