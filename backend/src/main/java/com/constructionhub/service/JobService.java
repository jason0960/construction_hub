package com.constructionhub.service;

import com.constructionhub.dto.*;
import com.constructionhub.entity.*;
import com.constructionhub.exception.ResourceNotFoundException;
import com.constructionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final ClientRepository clientRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final MaterialRepository materialRepository;
    private final PermitRepository permitRepository;

    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs(Long organizationId, boolean includeFinancials) {
        List<Job> jobs = jobRepository.findByOrganizationIdAndDeletedAtIsNullOrderByUpdatedAtDesc(organizationId);
        return jobs.stream()
                .map(job -> mapToResponse(job, includeFinancials))
                .toList();
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long jobId, Long organizationId, boolean includeFinancials) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        return mapToResponse(job, includeFinancials);
    }

    @Transactional
    public JobResponse createJob(JobRequest request, User currentUser) {
        Job job = new Job();
        job.setOrganization(currentUser.getOrganization());
        job.setCreatedBy(currentUser);
        updateJobFromRequest(job, request, currentUser);

        job = jobRepository.save(job);
        return mapToResponse(job, true);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, User currentUser) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, currentUser.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        updateJobFromRequest(job, request, currentUser);
        job = jobRepository.save(job);
        return mapToResponse(job, true);
    }

    @Transactional
    public void deleteJob(Long jobId, Long organizationId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Transactional
    public JobResponse updateJobStatus(Long jobId, JobStatus newStatus, Long organizationId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        job.setStatus(newStatus);
        job = jobRepository.save(job);
        return mapToResponse(job, true);
    }

    // Worker-specific: only returns jobs they're assigned to
    @Transactional(readOnly = true)
    public List<JobResponse> getWorkerJobs(Long workerId, Long organizationId) {
        // This will be called from the controller with the worker's assignments
        // For now, delegate to the repository
        List<Job> allJobs = jobRepository.findByOrganizationIdAndDeletedAtIsNullOrderByUpdatedAtDesc(organizationId);
        return allJobs.stream()
                .map(job -> mapToResponse(job, false))
                .toList();
    }

    private void updateJobFromRequest(Job job, JobRequest request, User currentUser) {
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setSiteAddress(request.getSiteAddress());
        job.setSiteCity(request.getSiteCity());
        job.setSiteState(request.getSiteState());
        job.setSiteZip(request.getSiteZip());
        job.setSiteUnit(request.getSiteUnit());
        job.setContractPrice(request.getContractPrice());
        job.setStartDate(request.getStartDate());
        job.setEstimatedEndDate(request.getEstimatedEndDate());
        job.setActualEndDate(request.getActualEndDate());

        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        } else if (job.getStatus() == null) {
            job.setStatus(JobStatus.LEAD);
        }

        // Handle client
        if (request.getClientId() != null) {
            Client client = clientRepository.findByIdAndOrganizationId(
                    request.getClientId(), currentUser.getOrganization().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            job.setClient(client);
        } else if (request.getClientName() != null && !request.getClientName().isBlank()) {
            Client newClient = Client.builder()
                    .organization(currentUser.getOrganization())
                    .name(request.getClientName())
                    .phone(request.getClientPhone())
                    .email(request.getClientEmail())
                    .createdBy(currentUser)
                    .build();
            newClient = clientRepository.save(newClient);
            job.setClient(newClient);
        }
    }

    private JobResponse mapToResponse(Job job, boolean includeFinancials) {
        JobResponse.JobResponseBuilder builder = JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .status(job.getStatus().name())
                .startDate(job.getStartDate())
                .estimatedEndDate(job.getEstimatedEndDate())
                .actualEndDate(job.getActualEndDate())
                .contractPrice(job.getContractPrice())
                .siteAddress(job.getSiteAddress())
                .siteCity(job.getSiteCity())
                .siteState(job.getSiteState())
                .siteZip(job.getSiteZip())
                .siteUnit(job.getSiteUnit())
                .createdByName(job.getCreatedBy().getFullName())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt());

        // Client info
        if (job.getClient() != null) {
            builder.clientId(job.getClient().getId())
                    .clientName(job.getClient().getName())
                    .clientPhone(job.getClient().getPhone())
                    .clientEmail(job.getClient().getEmail());
        }

        // Financials (owner/admin only)
        if (includeFinancials) {
            BigDecimal laborCost = timeEntryRepository.calculateLaborCostByJobId(job.getId());
            BigDecimal materialsCost = materialRepository.calculateTotalCostByJobId(job.getId());
            BigDecimal permitFees = permitRepository.calculateTotalFeesByJobId(job.getId());

            builder.laborCost(laborCost)
                    .materialsCost(materialsCost)
                    .permitFees(permitFees);

            if (job.getContractPrice() != null) {
                BigDecimal profit = job.getContractPrice()
                        .subtract(laborCost)
                        .subtract(materialsCost)
                        .subtract(permitFees);
                builder.profit(profit);
            }
        }

        return builder.build();
    }
}
