package com.constructionhub.repository;

import com.constructionhub.entity.Permit;
import com.constructionhub.entity.PermitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PermitRepository extends JpaRepository<Permit, Long> {
    List<Permit> findByJobIdOrderByExpirationDateAsc(Long jobId);

    @Query("SELECT COALESCE(SUM(p.fee), 0) FROM Permit p WHERE p.job.id = :jobId")
    BigDecimal calculateTotalFeesByJobId(Long jobId);

    @Query("SELECT p FROM Permit p WHERE p.status = :status AND p.expirationDate BETWEEN :start AND :end")
    List<Permit> findPermitsExpiringBetween(PermitStatus status, LocalDate start, LocalDate end);

    @Query("SELECT p FROM Permit p WHERE p.status = 'ACTIVE' AND p.expirationDate <= :date")
    List<Permit> findExpiredPermits(LocalDate date);
}
