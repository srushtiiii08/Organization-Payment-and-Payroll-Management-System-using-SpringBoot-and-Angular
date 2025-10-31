package com.aurionpro.payroll.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.service.CloudinaryService;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @PostMapping("/upload/organization-documents")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Map<String, String>> uploadOrganizationDocuments(
            @RequestParam("file") MultipartFile file) {
        
    	try {
            String url = cloudinaryService.uploadFile(file, "organization_documents");
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("fileUrl", url);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileType", file.getContentType());
            response.put("fileSize", String.valueOf(file.getSize()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Upload failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/upload/employee-account-proof")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, String>> uploadEmployeeAccountProof(
            @RequestParam("file") MultipartFile file) {

        try {
            String url = cloudinaryService.uploadFile(file, "employee_account_proofs");
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("fileUrl", url);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileType", file.getContentType());
            response.put("fileSize", String.valueOf(file.getSize()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Upload failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/upload/concern-attachment")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ORGANIZATION')")
    public ResponseEntity<Map<String, String>> uploadConcernAttachment(
            @RequestParam("file") MultipartFile file) {

        try {
            String url = cloudinaryService.uploadFile(file, "concern_attachments");
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("fileUrl", url);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileType", file.getContentType());
            response.put("fileSize", String.valueOf(file.getSize()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Upload failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}