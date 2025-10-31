package com.aurionpro.payroll.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.FileUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    
    @Autowired
    private Cloudinary cloudinary;

    // ✅ ALLOWED FILE TYPES - PDF, JPG, JPEG, PNG
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "application/pdf"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        
    	// ✅ VALIDATE FILE BEFORE UPLOAD
        validateFile(file);
        
    	try {
    		
    		// 🔧 DETERMINE RESOURCE TYPE BASED ON FILE
            String resourceType = "auto";
            String type = "upload";
            
            // For PDFs, explicitly use "raw" resource type
            if (file.getContentType() != null && file.getContentType().equals("application/pdf")) {
                resourceType = "raw";
            }
    		
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap("folder", folder,    //claudinary by default assume image file
                		"resource_type", resourceType,    //
                		"type", type,
                        "access_mode", "public"  //or else cant view file from frontend
                )
            );
            
            return uploadResult.get("secure_url").toString();
            
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }
    
    
 //Upload PDF from byte array
    @Override
    public String uploadPdf(byte[] pdfBytes, String fileName, String folder) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new BadRequestException("PDF data cannot be empty");
        }
        
        if (pdfBytes.length > MAX_FILE_SIZE) {
            throw new BadRequestException(
                String.format("PDF size exceeds maximum limit of %d MB", 
                    MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        try {
            Map uploadResult = cloudinary.uploader().upload(
                pdfBytes, 
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "raw",
                    "public_id", fileName,
                    "format", "pdf",
                    "type", "upload",
                    "access_mode", "public"
                )
            );
            
            return uploadResult.get("secure_url").toString();
            
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload PDF to Cloudinary: " + e.getMessage());
        }
    }
    
    //Upload Excel from byte array
    @Override
    public String uploadExcel(byte[] excelBytes, String fileName, String folder) {
        if (excelBytes == null || excelBytes.length == 0) {
            throw new BadRequestException("Excel data cannot be empty");
        }
        
        if (excelBytes.length > MAX_FILE_SIZE) {
            throw new BadRequestException(
                String.format("Excel size exceeds maximum limit of %d MB", 
                    MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        try {
            Map uploadResult = cloudinary.uploader().upload(
                excelBytes, 
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "raw",
                    "public_id", fileName,
                    "format", "xlsx"
                )
            );
            return uploadResult.get("secure_url").toString();
            
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload Excel to Cloudinary: " + e.getMessage());
        }
    }
    
    
    
    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new FileUploadException("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }
    
    // ✅ VALIDATION METHOD
    private void validateFile(MultipartFile file) {
    	
    	// Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                String.format("File size exceeds maximum limit of %d MB. Your file size: %.2f MB", 
                    MAX_FILE_SIZE / (1024 * 1024),
                    file.getSize() / (1024.0 * 1024.0))
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(
                String.format("Invalid file type: %s. Only PDF, JPG, JPEG, and PNG files are allowed.", 
                    contentType != null ? contentType : "unknown")
            );
        }
        

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png");
            
            if (!allowedExtensions.contains(extension)) {
                throw new BadRequestException(
                    String.format("Invalid file extension: .%s. Only .pdf, .jpg, .jpeg, and .png are allowed.", 
                        extension)
                );
            }
        }
    }
    
    // Helper method to extract file extension
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex + 1);
    }
        
    
}