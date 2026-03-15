package com.constructionhub.controller;

import com.constructionhub.dto.*;
import com.constructionhub.entity.User;
import com.constructionhub.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<MaterialResponse>> getJobMaterials(@PathVariable Long jobId) {
        return ResponseEntity.ok(materialService.getJobMaterials(jobId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> createMaterial(
            @PathVariable Long jobId,
            @Valid @RequestBody MaterialRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(materialService.createMaterial(jobId, request, user.getOrganization().getId()));
    }

    @PutMapping("/{materialId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<MaterialResponse> updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.ok(materialService.updateMaterial(materialId, request));
    }

    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) {
        materialService.deleteMaterial(materialId);
        return ResponseEntity.noContent().build();
    }
}
