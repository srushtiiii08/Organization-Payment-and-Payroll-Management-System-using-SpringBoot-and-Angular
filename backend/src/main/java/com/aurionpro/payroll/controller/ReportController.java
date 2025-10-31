package com.aurionpro.payroll.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.payroll.dto.response.OrganizationResponse;
import com.aurionpro.payroll.security.CustomUserDetailsService;
import com.aurionpro.payroll.service.CloudinaryService;
import com.aurionpro.payroll.service.ExcelGenerationService;
import com.aurionpro.payroll.service.OrganizationService;
import com.aurionpro.payroll.service.PdfGenerationService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ORGANIZATION')")
@Transactional
public class ReportController {
    
    @Autowired
    private ExcelGenerationService excelGenerationService;

    @Autowired
    private PdfGenerationService pdfGenerationService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    
    //EMPLOYEE LIST REPORTS
    
    //direct download emp list as excel
    @GetMapping("/employees/excel/download")
    public ResponseEntity<byte[]> downloadEmployeeListExcel(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        System.out.println("📊 Generating employee list Excel for: " + org.getName());
        
        byte[] excelBytes = excelGenerationService.generateEmployeeListExcel(org.getId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "employee_list.xlsx");
        
        System.out.println("✅ Employee list Excel generated successfully");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelBytes);
    }
    
    /**
     * Generate Employee List Excel and Upload to Cloudinary - Returns URL
     */
    @GetMapping("/employees/excel")
    public ResponseEntity<Map<String, String>> generateEmployeeListExcel(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        System.out.println("═══════════════════════════════════════");
        System.out.println("📊 GENERATING EMPLOYEE LIST EXCEL");
        System.out.println("═══════════════════════════════════════");
        System.out.println("🏢 Organization: " + org.getName());
        
        // Generate Excel
        byte[] excelBytes = excelGenerationService.generateEmployeeListExcel(org.getId());
        
        // Create filename
        String fileName = String.format("employee_list_%s_%s",
            org.getName().replace(" ", "_"),
            java.time.LocalDate.now()
        );
        
        // Upload to Cloudinary
        String excelUrl = cloudinaryService.uploadExcel(excelBytes, fileName, "reports/employee-lists");
        
        System.out.println("✅ Excel generated and uploaded");
        System.out.println("🔗 URL: " + excelUrl);
        System.out.println("═══════════════════════════════════════");
        
        return ResponseEntity.ok(Map.of(
            "message", "Employee list generated successfully",
            "reportUrl", excelUrl,
            "fileName", fileName + ".xlsx",
            "type", "EXCEL",
            "reportType", "EMPLOYEE_LIST"
        ));
    }
    
    
    
 // SALARY REPORT - EXCEL
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Download Salary Report as Excel - Direct Download
     */
    @GetMapping("/salary-report/excel/download")
    public ResponseEntity<byte[]> downloadSalaryReportExcel(
            @RequestParam String month,
            @RequestParam Integer year,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        System.out.println("📊 Generating salary report Excel: " + month + " " + year);
        
        byte[] excelBytes = excelGenerationService.generateSalaryReportExcel(org.getId(), month, year);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "salary_report_" + month + "_" + year + ".xlsx");
        
        System.out.println("✅ Salary report Excel generated successfully");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelBytes);
    }
    
    /**
     * Generate Salary Report Excel and Upload to Cloudinary - Returns URL
     */
    @GetMapping("/salary-report/excel")
    public ResponseEntity<Map<String, String>> generateSalaryReportExcel(
            @RequestParam String month,
            @RequestParam Integer year,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        System.out.println("═══════════════════════════════════════");
        System.out.println("📊 GENERATING SALARY REPORT EXCEL");
        System.out.println("═══════════════════════════════════════");
        System.out.println("🏢 Organization: " + org.getName());
        System.out.println("📅 Period: " + month + " " + year);
        
        // Generate Excel
        byte[] excelBytes = excelGenerationService.generateSalaryReportExcel(org.getId(), month, year);
        
        // Create filename
        String fileName = String.format("salary_report_%s_%s_%d",
            org.getName().replace(" ", "_"),
            month,
            year
        );
        
        // Upload to Cloudinary
        String excelUrl = cloudinaryService.uploadExcel(excelBytes, fileName, "reports/salary-reports");
        
        System.out.println("✅ Excel generated and uploaded");
        System.out.println("🔗 URL: " + excelUrl);
        System.out.println("═══════════════════════════════════════");
        
        return ResponseEntity.ok(Map.of(
            "message", "Salary report generated successfully",
            "reportUrl", excelUrl,
            "fileName", fileName + ".xlsx",
            "type", "EXCEL",
            "reportType", "SALARY_REPORT",
            "month", month,
            "year", year.toString()
        ));
    }
    
    
    
    
 // SALARY REPORT - PDF
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Download Salary Report as PDF - Direct Download
     */
    @GetMapping("/salary-report/pdf/download")
    public ResponseEntity<byte[]> downloadSalaryReportPdf(
            @RequestParam String month,
            @RequestParam Integer year,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        System.out.println("📄 Generating salary report PDF: " + month + " " + year);
        
        byte[] pdfBytes = pdfGenerationService.generateSalaryReportPdf(org.getId(), month, year);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "salary_report_" + month + "_" + year + ".pdf");
        
        System.out.println("✅ Salary report PDF generated successfully");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
    
    /**
     * Generate Salary Report PDF and Upload to Cloudinary - Returns URL
     */
    @GetMapping("/salary-report/pdf")
    public ResponseEntity<Map<String, String>> generateSalaryReportPdf(
            @RequestParam String month,
            @RequestParam Integer year,
            Authentication authentication) {
        
        String email = authentication.getName();
        Long userId = userDetailsService.loadUserEntityByEmail(email).getId();
        OrganizationResponse org = organizationService.getOrganizationByUserId(userId);
        
        System.out.println("═══════════════════════════════════════");
        System.out.println("📄 GENERATING SALARY REPORT PDF");
        System.out.println("═══════════════════════════════════════");
        System.out.println("🏢 Organization: " + org.getName());
        System.out.println("📅 Period: " + month + " " + year);
        
        // Generate PDF
        byte[] pdfBytes = pdfGenerationService.generateSalaryReportPdf(org.getId(), month, year);
        
        // Create filename
        String fileName = String.format("salary_report_%s_%s_%d",
            org.getName().replace(" ", "_"),
            month,
            year
        );
        
        // Upload to Cloudinary
        String pdfUrl = cloudinaryService.uploadPdf(pdfBytes, fileName, "reports/salary-reports");
        
        System.out.println("✅ PDF generated and uploaded");
        System.out.println("🔗 URL: " + pdfUrl);
        System.out.println("═══════════════════════════════════════");
        
        return ResponseEntity.ok(Map.of(
            "message", "Salary report generated successfully",
            "reportUrl", pdfUrl,
            "fileName", fileName + ".pdf",
            "type", "PDF",
            "reportType", "SALARY_REPORT",
            "month", month,
            "year", year.toString()
        ));
    }
    
    
    
    
 
}