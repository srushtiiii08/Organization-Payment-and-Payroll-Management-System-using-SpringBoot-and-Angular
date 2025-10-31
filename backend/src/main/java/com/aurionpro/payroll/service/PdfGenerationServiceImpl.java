package com.aurionpro.payroll.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.SalaryPayment;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.SalaryPaymentRepo;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {
    
	 @Autowired
	 private OrganizationRepo organizationRepo;
	    
	 @Autowired
	 private SalaryPaymentRepo salaryPaymentRepo;
	 
	 

	// ==========================================
	    // COLOR SCHEME
	    // ==========================================
	    private static final BaseColor HEADER_BG = new BaseColor(41, 128, 185);      // Professional Blue
	    private static final BaseColor HEADER_TEXT = BaseColor.WHITE;
	    private static final BaseColor ALTERNATE_ROW = new BaseColor(236, 240, 241);  // Light Grey
	    private static final BaseColor BORDER_COLOR = new BaseColor(189, 195, 199);   // Grey Border
	    private static final BaseColor TOTAL_BG = new BaseColor(52, 152, 219);        // Lighter Blue
	    
	    // ==========================================
	    // FONT DEFINITIONS
	    // ==========================================
	    private Font getTitleFont() {
	        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.WHITE);
	    }
	    
	    private Font getSubtitleFont() {
	        return FontFactory.getFont(FontFactory.HELVETICA, 12, new BaseColor(52, 73, 94));
	    }
	    
	    private Font getHeaderFont() {
	        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, HEADER_TEXT);
	    }
	    
	    private Font getDataFont() {
	        return FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
	    }
	    
	    private Font getTotalFont() {
	        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
	    }
	    
	    private Font getFooterFont() {
	        return FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
	    }
	    
	    // ==========================================
	    // HELPER METHODS
	    // ==========================================
	    
	    /**
	     * Create styled table cell with custom formatting
	     */
	    private PdfPCell createStyledCell(String content, Font font, BaseColor bgColor, 
	                                     int horizontalAlignment, int padding) {
	        PdfPCell cell = new PdfPCell(new Phrase(content, font));
	        cell.setBackgroundColor(bgColor);
	        cell.setHorizontalAlignment(horizontalAlignment);
	        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        cell.setPadding(padding);
	        cell.setBorderColor(BORDER_COLOR);
	        return cell;
	    }
	    
	    /**
	     * Create header cell
	     */
	    private PdfPCell createHeaderCell(String content) {
	        return createStyledCell(content, getHeaderFont(), HEADER_BG, 
	                              Element.ALIGN_CENTER, 8);
	    }
	    
	    /**
	     * Create data cell
	     */
	    private PdfPCell createDataCell(String content, boolean isAlternate) {
	        BaseColor bgColor = isAlternate ? ALTERNATE_ROW : BaseColor.WHITE;
	        return createStyledCell(content, getDataFont(), bgColor, 
	                              Element.ALIGN_LEFT, 6);
	    }
	    
	    /**
	     * Create currency cell (right-aligned)
	     */
	    private PdfPCell createCurrencyCell(String content, boolean isAlternate) {
	        BaseColor bgColor = isAlternate ? ALTERNATE_ROW : BaseColor.WHITE;
	        return createStyledCell(content, getDataFont(), bgColor, 
	                              Element.ALIGN_RIGHT, 6);
	    }
	    
	    /**
	     * Add title section to document
	     */
	    private void addTitleSection(Document document, String title, String subtitle) throws DocumentException {
	        // Title with colored background
	        PdfPTable titleTable = new PdfPTable(1);
	        titleTable.setWidthPercentage(100);
	        titleTable.setSpacingAfter(20);
	        
	        PdfPCell titleCell = new PdfPCell(new Phrase(title, getTitleFont()));
	        titleCell.setBackgroundColor(HEADER_BG);
	        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	        titleCell.setPadding(15);
	        titleCell.setBorder(Rectangle.NO_BORDER);
	        titleTable.addCell(titleCell);
	        
	        document.add(titleTable);
	        
	        // Subtitle
	        if (subtitle != null && !subtitle.isEmpty()) {
	            Paragraph subtitlePara = new Paragraph(subtitle, getSubtitleFont());
	            subtitlePara.setAlignment(Element.ALIGN_CENTER);
	            subtitlePara.setSpacingAfter(15);
	            document.add(subtitlePara);
	        }
	    }
	    
	    // ==========================================
	    // SALARY SLIP GENERATION
	    // ==========================================
	    
	    @Override
	    public byte[] generateSalarySlip(SalaryPayment salaryPayment) {
	        try {
	            // Force load employee and organization
	            Employee employee = salaryPayment.getEmployee();
	            if (employee == null) {
	                throw new BadRequestException("Employee not found in salary payment");
	            }
	            
	            String employeeName = employee.getName();
	            String employeeDept = employee.getDepartment();
	            String employeeDesig = employee.getDesignation();
	            Long employeeId = employee.getId();
	            
	            Organization organization = employee.getOrganization();
	            if (organization == null) {
	                throw new BadRequestException("Organization not found for employee");
	            }
	            String organizationName = organization.getName();
	            
	            ByteArrayOutputStream out = new ByteArrayOutputStream();
	            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
	            PdfWriter.getInstance(document, out);
	            
	            document.open();
	            
	            // Title Section
	            addTitleSection(document, "SALARY SLIP", organizationName);
	            
	            // Period
	            Paragraph period = new Paragraph(
	                "For the month of " + salaryPayment.getMonth() + " " + salaryPayment.getYear(),
	                getSubtitleFont()
	            );
	            period.setAlignment(Element.ALIGN_CENTER);
	            period.setSpacingAfter(20);
	            document.add(period);
	            
	            // Employee Details Table
	            PdfPTable empTable = new PdfPTable(2);
	            empTable.setWidthPercentage(100);
	            empTable.setWidths(new float[]{1, 2});
	            empTable.setSpacingBefore(10);
	            empTable.setSpacingAfter(20);
	            
	            empTable.addCell(createHeaderCell("Employee Name:"));
	            empTable.addCell(createDataCell(employeeName, false));
	            
	            empTable.addCell(createHeaderCell("Employee ID:"));
	            empTable.addCell(createDataCell(String.valueOf(employeeId), true));
	            
	            empTable.addCell(createHeaderCell("Department:"));
	            empTable.addCell(createDataCell(employeeDept, false));
	            
	            empTable.addCell(createHeaderCell("Designation:"));
	            empTable.addCell(createDataCell(employeeDesig, true));
	            
	            empTable.addCell(createHeaderCell("Payment Date:"));
	            empTable.addCell(createDataCell(
	                salaryPayment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
	                false
	            ));
	            
	            document.add(empTable);
	            
	            // Earnings Table
	            PdfPTable earningsTable = new PdfPTable(2);
	            earningsTable.setWidthPercentage(100);
	            earningsTable.setSpacingBefore(15);
	            
	            PdfPCell earningsHeader = createHeaderCell("EARNINGS");
	            earningsHeader.setColspan(2);
	            earningsTable.addCell(earningsHeader);
	            
	            earningsTable.addCell(createDataCell("Basic Salary", false));
	            earningsTable.addCell(createCurrencyCell("₹ " + salaryPayment.getBasicSalary(), false));
	            
	            earningsTable.addCell(createDataCell("HRA", true));
	            earningsTable.addCell(createCurrencyCell("₹ " + salaryPayment.getHra(), true));
	            
	            earningsTable.addCell(createDataCell("Dearness Allowance", false));
	            earningsTable.addCell(createCurrencyCell("₹ " + salaryPayment.getDearnessAllowance(), false));
	            
	            earningsTable.addCell(createDataCell("Other Allowances", true));
	            earningsTable.addCell(createCurrencyCell("₹ " + salaryPayment.getOtherAllowances(), true));
	            
	            PdfPCell grossCell1 = createStyledCell("Gross Salary", getTotalFont(), TOTAL_BG, Element.ALIGN_LEFT, 8);
	            PdfPCell grossCell2 = createStyledCell("₹ " + salaryPayment.getGrossSalary(), getTotalFont(), TOTAL_BG, Element.ALIGN_RIGHT, 8);
	            earningsTable.addCell(grossCell1);
	            earningsTable.addCell(grossCell2);
	            
	            document.add(earningsTable);
	            
	            // Deductions Table
	            PdfPTable deductionsTable = new PdfPTable(2);
	            deductionsTable.setWidthPercentage(100);
	            deductionsTable.setSpacingBefore(15);
	            
	            PdfPCell deductionsHeader = createHeaderCell("DEDUCTIONS");
	            deductionsHeader.setColspan(2);
	            deductionsTable.addCell(deductionsHeader);
	            
	            deductionsTable.addCell(createDataCell("Provident Fund", false));
	            deductionsTable.addCell(createCurrencyCell("₹ " + salaryPayment.getProvidentFund(), false));
	            
	            document.add(deductionsTable);
	            
	            // Net Salary
	            PdfPTable netTable = new PdfPTable(2);
	            netTable.setWidthPercentage(100);
	            netTable.setSpacingBefore(15);
	            
	            PdfPCell netCell1 = createStyledCell("NET SALARY", getTotalFont(), new BaseColor(39, 174, 96), Element.ALIGN_LEFT, 10);
	            PdfPCell netCell2 = createStyledCell("₹ " + salaryPayment.getNetSalary(), getTotalFont(), new BaseColor(39, 174, 96), Element.ALIGN_RIGHT, 10);
	            netTable.addCell(netCell1);
	            netTable.addCell(netCell2);
	            
	            document.add(netTable);
	            
	            // Transaction Details
	            Paragraph txnDetails = new Paragraph(
	                "\nTransaction ID: " + salaryPayment.getTransactionId(),
	                getFooterFont()
	            );
	            txnDetails.setSpacingBefore(20);
	            document.add(txnDetails);
	            
	            // Footer
	            Paragraph footer = new Paragraph(
	                "\nThis is a system-generated document. No signature required.",
	                getFooterFont()
	            );
	            footer.setAlignment(Element.ALIGN_CENTER);
	            footer.setSpacingBefore(30);
	            document.add(footer);
	            
	            document.close();
	            
	            return out.toByteArray();
	            
	        } catch (Exception e) {
	            throw new BadRequestException("Failed to generate PDF: " + e.getMessage());
	        }
	    }
	    
	    // ==========================================
	    // SALARY REPORT GENERATION
	    // ==========================================
	    
	    @Override
	    public byte[] generateSalaryReportPdf(Long organizationId, String month, Integer year) {
	        try {
	            Organization organization = organizationRepo.findById(organizationId)
	                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
	            
	            List<SalaryPayment> payments = 
	                salaryPaymentRepo.findByOrganizationAndMonthAndYear(organization, month, year);
	            
	            ByteArrayOutputStream out = new ByteArrayOutputStream();
	            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30); // Landscape
	            PdfWriter.getInstance(document, out);
	            
	            document.open();
	            
	            // Title Section
	            addTitleSection(document, "SALARY PAYMENT REPORT", organization.getName());
	            
	            // Period
	            Paragraph period = new Paragraph(
	                "For the month of " + month + " " + year,
	                getSubtitleFont()
	            );
	            period.setAlignment(Element.ALIGN_CENTER);
	            period.setSpacingAfter(15);
	            document.add(period);
	            
	            // Summary Table
	            PdfPTable summaryTable = new PdfPTable(2);
	            summaryTable.setWidthPercentage(50);
	            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
	            summaryTable.setSpacingAfter(20);
	            
	            summaryTable.addCell(createHeaderCell("Total Employees:"));
	            summaryTable.addCell(createDataCell(String.valueOf(payments.size()), false));
	            
	            double totalAmount = payments.stream()
	                .mapToDouble(p -> p.getNetSalary().doubleValue())
	                .sum();
	            
	            summaryTable.addCell(createHeaderCell("Total Amount Paid:"));
	            summaryTable.addCell(createCurrencyCell("₹ " + String.format("%,.2f", totalAmount), false));
	            
	            document.add(summaryTable);
	            
	            // Payment Details Table
	            PdfPTable table = new PdfPTable(7);
	            table.setWidthPercentage(100);
	            table.setWidths(new int[]{1, 3, 2, 2, 2, 2, 2});
	            
	            // Headers
	            String[] headers = {"Emp ID", "Name", "Dept", "Gross", "Deductions", "Net Salary", "Status"};
	            for (String header : headers) {
	                table.addCell(createHeaderCell(header));
	            }
	            
	            // Data rows
	            for (int i = 0; i < payments.size(); i++) {
	                SalaryPayment payment = payments.get(i);
	                Employee emp = payment.getEmployee();
	                boolean isAlternate = (i % 2 == 1);
	                
	                table.addCell(createDataCell(String.valueOf(emp.getId()), isAlternate));
	                table.addCell(createDataCell(emp.getName(), isAlternate));
	                table.addCell(createDataCell(emp.getDepartment(), isAlternate));
	                table.addCell(createCurrencyCell("₹ " + payment.getGrossSalary(), isAlternate));
	                table.addCell(createCurrencyCell("₹ " + payment.getProvidentFund(), isAlternate));
	                table.addCell(createCurrencyCell("₹ " + payment.getNetSalary(), isAlternate));
	                table.addCell(createDataCell(payment.getStatus().name(), isAlternate));
	            }
	            
	            document.add(table);
	            
	            // Footer
	            Paragraph footer = new Paragraph(
	                "\nGenerated on: " + java.time.LocalDateTime.now().format(
	                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
	                getFooterFont()
	            );
	            footer.setAlignment(Element.ALIGN_RIGHT);
	            footer.setSpacingBefore(20);
	            document.add(footer);
	            
	            document.close();
	            
	            return out.toByteArray();
	            
	        } catch (DocumentException e) {
	            throw new BadRequestException("Failed to generate PDF report: " + e.getMessage());
	        }
	    }
}
	 
	 
	 
	 
	 
