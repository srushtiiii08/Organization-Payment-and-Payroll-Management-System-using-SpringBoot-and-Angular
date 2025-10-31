package com.aurionpro.payroll.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.Vendor;
import com.aurionpro.payroll.enums.VendorStatus;

@Repository
public interface VendorRepo extends JpaRepository<Vendor, Long>{

	// Find by organization
    List<Vendor> findByOrganizationId(Long organizationId);
    
    // Find by name (case-insensitive)
    Optional<Vendor> findByNameIgnoreCase(String name);
    
    // Find by organization and name
    Optional<Vendor> findByOrganizationIdAndNameIgnoreCase(Long organizationId, String name);
    
    // Find by  email
    Optional<Vendor> findByEmail(String email);
    
    // Find by organization and status
    List<Vendor> findByOrganizationIdAndStatus(Long organizationId, VendorStatus status);
    
    // Check if vendor name exists for organization
    boolean existsByOrganizationIdAndNameIgnoreCase(Long organizationId, String name);
    
    // Count vendors by organization
    long countByOrganizationId(Long organizationId);
    
    // Count active vendors by organization
    long countByOrganizationIdAndStatus(Long organizationId, VendorStatus status);

    List<Vendor> findByOrganization(Organization organization);
    
    List<Vendor> findByOrganizationAndStatus(Organization organization, VendorStatus status);
    
    List<Vendor> findByStatus(VendorStatus status);
    
    boolean existsByEmail(String email);
    
    boolean existsByOrganizationAndName(Organization organization, String name);
    
    long countByOrganization(Organization organization);
    
    long countByOrganizationAndStatus(Organization organization, VendorStatus status);
}

