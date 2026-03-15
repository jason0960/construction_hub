package com.constructionhub.controller;

import com.constructionhub.dto.*;
import com.constructionhub.entity.User;
import com.constructionhub.service.PermitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/permits")
@RequiredArgsConstructor
public class PermitController {

    private final PermitService permitService;

    @GetMapping
    public ResponseEntity<List<PermitResponse>> getJobPermits(@PathVariable Long jobId) {
        return ResponseEntity.ok(permitService.getJobPermits(jobId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PermitResponse> createPermit(
            @PathVariable Long jobId,
            @Valid @RequestBody PermitRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(permitService.createPermit(jobId, request, user.getOrganization().getId()));
    }

    @PutMapping("/{permitId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PermitResponse> updatePermit(
            @PathVariable Long permitId,
            @Valid @RequestBody PermitRequest request) {
        return ResponseEntity.ok(permitService.updatePermit(permitId, request));
    }

    @DeleteMapping("/{permitId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deletePermit(@PathVariable Long permitId) {
        permitService.deletePermit(permitId);
        return ResponseEntity.noContent().build();
    }
}
