package com.aurionpro.payroll.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.payroll.entity.Document;
import com.aurionpro.payroll.enums.DocumentType;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long>{

	// Find by entity type and entity id
    List<Document> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    // Find by document type
    List<Document> findByDocumentType(DocumentType documentType);
    
    // Find by entity and document type
    List<Document> findByEntityTypeAndEntityIdAndDocumentType(
        String entityType, 
        Long entityId, 
        DocumentType documentType
    );
    
    // Find by file type
    List<Document> findByFileType(String fileType);
    
    // Find documents uploaded in date range
    List<Document> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by cloudinary public id
    List<Document> findByCloudinaryPublicId(String publicId);
    
 // Find large files (greater than size)
    @Query("SELECT d FROM Document d WHERE d.fileSize > :size")
    List<Document> findFilesLargerThan(@Param("size") Long size);
    
    // Count documents by entity
    long countByEntityTypeAndEntityId(String entityType, Long entityId);
    
    // Delete by entity (for cleanup)
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);

}
