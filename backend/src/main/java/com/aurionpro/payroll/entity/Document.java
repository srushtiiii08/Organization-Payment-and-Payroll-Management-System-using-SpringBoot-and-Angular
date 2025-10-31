package com.aurionpro.payroll.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.aurionpro.payroll.enums.DocumentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entityType;         // ORGANIZATION, EMPLOYEE, VENDOR, CONCERN
    
    @Column(nullable = false)
    private Long entityId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;
    
    @Column(nullable = false)
    private String cloudinaryUrl;
    
    @Column(nullable = false)
    private String cloudinaryPublicId;
    
    private String fileName;
    
    private String fileType; // PDF, JPG, PNG
    
    private Long fileSize;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;
}
