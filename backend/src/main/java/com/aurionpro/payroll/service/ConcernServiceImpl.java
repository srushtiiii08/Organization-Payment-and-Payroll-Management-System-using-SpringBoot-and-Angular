package com.aurionpro.payroll.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.ConcernRequest;
import com.aurionpro.payroll.dto.response.ConcernList;
import com.aurionpro.payroll.dto.response.ConcernResponse;
import com.aurionpro.payroll.entity.Concern;
import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.enums.ConcernStatus;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.ConcernRepo;
import com.aurionpro.payroll.repo.EmployeeRepo;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ConcernServiceImpl implements ConcernService{

	@Autowired
    private ConcernRepo concernRepo;
    
    @Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private OrganizationRepo organizationRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Override
    public ConcernResponse createConcern(ConcernRequest request, Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        //map dto to entity
        Concern concern = modelMapper.map(request, Concern.class);
        concern.setEmployee(employee);
        concern.setOrganization(employee.getOrganization());
        concern.setStatus(ConcernStatus.OPEN);
        
        Concern savedConcern = concernRepo.save(concern);
        
        return modelMapper.map(savedConcern, ConcernResponse.class);
    }
    
    @Override
    public ConcernResponse getConcernById(Long id) {
        Concern concern = concernRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Concern", "id", id));
        
        return modelMapper.map(concern, ConcernResponse.class);
    }
    
    
    @Override
    public List<ConcernList> getConcernsByEmployee(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        List<Concern> concerns = concernRepo.findByEmployeeOrderByRaisedAtDesc(employee);
        
        // ✅ Manual mapping - NO ModelMapper issues
        return concerns.stream()
            .map(concern -> {
                ConcernList dto = new ConcernList();
                
                // set fields
                dto.setId(concern.getId());
                dto.setSubject(concern.getSubject());
                dto.setTitle(concern.getSubject()); // Frontend uses 'title'
                dto.setDescription(concern.getDescription());
                dto.setPriority(concern.getPriority());
                dto.setStatus(concern.getStatus());
                dto.setCreatedAt(concern.getRaisedAt()); // Map raisedAt to createdAt
                dto.setEmployeeName(concern.getEmployee() != null ? 
                    concern.getEmployee().getName() : "Unknown");
                dto.setReportedBy(concern.getEmployee() != null ? 
                    concern.getEmployee().getName() : "Unknown");
                dto.setCategory(concern.getPriority().toString()); // Use priority as category
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    
    @Override
    public List<ConcernList> getConcernsByOrganization(Long organizationId) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<Concern> concerns = concernRepo.findByOrganizationOrderByRaisedAtDesc(organization);
        
        // ✅ Manual mapping - same as above
        return concerns.stream()
            .map(concern -> {
                ConcernList dto = new ConcernList();
                
                // set fields
                dto.setId(concern.getId());
                dto.setSubject(concern.getSubject());
                dto.setTitle(concern.getSubject());
                dto.setDescription(concern.getDescription());
                dto.setPriority(concern.getPriority());
                dto.setStatus(concern.getStatus());
                dto.setCreatedAt(concern.getRaisedAt());
                dto.setEmployeeName(concern.getEmployee() != null ? 
                    concern.getEmployee().getName() : "Unknown");
                dto.setReportedBy(concern.getEmployee() != null ? 
                    concern.getEmployee().getName() : "Unknown");
                dto.setCategory(concern.getPriority().toString());
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    
    @Override
    public List<ConcernList> getConcernsByStatus(Long organizationId, ConcernStatus status) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<Concern> concerns = concernRepo.findByOrganizationAndStatus(organization, status);
        
        return concerns.stream()
            .map(concern -> modelMapper.map(concern, ConcernList.class))
            .collect(Collectors.toList());
    }
    
    
    @Override
    public ConcernResponse respondToConcern(Long id, String response, Long respondedByUserId) {
        Concern concern = concernRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Concern", "id", id));
        
        if (concern.getStatus() == ConcernStatus.CLOSED) {
            throw new BadRequestException("Cannot respond to a closed concern");
        }
        
        User respondedBy = userRepo.findById(respondedByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", respondedByUserId));
        
        concern.setResponse(response);
        concern.setRespondedBy(respondedBy);
        concern.setRespondedAt(LocalDateTime.now());
        concern.setStatus(ConcernStatus.IN_PROGRESS);
        
        Concern updatedConcern = concernRepo.save(concern);

        // Send email to employee
        try {
            Employee employee = concern.getEmployee();
            emailService.sendConcernResponseEmail(
                employee.getUser().getEmail(),
                employee.getName(),
                concern.getSubject()
            );
            System.out.println("✅ Concern response email sent to: " + employee.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send concern response email: " + e.getMessage());
        }
        
        return modelMapper.map(updatedConcern, ConcernResponse.class);
    }

    @Override
    public ConcernResponse uploadAttachment(Long concernId, MultipartFile file) {
        Concern concern = concernRepo.findById(concernId)
            .orElseThrow(() -> new ResourceNotFoundException("Concern", "id", concernId));
        

        try {
            // Upload to Cloudinary (now supports PDF, JPG, PNG)
            String attachmentUrl = cloudinaryService.uploadFile(file, "concern_attachments");
            
            concern.setAttachmentUrl(attachmentUrl);
            Concern updatedConcern = concernRepo.save(concern);
           
            return modelMapper.map(updatedConcern, ConcernResponse.class);
            
        } catch (Exception e) {
            throw new BadRequestException("Failed to upload attachment: " + e.getMessage());
        }
    }
    
    
    @Override
    public ConcernResponse updateConcernStatus(Long id, ConcernStatus status) {
        Concern concern = concernRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Concern", "id", id));
        
        // Validate status transition
        if (concern.getStatus() == ConcernStatus.CLOSED && status != ConcernStatus.CLOSED) {
            throw new BadRequestException("Cannot reopen a closed concern");
        }
        
        concern.setStatus(status);
        
        Concern updatedConcern = concernRepo.save(concern);
        
        return modelMapper.map(updatedConcern, ConcernResponse.class);
    }
    
    
    @Override
    public ConcernResponse closeConcern(Long id) {
        Concern concern = concernRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Concern", "id", id));
        
        if (concern.getResponse() == null || concern.getResponse().trim().isEmpty()) {
            throw new BadRequestException("Cannot close concern without a response");
        }
        
        concern.setStatus(ConcernStatus.CLOSED);
        
        Concern closedConcern = concernRepo.save(concern);
        
        return modelMapper.map(closedConcern, ConcernResponse.class);
    }
    
    @Override
    public void deleteConcern(Long id) {
        Concern concern = concernRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Concern", "id", id));
        
        concernRepo.delete(concern);
    }
    
    
    
}
