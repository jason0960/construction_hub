package com.constructionhub.controller;

import com.constructionhub.dto.*;
import com.constructionhub.entity.User;
import com.constructionhub.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<WorkerResponse>> getAllWorkers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workerService.getAllWorkers(user.getOrganization().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerResponse> getWorker(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workerService.getWorker(id, user.getOrganization().getId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<WorkerResponse> createWorker(
            @Valid @RequestBody WorkerRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workerService.createWorker(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<WorkerResponse> updateWorker(
            @PathVariable Long id,
            @Valid @RequestBody WorkerRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workerService.updateWorker(id, request, user.getOrganization().getId()));
    }
}