//	    @Override
//	    public byte[] generateSalarySlip(SalaryPayment salaryPayment) {
//	        try {
//	            System.out.println("═══════════════════════════════════════");
//	            System.out.println("📄 GENERATING SALARY SLIP PDF");
//	            System.out.println("═══════════════════════════════════════");
//	            
//	            // ⭐ EAGERLY FETCH EMPLOYEE AND ORGANIZATION
//	            Employee employee = salaryPayment.getEmployee();
//	            if (employee == null) {
//	                throw new BadRequestException("Employee not found in salary payment");
//	            }
//	            
//	            // ⭐ FORCE LOAD EMPLOYEE DATA
//	            String employeeName = employee.getName();
//	            String employeeDept = employee.getDepartment();
//	            String employeeDesig = employee.getDesignation();
//	            Long employeeId = employee.getId();
//	            
//	            System.out.println("Employee: " + employeeName);
//	            System.out.println("Department: " + employeeDept);
//	            System.out.println("Designation: " + employeeDesig);
//	            
//	            // ⭐ FORCE LOAD ORGANIZATION
//	            Organization organization = employee.getOrganization();
//	            if (organization == null) {
//	                throw new BadRequestException("Organization not found for employee");
//	            }
//	            String organizationName = organization.getName();
//	            System.out.println("Organization: " + organizationName);
//	            
//	            // ⭐ LOG SALARY DETAILS
//	            System.out.println("Basic Salary: ₹" + salaryPayment.getBasicSalary());
//	            System.out.println("HRA: ₹" + salaryPayment.getHra());
//	            System.out.println("DA: ₹" + salaryPayment.getDearnessAllowance());
//	            System.out.println("Other Allowances: ₹" + salaryPayment.getOtherAllowances());
//	            System.out.println("Gross Salary: ₹" + salaryPayment.getGrossSalary());
//	            System.out.println("PF: ₹" + salaryPayment.getProvidentFund());
//	            System.out.println("Net Salary: ₹" + salaryPayment.getNetSalary());
//	            
//	            ByteArrayOutputStream out = new ByteArrayOutputStream();
//	            Document document = new Document();
//	            PdfWriter.getInstance(document, out);
//	            
//	            document.open();
//	            System.out.println("✅ PDF document opened");
//	            
//	            // Title
//	            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
//	            Paragraph title = new Paragraph("SALARY SLIP", titleFont);
//	            title.setAlignment(Element.ALIGN_CENTER);
//	            title.setSpacingAfter(20);
//	            document.add(title);
//	            
//	            // Organization Details
//	            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
//	            Paragraph orgName = new Paragraph(organizationName, headerFont);
//	            orgName.setAlignment(Element.ALIGN_CENTER);
//	            document.add(orgName);
//	            
//	            Paragraph period = new Paragraph("For the month of " + salaryPayment.getMonth() + " " + salaryPayment.getYear());
//	            period.setAlignment(Element.ALIGN_CENTER);
//	            period.setSpacingAfter(20);
//	            document.add(period);
//	            
//	            // Employee Details Table
//	            PdfPTable empTable = new PdfPTable(2);
//	            empTable.setWidthPercentage(100);
//	            empTable.setSpacingBefore(10);
//	            empTable.setSpacingAfter(10);
//	            
//	            addCell(empTable, "Employee Name:", headerFont);
//	            addCell(empTable, employeeName, FontFactory.getFont(FontFactory.HELVETICA, 12));
//	            
//	            addCell(empTable, "Employee ID:", headerFont);
//	            addCell(empTable, String.valueOf(employeeId), FontFactory.getFont(FontFactory.HELVETICA, 12));
//	            
//	            addCell(empTable, "Department:", headerFont);
//	            addCell(empTable, employeeDept, FontFactory.getFont(FontFactory.HELVETICA, 12));
//	            
//	            addCell(empTable, "Designation:", headerFont);
//	            addCell(empTable, employeeDesig, FontFactory.getFont(FontFactory.HELVETICA, 12));
//	            
//	            addCell(empTable, "Payment Date:", headerFont);
//	            addCell(empTable, salaryPayment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), 
//	                FontFactory.getFont(FontFactory.HELVETICA, 12));
//	            
//	            document.add(empTable);
//	            
//	            // Earnings Table
//	            PdfPTable earningsTable = new PdfPTable(2);
//	            earningsTable.setWidthPercentage(100);
//	            earningsTable.setSpacingBefore(15);
//	            
//	            PdfPCell earningsHeader = new PdfPCell(new Phrase("EARNINGS", headerFont));
//	            earningsHeader.setColspan(2);
//	            earningsHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
//	            earningsTable.addCell(earningsHeader);
//	            
//	            addRowToTable(earningsTable, "Basic Salary", "₹ " + salaryPayment.getBasicSalary());
//	            addRowToTable(earningsTable, "HRA", "₹ " + salaryPayment.getHra());
//	            addRowToTable(earningsTable, "Dearness Allowance", "₹ " + salaryPayment.getDearnessAllowance());
//	            addRowToTable(earningsTable, "Other Allowances", "₹ " + salaryPayment.getOtherAllowances());
//	            
//	            PdfPCell grossCell1 = new PdfPCell(new Phrase("Gross Salary", headerFont));
//	            PdfPCell grossCell2 = new PdfPCell(new Phrase("₹ " + salaryPayment.getGrossSalary(), headerFont));
//	            earningsTable.addCell(grossCell1);
//	            earningsTable.addCell(grossCell2);
//	            
//	            document.add(earningsTable);
//	            
//	            // Deductions Table
//	            PdfPTable deductionsTable = new PdfPTable(2);
//	            deductionsTable.setWidthPercentage(100);
//	            deductionsTable.setSpacingBefore(15);
//	            
//	            PdfPCell deductionsHeader = new PdfPCell(new Phrase("DEDUCTIONS", headerFont));
//	            deductionsHeader.setColspan(2);
//	            deductionsHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
//	            deductionsTable.addCell(deductionsHeader);
//	            
//	            addRowToTable(deductionsTable, "Provident Fund", "₹ " + salaryPayment.getProvidentFund());
//	            
//	            document.add(deductionsTable);
//	            
//	            // Net Salary
//	            PdfPTable netTable = new PdfPTable(2);
//	            netTable.setWidthPercentage(100);
//	            netTable.setSpacingBefore(15);
//	            
//	            Font netFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
//	            PdfPCell netCell1 = new PdfPCell(new Phrase("NET SALARY", netFont));
//	            PdfPCell netCell2 = new PdfPCell(new Phrase("₹ " + salaryPayment.getNetSalary(), netFont));
//	            netTable.addCell(netCell1);
//	            netTable.addCell(netCell2);
//	            
//	            document.add(netTable);
//	            
//	            // Transaction Details
//	            Paragraph txnDetails = new Paragraph("\nTransaction ID: " + salaryPayment.getTransactionId(), 
//	                FontFactory.getFont(FontFactory.HELVETICA, 10));
//	            txnDetails.setSpacingBefore(20);
//	            document.add(txnDetails);
//	            
//	            document.close();
//	            System.out.println("✅ PDF document closed");
//	            
//	            byte[] pdfBytes = out.toByteArray();
//	            System.out.println("✅ PDF generated successfully!");
//	            System.out.println("📊 PDF Size: " + pdfBytes.length + " bytes");
//	            System.out.println("═══════════════════════════════════════");
//	            
//	            return pdfBytes;
//	            
//	        } catch (Exception e) {
//	            System.err.println("❌ PDF GENERATION FAILED!");
//	            System.err.println("Error Type: " + e.getClass().getName());
//	            System.err.println("Error Message: " + e.getMessage());
//	            e.printStackTrace();
//	            throw new BadRequestException("Failed to generate PDF: " + e.getMessage());
//	        }
//	    }
//	    
//
//	    @Override
//	    public byte[] generateSalaryReportPdf(Long organizationId, String month, Integer year) {
//	        try {
//	            Organization organization = organizationRepo.findById(organizationId)
//	                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
//	            
//	            List<SalaryPayment> payments = 
//	                salaryPaymentRepo.findByOrganizationAndMonthAndYear(organization, month, year);
//	            
//	            ByteArrayOutputStream out = new ByteArrayOutputStream();
//	            Document document = new Document(PageSize.A4.rotate());
//	            PdfWriter.getInstance(document, out);
//	            
//	            document.open();
//	            
//	            // Title
//	            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
//	            Paragraph title = new Paragraph("SALARY PAYMENT REPORT", titleFont);
//	            title.setAlignment(Element.ALIGN_CENTER);
//	            title.setSpacingAfter(10);
//	            document.add(title);
//	            
//	            // Organization and Period
//	            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
//	            Paragraph orgName = new Paragraph(organization.getName(), headerFont);
//	            orgName.setAlignment(Element.ALIGN_CENTER);
//	            document.add(orgName);
//	            
//	            Paragraph period = new Paragraph("For the month of " + month + " " + year);
//	            period.setAlignment(Element.ALIGN_CENTER);
//	            period.setSpacingAfter(20);
//	            document.add(period);
//	            
//	            // Summary
//	            PdfPTable summaryTable = new PdfPTable(2);
//	            summaryTable.setWidthPercentage(50);
//	            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
//	            summaryTable.setSpacingAfter(20);
//	            
//	            addCell(summaryTable, "Total Employees:", headerFont);
//	            addCell(summaryTable, String.valueOf(payments.size()), FontFactory.getFont(FontFactory.HELVETICA, 12));
//	            
//	            addCell(summaryTable, "Total Amount Paid:", headerFont);
//	            addCell(summaryTable, "₹ " + payments.stream()
//	                .map(SalaryPayment::getNetSalary)
//	                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add), 
//	                FontFactory.getFont(FontFactory.HELVETICA, 12));
//	            
//	            document.add(summaryTable);
//	            
//	            // Payment Details Table
//	            PdfPTable table = new PdfPTable(7);
//	            table.setWidthPercentage(100);
//	            table.setWidths(new int[]{2, 3, 2, 2, 2, 2, 3});
//	            
//	            // Headers
//	            String[] headers = {"Emp ID", "Name", "Dept", "Gross", "Deductions", "Net Salary", "Status"};
//	            for (String header : headers) {
//	                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
//	                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//	                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
//	                table.addCell(cell);
//	            }
//	            
//	            // Data rows - ⭐ FORCE LOAD EMPLOYEE DATA
//	            for (SalaryPayment payment : payments) {
//	                Employee emp = payment.getEmployee();
//	                table.addCell(String.valueOf(emp.getId()));
//	                table.addCell(emp.getName());
//	                table.addCell(emp.getDepartment());
//	                table.addCell("₹ " + payment.getGrossSalary());
//	                table.addCell("₹ " + payment.getProvidentFund());
//	                table.addCell("₹ " + payment.getNetSalary());
//	                table.addCell(payment.getStatus().name());
//	            }
//	            
//	            document.add(table);
//	            
//	            // Footer
//	            Paragraph footer = new Paragraph("\nGenerated on: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), 
//	                FontFactory.getFont(FontFactory.HELVETICA, 8));
//	            footer.setAlignment(Element.ALIGN_RIGHT);
//	            footer.setSpacingBefore(20);
//	            document.add(footer);
//	            
//	            document.close();
//	            
//	            return out.toByteArray();
//	            
//	        } catch (DocumentException e) {
//	            throw new BadRequestException("Failed to generate PDF report: " + e.getMessage());
//	        }
//	    }
//	    
//
//    
//    
//    private void addCell(PdfPTable table, String text, Font font) {
//        PdfPCell cell = new PdfPCell(new Phrase(text, font));
//        table.addCell(cell);
//    }
//    
//    private void addRowToTable(PdfPTable table, String label, String value) {
//        table.addCell(new Phrase(label));
//        table.addCell(new Phrase(value));
//    }
//}