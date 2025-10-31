package com.aurionpro.payroll.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.SalaryPayment;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.EmployeeRepo;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.SalaryPaymentRepo;

@Service
public class ExcelGenerationServiceImpl implements ExcelGenerationService {
    
    @Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private OrganizationRepo organizationRepo;
    
    @Autowired
    private SalaryPaymentRepo salaryPaymentRepo;
    
    
    
    
 // ==========================================
 // STYLING HELPER METHODS
 // ==========================================

 /**
  * Create title style - Large, bold, centered
  */
 private CellStyle createTitleStyle(Workbook workbook) {
     CellStyle style = workbook.createCellStyle();
     Font font = workbook.createFont();
     font.setFontName("Arial");
     font.setFontHeightInPoints((short) 16);
     font.setBold(true);
     font.setColor(IndexedColors.WHITE.getIndex());
     style.setFont(font);
     
     // Background color - Dark Blue
     style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
     style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
     
     // Alignment
     style.setAlignment(HorizontalAlignment.CENTER);
     style.setVerticalAlignment(VerticalAlignment.CENTER);
     
     // Borders
     style.setBorderTop(BorderStyle.THIN);
     style.setBorderBottom(BorderStyle.THIN);
     style.setBorderLeft(BorderStyle.THIN);
     style.setBorderRight(BorderStyle.THIN);
     
     return style;
 }

 /**
  * Create header style - Bold, colored background
  */
 private CellStyle createHeaderStyle(Workbook workbook) {
     CellStyle style = workbook.createCellStyle();
     Font font = workbook.createFont();
     font.setFontName("Arial");
     font.setFontHeightInPoints((short) 11);
     font.setBold(true);
     font.setColor(IndexedColors.WHITE.getIndex());
     style.setFont(font);
     
     // Background color - Blue-Grey
     style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
     style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
     
     // Alignment
     style.setAlignment(HorizontalAlignment.CENTER);
     style.setVerticalAlignment(VerticalAlignment.CENTER);
     
     // Borders
     style.setBorderTop(BorderStyle.MEDIUM);
     style.setBorderBottom(BorderStyle.MEDIUM);
     style.setBorderLeft(BorderStyle.THIN);
     style.setBorderRight(BorderStyle.THIN);
     
     return style;
 }

 /**
  * Create data style - Normal text
  */
 private CellStyle createDataStyle(Workbook workbook) {
     CellStyle style = workbook.createCellStyle();
     Font font = workbook.createFont();
     font.setFontName("Arial");
     font.setFontHeightInPoints((short) 10);
     style.setFont(font);
     
     // Alignment
     style.setAlignment(HorizontalAlignment.LEFT);
     style.setVerticalAlignment(VerticalAlignment.CENTER);
     
     // Borders
     style.setBorderTop(BorderStyle.THIN);
     style.setBorderBottom(BorderStyle.THIN);
     style.setBorderLeft(BorderStyle.THIN);
     style.setBorderRight(BorderStyle.THIN);
     
     // ✅ FIXED: Correct method names
     style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     
     return style;
 }

