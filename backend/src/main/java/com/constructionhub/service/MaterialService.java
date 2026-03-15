package com.constructionhub.service;

import com.constructionhub.dto.*;
import com.constructionhub.entity.*;
import com.constructionhub.exception.ResourceNotFoundException;
import com.constructionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final JobRepository jobRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<MaterialResponse> getJobMaterials(Long jobId, Long organizationId) {
        jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        return materialRepository.findByJobIdOrderByCreatedAtDesc(jobId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public MaterialResponse createMaterial(Long jobId, MaterialRequest request, Long organizationId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        Material material = Material.builder()
                .job(job)
                .name(request.getName())
                .quantity(request.getQuantity())
                .unitCost(request.getUnitCost())
                .total(request.getQuantity().multiply(request.getUnitCost()))
                .build();

        if (request.getReceiptDocumentId() != null) {
            Document doc = documentRepository.findById(request.getReceiptDocumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Document", request.getReceiptDocumentId()));
            material.setReceiptDocument(doc);
        }

        material = materialRepository.save(material);
        return mapToResponse(material);
    }

    @Transactional
    public MaterialResponse updateMaterial(Long materialId, MaterialRequest request, Long organizationId) {
        Material material = materialRepository.findByIdAndOrganizationId(materialId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        material.setName(request.getName());
        material.setQuantity(request.getQuantity());
        material.setUnitCost(request.getUnitCost());
        material.setTotal(request.getQuantity().multiply(request.getUnitCost()));

        if (request.getReceiptDocumentId() != null) {
            Document doc = documentRepository.findById(request.getReceiptDocumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Document", request.getReceiptDocumentId()));
            material.setReceiptDocument(doc);
        }

        material = materialRepository.save(material);
        return mapToResponse(material);
    }

    @Transactional
    public void deleteMaterial(Long materialId, Long organizationId) {
        materialRepository.findByIdAndOrganizationId(materialId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));
        materialRepository.deleteById(materialId);
    }

    private MaterialResponse mapToResponse(Material material) {
        MaterialResponse.MaterialResponseBuilder builder = MaterialResponse.builder()
                .id(material.getId())
                .jobId(material.getJob().getId())
                .name(material.getName())
                .quantity(material.getQuantity())
                .unitCost(material.getUnitCost())
                .total(material.getTotal())
                .createdAt(material.getCreatedAt());

        if (material.getReceiptDocument() != null) {
            builder.receiptDocumentId(material.getReceiptDocument().getId())
                    .receiptFileName(material.getReceiptDocument().getFileName());
        }

        return builder.build();
    }
}
