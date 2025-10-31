package com.aurionpro.payroll.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.User;

@Repository
public interface OrganizationRepo extends JpaRepository<Organization, Long>{

	// FIND METHODS
    // Find by user entity (REQUIRED for Day 4 service)
    Optional<Organization> findByUser(User user);
    
    // Find by user id
    Optional<Organization> findByUserId(Long userId);
    
    // Find by name (case-insensitive)
    Optional<Organization> findByNameIgnoreCase(String name);
    
    // Find by registration number
    Optional<Organization> findByRegistrationNumber(String registrationNumber);
    
    // Find verified organizations
    List<Organization> findByVerified(Boolean verified);
    
    
    // EXISTS/CHECK METHODS
    // Check if organization exists for user (REQUIRED for Day 4 service)
    boolean existsByUser(User user);
    
    // Check if registration number exists
    boolean existsByRegistrationNumber(String registrationNumber);
    
    
    // CUSTOM QUERIES
    // Find organizations pending verification
    @Query("SELECT o FROM Organization o WHERE o.verified = false ORDER BY o.createdAt DESC")
    List<Organization> findPendingVerification();
    
    // Find organization with all employees
    @Query("SELECT o FROM Organization o LEFT JOIN FETCH o.employees WHERE o.id = :id")
    Optional<Organization> findByIdWithEmployees(@Param("id") Long id);
    
    // Find organization with all relations
    @Query("SELECT DISTINCT o FROM Organization o " +
           "LEFT JOIN FETCH o.employees " +
           "LEFT JOIN FETCH o.vendors " +
           "WHERE o.id = :id")
    Optional<Organization> findByIdWithAllRelations(@Param("id") Long id);
    
    
    // DEFAULT METHODS (CONVENIENCE)
    // Find verified organizations (convenience method)
    default List<Organization> findVerifiedOrganizations() {
        return findByVerified(true);
    }
    
    // Find pending verifications (convenience method)
    default List<Organization> findPendingVerifications() {
        return findByVerified(false);
    }
}
