package com.constructionhub.service;

import com.constructionhub.dto.*;
import com.constructionhub.entity.*;
import com.constructionhub.exception.ResourceNotFoundException;
import com.constructionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final CrewAssignmentRepository crewAssignmentRepository;
    private final TimeEntryRepository timeEntryRepository;

    @Transactional(readOnly = true)
    public List<WorkerResponse> getAllWorkers(Long organizationId) {
        List<Worker> workers = workerRepository.findByOrganizationIdOrderByLastNameAsc(organizationId);
        return workers.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkerResponse getWorker(Long workerId, Long organizationId) {
        Worker worker = workerRepository.findByIdAndOrganizationId(workerId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", workerId));
        return mapToResponse(worker);
    }

    @Transactional
    public WorkerResponse createWorker(WorkerRequest request, User currentUser) {
        Worker worker = Worker.builder()
                .organization(currentUser.getOrganization())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .trade(request.getTrade())
                .hourlyRate(request.getHourlyRate() != null ? request.getHourlyRate() : BigDecimal.ZERO)
                .status(WorkerStatus.ACTIVE)
                .build();

        worker = workerRepository.save(worker);
        return mapToResponse(worker);
    }

    @Transactional
    public WorkerResponse updateWorker(Long workerId, WorkerRequest request, Long organizationId) {
        Worker worker = workerRepository.findByIdAndOrganizationId(workerId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", workerId));

        worker.setFirstName(request.getFirstName());
        worker.setLastName(request.getLastName());
        worker.setPhone(request.getPhone());
        worker.setEmail(request.getEmail());
        worker.setTrade(request.getTrade());
        if (request.getHourlyRate() != null) {
            worker.setHourlyRate(request.getHourlyRate());
        }
        if (request.getStatus() != null) {
            worker.setStatus(WorkerStatus.valueOf(request.getStatus()));
        }

        worker = workerRepository.save(worker);
        return mapToResponse(worker);
    }

    private WorkerResponse mapToResponse(Worker worker) {
        List<CrewAssignment> activeAssignments = crewAssignmentRepository.findActiveAssignmentsByWorkerId(worker.getId());

        List<WorkerResponse.WorkerJobAssignment> jobAssignments = activeAssignments.stream()
                .map(ca -> WorkerResponse.WorkerJobAssignment.builder()
                        .jobId(ca.getJob().getId())
                        .jobTitle(ca.getJob().getTitle())
                        .roleOnJob(ca.getRoleOnJob())
                        .assignmentStatus(ca.getStatus().name())
                        .build())
                .toList();

        return WorkerResponse.builder()
                .id(worker.getId())
                .firstName(worker.getFirstName())
                .lastName(worker.getLastName())
                .phone(worker.getPhone())
                .email(worker.getEmail())
                .trade(worker.getTrade())
                .hourlyRate(worker.getHourlyRate())
                .status(worker.getStatus().name())
                .currentJobs(jobAssignments)
                .build();
    }
}
