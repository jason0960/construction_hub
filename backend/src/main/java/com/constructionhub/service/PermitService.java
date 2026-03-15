package com.constructionhub.service;

import com.constructionhub.dto.*;
import com.constructionhub.entity.*;
import com.constructionhub.exception.ResourceNotFoundException;
import com.constructionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermitService {

    private final PermitRepository permitRepository;
    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public List<PermitResponse> getJobPermits(Long jobId, Long organizationId) {
        jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        return permitRepository.findByJobIdOrderByExpirationDateAsc(jobId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PermitResponse createPermit(Long jobId, PermitRequest request, Long organizationId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        Permit permit = Permit.builder()
                .job(job)
                .permitType(request.getPermitType())
                .permitNumber(request.getPermitNumber())
                .issuingAuthority(request.getIssuingAuthority())
                .status(request.getStatus() != null ? request.getStatus() : PermitStatus.PENDING)
                .fee(request.getFee() != null ? request.getFee() : java.math.BigDecimal.ZERO)
                .applicationDate(request.getApplicationDate())
                .issueDate(request.getIssueDate())
                .expirationDate(request.getExpirationDate())
                .reminderDaysBefore(request.getReminderDaysBefore() != null ? request.getReminderDaysBefore() : 30)
                .notes(request.getNotes())
                .build();

        permit = permitRepository.save(permit);
        return mapToResponse(permit);
    }

    @Transactional
    public PermitResponse updatePermit(Long permitId, PermitRequest request, Long organizationId) {
        Permit permit = permitRepository.findByIdAndOrganizationId(permitId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Permit", permitId));

        permit.setPermitType(request.getPermitType());
        permit.setPermitNumber(request.getPermitNumber());
        permit.setIssuingAuthority(request.getIssuingAuthority());
        if (request.getStatus() != null) permit.setStatus(request.getStatus());
        if (request.getFee() != null) permit.setFee(request.getFee());
        permit.setApplicationDate(request.getApplicationDate());
        permit.setIssueDate(request.getIssueDate());
        permit.setExpirationDate(request.getExpirationDate());
        if (request.getReminderDaysBefore() != null) permit.setReminderDaysBefore(request.getReminderDaysBefore());
        permit.setNotes(request.getNotes());

        permit = permitRepository.save(permit);
        return mapToResponse(permit);
    }

    @Transactional
    public void deletePermit(Long permitId, Long organizationId) {
        permitRepository.findByIdAndOrganizationId(permitId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Permit", permitId));
        permitRepository.deleteById(permitId);
    }

    private PermitResponse mapToResponse(Permit permit) {
        return PermitResponse.builder()
                .id(permit.getId())
                .jobId(permit.getJob().getId())
                .permitType(permit.getPermitType())
                .permitNumber(permit.getPermitNumber())
                .issuingAuthority(permit.getIssuingAuthority())
                .status(permit.getStatus().name())
                .fee(permit.getFee())
                .applicationDate(permit.getApplicationDate())
                .issueDate(permit.getIssueDate())
                .expirationDate(permit.getExpirationDate())
                .reminderDaysBefore(permit.getReminderDaysBefore())
                .notes(permit.getNotes())
                .createdAt(permit.getCreatedAt())
                .build();
    }
}
