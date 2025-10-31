package com.aurionpro.payroll.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.payroll.dto.request.ConcernRequest;
import com.aurionpro.payroll.dto.response.ConcernList;
import com.aurionpro.payroll.dto.response.ConcernResponse;
import com.aurionpro.payroll.enums.ConcernStatus;

public interface ConcernService {

	ConcernResponse createConcern(ConcernRequest request, Long employeeId);
    
    ConcernResponse getConcernById(Long id);
    
    List<ConcernList> getConcernsByEmployee(Long employeeId);
    
    List<ConcernList> getConcernsByOrganization(Long organizationId);
    
    List<ConcernList> getConcernsByStatus(Long organizationId, ConcernStatus status);
    
    ConcernResponse respondToConcern(Long id, String response, Long respondedByUserId);
    
    ConcernResponse uploadAttachment(Long concernId, MultipartFile file);
    
    ConcernResponse updateConcernStatus(Long id, ConcernStatus status);
    
    ConcernResponse closeConcern(Long id);
    
    void deleteConcern(Long id);
}
