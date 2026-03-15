package com.constructionhub.repository;

import com.constructionhub.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByJobIdOrderByCreatedAtDesc(Long jobId);

    @Query("SELECT m FROM Material m WHERE m.id = :id AND m.job.organization.id = :orgId")
    Optional<Material> findByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);

    @Query("SELECT COALESCE(SUM(m.total), 0) FROM Material m WHERE m.job.id = :jobId")
    BigDecimal calculateTotalCostByJobId(Long jobId);
}
