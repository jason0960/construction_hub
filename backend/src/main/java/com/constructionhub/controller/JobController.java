package com.constructionhub.controller;

import com.constructionhub.dto.*;
import com.constructionhub.entity.JobStatus;
import com.constructionhub.entity.User;
import com.constructionhub.entity.UserRole;
import com.constructionhub.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs(@AuthenticationPrincipal User user) {
        boolean includeFinancials = user.getRole() != UserRole.WORKER;
        return ResponseEntity.ok(jobService.getAllJobs(user.getOrganization().getId(), includeFinancials));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id, @AuthenticationPrincipal User user) {
        boolean includeFinancials = user.getRole() != UserRole.WORKER;
        return ResponseEntity.ok(jobService.getJob(id, user.getOrganization().getId(), includeFinancials));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobService.createJob(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobService.updateJob(id, request, user));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<JobResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam JobStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobService.updateJobStatus(id, status, user.getOrganization().getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, @AuthenticationPrincipal User user) {
        jobService.deleteJob(id, user.getOrganization().getId());
        return ResponseEntity.noContent().build();
    }
}
