package com.constructionhub.repository;

import com.constructionhub.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByJobIdAndDeletedAtIsNull(Long jobId);
    List<Invoice> findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long organizationId);
    Optional<Invoice> findByIdAndOrganizationId(Long id, Long organizationId);
}
