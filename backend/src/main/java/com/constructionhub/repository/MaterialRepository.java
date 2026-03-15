package com.constructionhub.repository;

import com.constructionhub.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByJobIdOrderByCreatedAtDesc(Long jobId);

    @Query("SELECT COALESCE(SUM(m.total), 0) FROM Material m WHERE m.job.id = :jobId")
    BigDecimal calculateTotalCostByJobId(Long jobId);
}