 /**
  * Create currency style - For money values
  */
 private CellStyle createCurrencyStyle(Workbook workbook) {
     CellStyle style = workbook.createCellStyle();
     Font font = workbook.createFont();
     font.setFontName("Arial");
     font.setFontHeightInPoints((short) 10);
     style.setFont(font);
     
     // Number format for currency
     style.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));
     
     // Alignment
     style.setAlignment(HorizontalAlignment.RIGHT);
     style.setVerticalAlignment(VerticalAlignment.CENTER);
     
     // Borders
     style.setBorderTop(BorderStyle.THIN);
     style.setBorderBottom(BorderStyle.THIN);
     style.setBorderLeft(BorderStyle.THIN);
     style.setBorderRight(BorderStyle.THIN);
     
     // ✅ FIXED: Correct method names
     style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     
     return style;
 }

 /**
  * Create alternating row style - Light grey background
  */
 private CellStyle createAlternateRowStyle(Workbook workbook) {
     CellStyle style = workbook.createCellStyle();
     Font font = workbook.createFont();
     font.setFontName("Arial");
     font.setFontHeightInPoints((short) 10);
     style.setFont(font);
     
     // Light grey background
     style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
     style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
     
     // Alignment
     style.setAlignment(HorizontalAlignment.LEFT);
     style.setVerticalAlignment(VerticalAlignment.CENTER);
     
     // Borders
     style.setBorderTop(BorderStyle.THIN);
     style.setBorderBottom(BorderStyle.THIN);
     style.setBorderLeft(BorderStyle.THIN);
     style.setBorderRight(BorderStyle.THIN);
     
     //Border colors for consistency
     style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     
     
     return style;
 }
 
 /**
  * Create alternating row style for currency cells - Light grey background with currency format
  */
 private CellStyle createAlternateCurrencyStyle(Workbook workbook) {
     CellStyle style = workbook.createCellStyle();
     Font font = workbook.createFont();
     font.setFontName("Arial");
     font.setFontHeightInPoints((short) 10);
     style.setFont(font);
     
     // Number format for currency
     style.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));
     
     // ✅ LIGHTER grey background
     style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
     style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
     
     // Alignment
     style.setAlignment(HorizontalAlignment.RIGHT);
     style.setVerticalAlignment(VerticalAlignment.CENTER);
     
     // Borders
     style.setBorderTop(BorderStyle.THIN);
     style.setBorderBottom(BorderStyle.THIN);
     style.setBorderLeft(BorderStyle.THIN);
     style.setBorderRight(BorderStyle.THIN);
     
     // Border colors
     style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
     
     return style;
 }

 /**
  * Create info style - For metadata/footer
  */
 private CellStyle createInfoStyle(Workbook workbook) {
     CellStyle style = workbook.createCellStyle();
     Font font = workbook.createFont();
     font.setFontName("Arial");
     font.setFontHeightInPoints((short) 9);
     font.setItalic(true);
     font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
     style.setFont(font);
     
     style.setAlignment(HorizontalAlignment.LEFT);
     
     return style;
 }

 // ==========================================
 // EMPLOYEE LIST EXCEL
 // ==========================================

 @Override
 public byte[] generateEmployeeListExcel(Long organizationId) {
     try {
         Organization organization = organizationRepo.findById(organizationId)
             .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
         
         List<Employee> employees = employeeRepo.findByOrganization(organization);
         
         //Sort employees by ID in ascending order
         employees.sort((e1, e2) -> Long.compare(e1.getId(), e2.getId()));
         
         Workbook workbook = new XSSFWorkbook();
         Sheet sheet = workbook.createSheet("Employee List");
         
         // Create styles
         CellStyle titleStyle = createTitleStyle(workbook);
         CellStyle headerStyle = createHeaderStyle(workbook);
         CellStyle dataStyle = createDataStyle(workbook);
         CellStyle alternateStyle = createAlternateRowStyle(workbook);
         CellStyle alternateCurrencyStyle = createAlternateCurrencyStyle(workbook);
         CellStyle infoStyle = createInfoStyle(workbook);
         
         int rowNum = 0;
         
         // Title Row
         Row titleRow = sheet.createRow(rowNum++);
         titleRow.setHeightInPoints(30);
         Cell titleCell = titleRow.createCell(0);
         titleCell.setCellValue("EMPLOYEE LIST - " + organization.getName());
         titleCell.setCellStyle(titleStyle);
         sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7)); // Merge across all columns
         
         // Empty row for spacing
         rowNum++;
         
         // Info Row
         Row infoRow = sheet.createRow(rowNum++);
         Cell infoCell = infoRow.createCell(0);
         infoCell.setCellValue("Generated on: " + java.time.LocalDateTime.now().format(
             java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
         infoCell.setCellStyle(infoStyle);
         
         Cell countCell = infoRow.createCell(6);
         countCell.setCellValue("Total Employees: " + employees.size());
         countCell.setCellStyle(infoStyle);
         
         // Empty row for spacing
         rowNum++;
         
         // Header row
         Row headerRow = sheet.createRow(rowNum++);
         headerRow.setHeightInPoints(25);
         String[] headers = {"ID", "Name", "Email", "Phone", "Department", "Designation", "Date of Joining", "Status"};
         
         for (int i = 0; i < headers.length; i++) {
             Cell cell = headerRow.createCell(i);
             cell.setCellValue(headers[i]);
             cell.setCellStyle(headerStyle);
         }
         
         // Data rows
         for (int i = 0; i < employees.size(); i++) {
             Employee employee = employees.get(i);
             Row row = sheet.createRow(rowNum++);
             row.setHeightInPoints(20);
             
             // Alternate row colors
             boolean isAlternate = (i % 2 == 1);
             CellStyle currentStyle = isAlternate ? alternateStyle : dataStyle;
             
             Cell cell0 = row.createCell(0);
             cell0.setCellValue(employee.getId());
             cell0.setCellStyle(currentStyle);
             
             Cell cell1 = row.createCell(1);
             cell1.setCellValue(employee.getName());
             cell1.setCellStyle(currentStyle);
             
             Cell cell2 = row.createCell(2);
             cell2.setCellValue(employee.getEmail());
             cell2.setCellStyle(currentStyle);
             
             Cell cell3 = row.createCell(3);
             cell3.setCellValue(employee.getPhone());
             cell3.setCellStyle(currentStyle);
             
             Cell cell4 = row.createCell(4);
             cell4.setCellValue(employee.getDepartment());
             cell4.setCellStyle(currentStyle);
             
             Cell cell5 = row.createCell(5);
             cell5.setCellValue(employee.getDesignation());
             cell5.setCellStyle(currentStyle);
             
             Cell cell6 = row.createCell(6);
             cell6.setCellValue(employee.getDateOfJoining().toString());
             cell6.setCellStyle(currentStyle);
             
             Cell cell7 = row.createCell(7);
             cell7.setCellValue(employee.getStatus().name());
             cell7.setCellStyle(currentStyle);
         }
         
         // Auto-size columns
         for (int i = 0; i < headers.length; i++) {
             sheet.autoSizeColumn(i);
             sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000); // Add padding
         }
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         workbook.write(out);
         workbook.close();
         
         return out.toByteArray();
         
     } catch (IOException e) {
         throw new BadRequestException("Failed to generate Excel: " + e.getMessage());
     }
 }

 // ==========================================
 // SALARY REPORT EXCEL
 // ==========================================

 @Override
 public byte[] generateSalaryReportExcel(Long organizationId, String month, Integer year) {
     try {
         Organization organization = organizationRepo.findById(organizationId)
             .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
         
         List<SalaryPayment> payments = 
             salaryPaymentRepo.findByOrganizationAndMonthAndYear(organization, month, year);
         
         //Sort payments by ID in ascending order
         payments.sort((p1, p2) -> Long.compare(p1.getEmployee().getId(), p2.getEmployee().getId()));
         
         Workbook workbook = new XSSFWorkbook();
         Sheet sheet = workbook.createSheet("Salary Report");
         
         // Create styles
         CellStyle titleStyle = createTitleStyle(workbook);
         CellStyle headerStyle = createHeaderStyle(workbook);
         CellStyle dataStyle = createDataStyle(workbook);
         CellStyle currencyStyle = createCurrencyStyle(workbook);
         CellStyle alternateStyle = createAlternateRowStyle(workbook);
         CellStyle alternateCurrencyStyle = createAlternateCurrencyStyle(workbook);
         CellStyle infoStyle = createInfoStyle(workbook);
         
         int rowNum = 0;
         
         // Title Row
         Row titleRow = sheet.createRow(rowNum++);
         titleRow.setHeightInPoints(30);
         Cell titleCell = titleRow.createCell(0);
         titleCell.setCellValue("SALARY REPORT - " + month + " " + year);
         titleCell.setCellStyle(titleStyle);
         sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));
         
         // Organization Name
         Row orgRow = sheet.createRow(rowNum++);
         orgRow.setHeightInPoints(20);
         Cell orgCell = orgRow.createCell(0);
         orgCell.setCellValue(organization.getName());
         orgCell.setCellStyle(titleStyle);
         sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));
         
         // Empty row
         rowNum++;
         
         // Summary Info
         Row infoRow = sheet.createRow(rowNum++);
         Cell infoCell1 = infoRow.createCell(0);
         infoCell1.setCellValue("Generated on: " + java.time.LocalDateTime.now().format(
             java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
         infoCell1.setCellStyle(infoStyle);
         
         Cell infoCell2 = infoRow.createCell(8);
         infoCell2.setCellValue("Total Employees: " + payments.size());
         infoCell2.setCellStyle(infoStyle);
         
         // Calculate total
         double totalNetSalary = payments.stream()
             .mapToDouble(p -> p.getNetSalary().doubleValue())
             .sum();
         
         Cell infoCell3 = infoRow.createCell(10);
         infoCell3.setCellValue("Total Payout: ₹" + String.format("%,.2f", totalNetSalary));
         infoCell3.setCellStyle(infoStyle);
         
         // Empty row
         rowNum++;
         
         // Header row
         Row headerRow = sheet.createRow(rowNum++);
         headerRow.setHeightInPoints(25);
         String[] headers = {"Emp ID", "Employee Name", "Department", "Basic Salary", 
             "HRA", "DA", "Other Allow.", "Gross Salary", "PF", "Net Salary", "Payment Date", "Transaction ID"};
         
         for (int i = 0; i < headers.length; i++) {
             Cell cell = headerRow.createCell(i);
             cell.setCellValue(headers[i]);
             cell.setCellStyle(headerStyle);
         }
         
         // Data rows
         for (int i = 0; i < payments.size(); i++) {
             SalaryPayment payment = payments.get(i);
             Row row = sheet.createRow(rowNum++);
             row.setHeightInPoints(20);
             
             // ✅ Determine if this is an alternate (grey) row
             boolean isAlternate = (i % 2 == 1);
             
             // ✅ Choose the correct styles for text and currency cells
             CellStyle textStyle = isAlternate ? alternateStyle : dataStyle;
             CellStyle moneyStyle = isAlternate ? alternateCurrencyStyle : currencyStyle;
             
             // ID (text)
             Cell cell0 = row.createCell(0);
             cell0.setCellValue(payment.getEmployee().getId());
             cell0.setCellStyle(textStyle);
             
             // Name (text)
             Cell cell1 = row.createCell(1);
             cell1.setCellValue(payment.getEmployee().getName());
             cell1.setCellStyle(textStyle);
             
             // Department (text)
             Cell cell2 = row.createCell(2);
             cell2.setCellValue(payment.getEmployee().getDepartment());
             cell2.setCellStyle(textStyle);
             
             // ✅ Basic Salary (currency with alternating style)
             Cell cell3 = row.createCell(3);
             cell3.setCellValue(payment.getBasicSalary().doubleValue());
             cell3.setCellStyle(moneyStyle);
             
             // ✅ HRA (currency with alternating style)
             Cell cell4 = row.createCell(4);
             cell4.setCellValue(payment.getHra().doubleValue());
             cell4.setCellStyle(moneyStyle);
             
             // ✅ DA (currency with alternating style)
             Cell cell5 = row.createCell(5);
             cell5.setCellValue(payment.getDearnessAllowance().doubleValue());
             cell5.setCellStyle(moneyStyle);
             
             // ✅ Other Allowances (currency with alternating style)
             Cell cell6 = row.createCell(6);
             cell6.setCellValue(payment.getOtherAllowances().doubleValue());
             cell6.setCellStyle(moneyStyle);
             
             // ✅ Gross Salary (currency with alternating style)
             Cell cell7 = row.createCell(7);
             cell7.setCellValue(payment.getGrossSalary().doubleValue());
             cell7.setCellStyle(moneyStyle);
             
             // ✅ PF (currency with alternating style)
             Cell cell8 = row.createCell(8);
             cell8.setCellValue(payment.getProvidentFund().doubleValue());
             cell8.setCellStyle(moneyStyle);
             
             // ✅ Net Salary (currency with alternating style)
             Cell cell9 = row.createCell(9);
             cell9.setCellValue(payment.getNetSalary().doubleValue());
             cell9.setCellStyle(moneyStyle);
             
             // Payment Date (text)
             Cell cell10 = row.createCell(10);
             cell10.setCellValue(payment.getPaymentDate().toString());
             cell10.setCellStyle(textStyle);
             
             // Transaction ID (text)
             Cell cell11 = row.createCell(11);
             cell11.setCellValue(payment.getTransactionId());
             cell11.setCellStyle(textStyle);
         }
         
         // Auto-size columns
         for (int i = 0; i < headers.length; i++) {
             sheet.autoSizeColumn(i);
             sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
         }
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         workbook.write(out);
         workbook.close();
         
         return out.toByteArray();
         
     } catch (IOException e) {
         throw new BadRequestException("Failed to generate Excel: " + e.getMessage());
     }
 }
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//    @Override
//    public byte[] generateEmployeeListExcel(Long organizationId) {
//        try {
//            Organization organization = organizationRepo.findById(organizationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
//            
//            List<Employee> employees = employeeRepo.findByOrganization(organization);
//            
//            Workbook workbook = new XSSFWorkbook();
//            Sheet sheet = workbook.createSheet("Employees");
//            
//            // Header style
//            CellStyle headerStyle = workbook.createCellStyle();
//            Font headerFont = workbook.createFont();
//            headerFont.setBold(true);
//            headerStyle.setFont(headerFont);
//            
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            String[] headers = {"ID", "Name", "Email", "Phone", "Department", "Designation", "Date of Joining", "Status"};
//            
//            for (int i = 0; i < headers.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headers[i]);
//                cell.setCellStyle(headerStyle);
//            }
//            
//            // Data rows
//            int rowNum = 1;
//            for (Employee employee : employees) {
//                Row row = sheet.createRow(rowNum++);
//                
//                row.createCell(0).setCellValue(employee.getId());
//                row.createCell(1).setCellValue(employee.getName());
//                row.createCell(2).setCellValue(employee.getEmail());
//                row.createCell(3).setCellValue(employee.getPhone());
//                row.createCell(4).setCellValue(employee.getDepartment());
//                row.createCell(5).setCellValue(employee.getDesignation());
//                row.createCell(6).setCellValue(employee.getDateOfJoining().toString());
//                row.createCell(7).setCellValue(employee.getStatus().name());
//            }
//            
//            // Auto-size columns
//            for (int i = 0; i < headers.length; i++) {
//                sheet.autoSizeColumn(i);
//            }
//            
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            workbook.write(out);
//            workbook.close();
//            
//            return out.toByteArray();
//            
//        } catch (IOException e) {
//            throw new BadRequestException("Failed to generate Excel: " + e.getMessage());
//        }
//    }
//    
//    @Override
//    public byte[] generateSalaryReportExcel(Long organizationId, String month, Integer year) {
//        try {
//            Organization organization = organizationRepo.findById(organizationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
//            
//            List<SalaryPayment> payments = 
//                salaryPaymentRepo.findByOrganizationAndMonthAndYear(organization, month, year);
//            
//            Workbook workbook = new XSSFWorkbook();
//            Sheet sheet = workbook.createSheet("Salary Report - " + month + " " + year);
//            
//            // Header style
//            CellStyle headerStyle = workbook.createCellStyle();
//            Font headerFont = workbook.createFont();
//            headerFont.setBold(true);
//            headerStyle.setFont(headerFont);
//            
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            String[] headers = {"Employee ID", "Employee Name", "Department", "Basic Salary", 
//                "HRA", "DA", "Other Allowances", "Gross Salary", "PF", "Net Salary", "Payment Date", "Transaction ID"};
//            
//            for (int i = 0; i < headers.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headers[i]);
//                cell.setCellStyle(headerStyle);
//            }
//            
//            // Data rows
//            int rowNum = 1;
//            for (SalaryPayment payment : payments) {
//                Row row = sheet.createRow(rowNum++);
//                
//                row.createCell(0).setCellValue(payment.getEmployee().getId());
//                row.createCell(1).setCellValue(payment.getEmployee().getName());
//                row.createCell(2).setCellValue(payment.getEmployee().getDepartment());
//                row.createCell(3).setCellValue(payment.getBasicSalary().doubleValue());
//                row.createCell(4).setCellValue(payment.getHra().doubleValue());
//                row.createCell(5).setCellValue(payment.getDearnessAllowance().doubleValue());
//                row.createCell(6).setCellValue(payment.getOtherAllowances().doubleValue());
//                row.createCell(7).setCellValue(payment.getGrossSalary().doubleValue());
//                row.createCell(8).setCellValue(payment.getProvidentFund().doubleValue());
//                row.createCell(9).setCellValue(payment.getNetSalary().doubleValue());
//                row.createCell(10).setCellValue(payment.getPaymentDate().toString());
//                row.createCell(11).setCellValue(payment.getTransactionId());
//            }
//            
//            // Auto-size columns
//            for (int i = 0; i < headers.length; i++) {
//                sheet.autoSizeColumn(i);
//            }
//            
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            workbook.write(out);
//            workbook.close();
//            
//            return out.toByteArray();
//            
//        } catch (IOException e) {
//            throw new BadRequestException("Failed to generate Excel: " + e.getMessage());
//        }
//    }
//}