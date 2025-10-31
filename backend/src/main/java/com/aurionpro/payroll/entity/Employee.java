package com.aurionpro.payroll.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aurionpro.payroll.enums.AccountVerificationStatus;
import com.aurionpro.payroll.enums.EmployeeStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(length = 500)
    private String address;
    
    private String department;
    
    private String designation;
    
    private LocalDate dateOfJoining;
    
    private String bankAccountNumber;
    
    private String bankName;
    
    private String ifscCode;
    
    private String accountProofUrl; // Cloudinary URL
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl; // Cloudinary URL for profile picture
    
    @Enumerated(EnumType.STRING)
    private AccountVerificationStatus accountVerificationStatus = AccountVerificationStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE; // ACTIVE, INACTIVE, TERMINATED
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Relationships
//    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private SalaryStructure salaryStructure;
    
    
    
    //one emp should have multiple sal structure but only one active sal structure
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalaryStructure> salaryStructures;
    
    
    
    //HELPER METHOD to get the ACTIVE salary structure
    public SalaryStructure getActiveSalaryStructure() {
        if (salaryStructures == null || salaryStructures.isEmpty()) {
            return null;
        }
        return salaryStructures.stream()
            .filter(SalaryStructure::getIsActive)
            .findFirst()
            .orElse(null);
    }
    
  //HELPER METHOD to set (and enforces) the ACTIVE salary structure
    public void setActiveSalaryStructure(SalaryStructure newStructure) {
    	if (salaryStructures == null) {
            salaryStructures = new java.util.ArrayList<>();
        }

        // Deactivate all existing structures
        salaryStructures.forEach(s -> s.setIsActive(false));

        // Mark the new one active
        newStructure.setIsActive(true);
        newStructure.setEmployee(this); // maintain relationship

        // Add to list if not already present
        if (!salaryStructures.contains(newStructure)) {
            salaryStructures.add(newStructure);
        }
    }
    
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalaryPayment> salaryPayments;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Concern> concerns;
}
