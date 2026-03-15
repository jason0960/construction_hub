package com.constructionhub.controller;

import com.constructionhub.dto.*;
import com.constructionhub.entity.User;
import com.constructionhub.service.CrewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/crew")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;

    @GetMapping
    public ResponseEntity<List<CrewAssignmentResponse>> getJobCrew(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(crewService.getJobCrew(jobId, user.getOrganization().getId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<CrewAssignmentResponse> assignWorker(
            @PathVariable Long jobId,
            @RequestBody CrewAssignmentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(crewService.assignWorker(jobId, request, user.getOrganization().getId()));
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<CrewAssignmentResponse> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody CrewAssignmentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(crewService.updateAssignment(assignmentId, request, user.getOrganization().getId()));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> removeWorker(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User user) {
        crewService.removeWorkerFromJob(assignmentId, user.getOrganization().getId());
        return ResponseEntity.noContent().build();
    }
}
