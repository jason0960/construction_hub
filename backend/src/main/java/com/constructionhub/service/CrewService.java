package com.constructionhub.service;

import com.constructionhub.dto.*;
import com.constructionhub.entity.*;
import com.constructionhub.exception.BusinessException;
import com.constructionhub.exception.ResourceNotFoundException;
import com.constructionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrewService {

    private final CrewAssignmentRepository crewAssignmentRepository;
    private final JobRepository jobRepository;
    private final WorkerRepository workerRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Transactional(readOnly = true)
    public List<CrewAssignmentResponse> getJobCrew(Long jobId, Long organizationId) {
        jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        List<CrewAssignment> assignments = crewAssignmentRepository.findByJobId(jobId);
        return assignments.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public CrewAssignmentResponse assignWorker(Long jobId, CrewAssignmentRequest request, Long organizationId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        Worker worker = workerRepository.findByIdAndOrganizationId(request.getWorkerId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", request.getWorkerId()));

        // Check if already assigned
        crewAssignmentRepository.findByJobIdAndWorkerId(jobId, worker.getId())
                .ifPresent(existing -> {
                    throw new BusinessException("Worker is already assigned to this job");
                });

        CrewAssignment assignment = CrewAssignment.builder()
                .job(job)
                .worker(worker)
                .roleOnJob(request.getRoleOnJob())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(AssignmentStatus.ASSIGNED)
                .build();

        assignment = crewAssignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    @Transactional
    public CrewAssignmentResponse updateAssignment(Long assignmentId, CrewAssignmentRequest request, Long organizationId) {
        CrewAssignment assignment = crewAssignmentRepository.findByIdAndOrganizationId(assignmentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));

        if (request.getRoleOnJob() != null) assignment.setRoleOnJob(request.getRoleOnJob());
        if (request.getStartDate() != null) assignment.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) assignment.setEndDate(request.getEndDate());

        assignment = crewAssignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    @Transactional
    public void removeWorkerFromJob(Long assignmentId, Long organizationId) {
        CrewAssignment assignment = crewAssignmentRepository.findByIdAndOrganizationId(assignmentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));
        assignment.setStatus(AssignmentStatus.REMOVED);
        crewAssignmentRepository.save(assignment);
    }

    private CrewAssignmentResponse mapToResponse(CrewAssignment ca) {
        BigDecimal totalHours = timeEntryRepository.calculateHoursByJobAndWorker(
                ca.getJob().getId(), ca.getWorker().getId());

        return CrewAssignmentResponse.builder()
                .id(ca.getId())
                .jobId(ca.getJob().getId())
                .jobTitle(ca.getJob().getTitle())
                .workerId(ca.getWorker().getId())
                .workerName(ca.getWorker().getFullName())
                .workerTrade(ca.getWorker().getTrade())
                .workerHourlyRate(ca.getWorker().getHourlyRate())
                .roleOnJob(ca.getRoleOnJob())
                .startDate(ca.getStartDate())
                .endDate(ca.getEndDate())
                .status(ca.getStatus().name())
                .totalHours(totalHours)
                .build();
    }
}
