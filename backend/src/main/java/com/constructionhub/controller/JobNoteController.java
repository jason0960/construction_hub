package com.constructionhub.controller;

import com.constructionhub.dto.*;
import com.constructionhub.entity.User;
import com.constructionhub.entity.UserRole;
import com.constructionhub.service.JobNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/notes")
@RequiredArgsConstructor
public class JobNoteController {

    private final JobNoteService jobNoteService;

    @GetMapping
    public ResponseEntity<List<JobNoteResponse>> getJobNotes(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User user) {
        boolean includeOwnerOnly = user.getRole() != UserRole.WORKER;
        return ResponseEntity.ok(jobNoteService.getJobNotes(jobId, includeOwnerOnly, user.getOrganization().getId()));
    }

    @PostMapping
    public ResponseEntity<JobNoteResponse> createNote(
            @PathVariable Long jobId,
            @Valid @RequestBody JobNoteRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobNoteService.createNote(jobId, request, user, user.getOrganization().getId()));
    }
}
