package com.constructionhub.controller;

import com.constructionhub.dto.*;
import com.constructionhub.entity.User;
import com.constructionhub.service.TimeEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @GetMapping
    public ResponseEntity<List<TimeEntryResponse>> getJobTimeEntries(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timeEntryService.getJobTimeEntries(jobId, user.getOrganization().getId()));
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponse> createTimeEntry(
            @PathVariable Long jobId,
            @Valid @RequestBody TimeEntryRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timeEntryService.createTimeEntry(jobId, request, user, user.getOrganization().getId()));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<TimeEntryResponse> updateTimeEntry(
            @PathVariable Long entryId,
            @Valid @RequestBody TimeEntryRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timeEntryService.updateTimeEntry(entryId, request, user, user.getOrganization().getId()));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteTimeEntry(
            @PathVariable Long entryId,
            @AuthenticationPrincipal User user) {
        timeEntryService.deleteTimeEntry(entryId, user.getOrganization().getId());
        return ResponseEntity.noContent().build();
    }
}
