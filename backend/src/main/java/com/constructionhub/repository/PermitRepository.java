package com.constructionhub.repository;

import com.constructionhub.entity.Permit;
import com.constructionhub.entity.PermitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PermitRepository extends JpaRepository<Permit, Long> {
    List<Permit> findByJobIdOrderByExpirationDateAsc(Long jobId);

    @Query("SELECT p FROM Permit p WHERE p.id = :id AND p.job.organization.id = :orgId")
    Optional<Permit> findByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);

    @Query("SELECT COALESCE(SUM(p.fee), 0) FROM Permit p WHERE p.job.id = :jobId")
    BigDecimal calculateTotalFeesByJobId(Long jobId);

    @Query("SELECT p FROM Permit p WHERE p.status = :status AND p.expirationDate BETWEEN :start AND :end")
    List<Permit> findPermitsExpiringBetween(PermitStatus status, LocalDate start, LocalDate end);

    @Query("SELECT p FROM Permit p WHERE p.status = 'ACTIVE' AND p.expirationDate <= :date")
    List<Permit> findExpiredPermits(LocalDate date);
}
