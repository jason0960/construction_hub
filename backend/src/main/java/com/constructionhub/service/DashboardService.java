package com.constructionhub.service;

import com.constructionhub.dto.DashboardResponse;
import com.constructionhub.entity.Job;
import com.constructionhub.entity.JobStatus;
import com.constructionhub.repository.JobRepository;
import com.constructionhub.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final JobRepository jobRepository;
    private final WorkerRepository workerRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long organizationId) {
        List<Job> jobs = jobRepository.findByOrganizationIdAndDeletedAtIsNullOrderByUpdatedAtDesc(organizationId);
        long workerCount = workerRepository.countByOrganizationIdAndDeletedAtIsNull(organizationId);

        int active = 0;
        int completed = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, Integer> byStatus = new LinkedHashMap<>();

        for (JobStatus s : JobStatus.values()) {
            byStatus.put(s.name(), 0);
        }

        for (Job job : jobs) {
            String status = job.getStatus().name();
            byStatus.merge(status, 1, Integer::sum);

            if (job.getStatus() == JobStatus.IN_PROGRESS || job.getStatus() == JobStatus.CONTRACTED) {
                active++;
            }
            if (job.getStatus() == JobStatus.COMPLETED) {
                completed++;
                if (job.getContractPrice() != null) {
                    totalRevenue = totalRevenue.add(job.getContractPrice());
                }
            }
        }

        List<DashboardResponse.RecentJob> recentJobs = jobs.stream()
                .limit(5)
                .map(j -> DashboardResponse.RecentJob.builder()
                        .id(j.getId())
                        .title(j.getTitle())
                        .status(j.getStatus().name())
                        .clientName(j.getClient() != null ? j.getClient().getName() : null)
                        .updatedAt(j.getUpdatedAt() != null ? j.getUpdatedAt().toString() : null)
                        .build())
                .toList();

        log.debug("Dashboard loaded: orgId={} totalJobs={} activeJobs={} workers={} revenue={}", organizationId, jobs.size(), active, workerCount, totalRevenue);
        return DashboardResponse.builder()
                .totalJobs(jobs.size())
                .activeJobs(active)
                .completedJobs(completed)
                .totalWorkers((int) workerCount)
                .totalRevenue(totalRevenue)
                .jobsByStatus(byStatus)
                .recentJobs(recentJobs)
                .build();
    }
}
