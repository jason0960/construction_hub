package com.constructionhub.repository;

import com.constructionhub.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByJobIdOrderByCreatedAtDesc(Long jobId);
    List<Document> findByPermitId(Long permitId);
    List<Document> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}
