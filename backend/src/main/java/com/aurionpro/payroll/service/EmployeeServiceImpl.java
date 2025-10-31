package com.aurionpro.payroll.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.EmployeeRequest;
import com.aurionpro.payroll.dto.response.EmployeeList;
import com.aurionpro.payroll.dto.response.EmployeeProfile;
import com.aurionpro.payroll.dto.response.EmployeeResponse;
import com.aurionpro.payroll.entity.Employee;
import com.aurionpro.payroll.entity.Organization;
import com.aurionpro.payroll.entity.SalaryStructure;
import com.aurionpro.payroll.entity.User;
import com.aurionpro.payroll.enums.AccountVerificationStatus;
import com.aurionpro.payroll.enums.EmployeeStatus;
import com.aurionpro.payroll.enums.Role;
import com.aurionpro.payroll.enums.UserStatus;
import com.aurionpro.payroll.exception.BadRequestException;
import com.aurionpro.payroll.exception.DuplicateResourceException;
import com.aurionpro.payroll.exception.ResourceNotFoundException;
import com.aurionpro.payroll.repo.EmployeeRepo;
import com.aurionpro.payroll.repo.OrganizationRepo;
import com.aurionpro.payroll.repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService{

	
	@Autowired
    private EmployeeRepo employeeRepo;
    
    @Autowired
    private OrganizationRepo organizationRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    
    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request, Long organizationId) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        if (!organization.getVerified()) {
            throw new BadRequestException("Cannot add employees to unverified organization");
        }
        
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        
        if (employeeRepo.existsByBankAccountNumber(request.getBankAccountNumber())) {
            throw new DuplicateResourceException("Employee", "bankAccountNumber", 
                request.getBankAccountNumber());
        }
        
        //CREATE USER WITH DUMMY PASSWORD (will be set during employee registration)
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode("TEMP_PASSWORD_CHANGE_REQUIRED"))  // ⭐ Temporary password
            .role(Role.EMPLOYEE)
            .status(UserStatus.PENDING)  //PENDING until employee completes registration
            .build();
          
        User savedUser = userRepo.save(user);
            
        Employee employee = modelMapper.map(request, Employee.class);
        employee.setOrganization(organization);
        employee.setUser(savedUser);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setAccountVerificationStatus(AccountVerificationStatus.PENDING);
            
        Employee savedEmployee = employeeRepo.save(employee);

        // ⭐ SEND REGISTRATION INVITATION EMAIL
        try {
            emailService.sendEmployeeInvitation(
                request.getEmail(),
                request.getName(),
                organization.getName()
            );
        } catch (Exception e) {
            System.err.println("❌ Failed to send invitation email: " + e.getMessage());
        }
        
        return modelMapper.map(savedEmployee, EmployeeResponse.class);
    }
    
    
    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        return modelMapper.map(employee, EmployeeResponse.class);
    }
    
    @Override
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Employee employee = employeeRepo.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "userId", userId));
        
        return modelMapper.map(employee, EmployeeResponse.class);
    }
    
    
    @Override
    public EmployeeProfile getEmployeeProfile(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Employee employee = employeeRepo.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "userId", userId));
        
        return modelMapper.map(employee, EmployeeProfile.class);
    }
    
    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        if (!request.getEmail().equals(employee.getEmail()) && 
            userRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        
        if (!request.getBankAccountNumber().equals(employee.getBankAccountNumber()) &&
            employeeRepo.existsByBankAccountNumber(request.getBankAccountNumber())) {
            throw new DuplicateResourceException("Employee", "bankAccountNumber", 
                request.getBankAccountNumber());
        }
        
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setDesignation(request.getDesignation());
        employee.setDepartment(request.getDepartment());
        employee.setBankAccountNumber(request.getBankAccountNumber());
        employee.setBankName(request.getBankName());
        employee.setIfscCode(request.getIfscCode());
        
        User user = employee.getUser();
        user.setEmail(request.getEmail());
        userRepo.save(user);
        
        Employee updatedEmployee = employeeRepo.save(employee);
        
        return modelMapper.map(updatedEmployee, EmployeeResponse.class);
    }
    
    
    @Override
    public List<EmployeeList> getAllEmployeesByOrganization(Long organizationId) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<Employee> employees = employeeRepo.findByOrganization(organization);
        
        return employees.stream()
            .map(emp -> {
                EmployeeList dto = modelMapper.map(emp, EmployeeList.class);
                
                // ⭐ ADD: Account verification status
                dto.setAccountVerificationStatus(emp.getAccountVerificationStatus());
                
                // Current salary from active salary structure
                SalaryStructure activeSalary = emp.getActiveSalaryStructure();  // ⭐ Uses helper method
                if (activeSalary != null) {
                    dto.setCurrentSalary(activeSalary.getNetSalary().doubleValue());
                } else {
                    dto.setCurrentSalary(null);
                }
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public EmployeeResponse addOrUpdateSalaryStructure(Long employeeId, SalaryStructure newStructure) {
        Employee employee = employeeRepo.findByIdWithSalaryStructures(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        employee.setActiveSalaryStructure(newStructure);
        employeeRepo.save(employee);

        return modelMapper.map(employee, EmployeeResponse.class);
    }
    
    
    
    @Override
    public List<EmployeeList> getEmployeesByStatus(Long organizationId, EmployeeStatus status) {
        Organization organization = organizationRepo.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        List<Employee> employees = employeeRepo.findByOrganizationAndStatus(organization, status);
        
        return employees.stream()
            .map(emp -> modelMapper.map(emp, EmployeeList.class))
            .collect(Collectors.toList());
    }
    

    @Override
    public EmployeeResponse uploadAccountProof(Long id, MultipartFile file) {
        Employee employee = employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        try {
            // Upload to Cloudinary (now supports PDF, JPG, PNG)
            String proofUrl = cloudinaryService.uploadFile(file, "employee_account_proofs");
            
            employee.setAccountProofUrl(proofUrl);
            Employee updatedEmployee = employeeRepo.save(employee);
            
            return modelMapper.map(updatedEmployee, EmployeeResponse.class);
            
        } catch (Exception e) {
            throw new BadRequestException("Failed to upload account proof: " + e.getMessage());
        }
    }
    
    
    @Override
    public EmployeeResponse verifyEmployeeAccount(Long id) {
        Employee employee = employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
     
        //Prevent verification of terminated employees
        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new BadRequestException("Cannot verify account for terminated employee. Please reactivate the employee first.");
        }
        
        if (employee.getAccountVerificationStatus() == AccountVerificationStatus.VERIFIED) {
            throw new BadRequestException("Employee account is already verified");
        }
        
        employee.setAccountVerificationStatus(AccountVerificationStatus.VERIFIED);
        
        User user = employee.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
        
        Employee verifiedEmployee = employeeRepo.save(employee);

        // Send verification email
        try {
            emailService.sendEmployeeAccountVerificationEmail(
                user.getEmail(),
                employee.getName()
            );
            System.out.println("✅ Verification email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
        }
        
        return modelMapper.map(verifiedEmployee, EmployeeResponse.class);
    }
    
    
    @Override
    public EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status) {
        Employee employee = employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        employee.setStatus(status);
        
        if (status == EmployeeStatus.INACTIVE || status == EmployeeStatus.TERMINATED) {
            User user = employee.getUser();
            user.setStatus(UserStatus.INACTIVE);
            userRepo.save(user);
        }
        
        Employee updatedEmployee = employeeRepo.save(employee);
        
        return modelMapper.map(updatedEmployee, EmployeeResponse.class);
    }
    
    
    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        
        employee.setStatus(EmployeeStatus.TERMINATED);
        
        User user = employee.getUser();
        user.setStatus(UserStatus.INACTIVE);
        userRepo.save(user);
        
        employeeRepo.save(employee);
      
    }
    
    @Override
    public void updateProfilePicture(Long employeeId, String profilePictureUrl) {
        Employee employee = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        employee.setProfilePictureUrl(profilePictureUrl);
        employeeRepo.save(employee);
    }
        
        
}
