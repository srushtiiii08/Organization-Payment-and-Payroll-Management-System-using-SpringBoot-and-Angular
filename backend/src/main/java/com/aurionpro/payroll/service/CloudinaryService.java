package com.aurionpro.payroll.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    
    String uploadFile(MultipartFile file, String folder);

    //Upload PDF from byte array
    String uploadPdf(byte[] pdfBytes, String fileName, String folder);
    
    //Upload Excel from byte array
    String uploadExcel(byte[] excelBytes, String fileName, String folder);
    
    
    void deleteFile(String publicId);
}