package com.aurionpro.payroll.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aurionpro.payroll.enums.ConcernPriority;
import com.aurionpro.payroll.enums.ConcernStatus;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "concerns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Concern {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcernPriority priority = ConcernPriority.MEDIUM;
    
    
    @Column(length = 1000)
    private String attachmentUrl;      // Cloudinary URL (multiple URLs comma-separated)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcernStatus status = ConcernStatus.OPEN;
    
    @Column(length = 2000)
    private String response;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private User respondedBy;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime raisedAt;
    
    private LocalDateTime respondedAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
