package com.aurionpro.payroll.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.dto.request.VendorRequest;
import com.aurionpro.payroll.dto.response.VendorList;
import com.aurionpro.payroll.dto.response.VendorResponse;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.Vendor;
import com.aurionpro.payroll.enums.VendorStatus;
import com.aurionpro.payroll.exception.DuplicateResourceException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.VendorRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class VendorServiceImpl implements VendorService{

	
	@Autowired
    private VendorRepo vendorRepo;
    
    @Autowired
    private OrganizationRepo organizationRepo;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Override
    public VendorResponse createVendor(VendorRequest request, Long organizationId) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        if (vendorRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Vendor", "email", request.getEmail());
        }
        
        if (vendorRepo.existsByOrganizationAndName(organization, request.getName())) {
            throw new DuplicateResourceException("Vendor", "name", request.getName());
        }
        
        Vendor vendor = modelMapper.map(request, Vendor.class);
        vendor.setOrganization(organization);
        vendor.setStatus(VendorStatus.ACTIVE);
        
        Vendor savedVendor = vendorRepo.save(vendor);
        
        return modelMapper.map(savedVendor, VendorResponse.class);
    }
    
    @Override
    public VendorResponse getVendorById(Long id) {
        Vendor vendor = vendorRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));
        
        return modelMapper.map(vendor, VendorResponse.class);
    }
    
    
    @Override
    public VendorResponse updateVendor(Long id, VendorRequest request) {
        Vendor vendor = vendorRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));
        
        if (!request.getEmail().equals(vendor.getEmail()) && 
            vendorRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Vendor", "email", request.getEmail());
        }
        
        vendor.setName(request.getName());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setAddress(request.getAddress());
        vendor.setServiceType(request.getServiceType());
        vendor.setGstNumber(request.getGstNumber());
        
        Vendor updatedVendor = vendorRepo.save(vendor);
        
        return modelMapper.map(updatedVendor, VendorResponse.class);
    }
    
    
    @Override
    public List<VendorList> getAllVendorsByOrganization(Long organizationId) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<Vendor> vendors = vendorRepo.findByOrganization(organization);
        
        return vendors.stream()
            .map(vendor -> modelMapper.map(vendor, VendorList.class))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VendorList> getVendorsByStatus(Long organizationId, VendorStatus status) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<Vendor> vendors = vendorRepo.findByOrganizationAndStatus(organization, status);
        
        return vendors.stream()
            .map(vendor -> modelMapper.map(vendor, VendorList.class))
            .collect(Collectors.toList());
    }
    
    
    @Override
    public VendorResponse updateVendorStatus(Long id, VendorStatus status) {
        Vendor vendor = vendorRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));
        
        vendor.setStatus(status);
        
        Vendor updatedVendor = vendorRepo.save(vendor);
        
        return modelMapper.map(updatedVendor, VendorResponse.class);
    }
    
    @Override
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));
        
        vendor.setStatus(VendorStatus.INACTIVE);
        vendorRepo.save(vendor);
    }
}
