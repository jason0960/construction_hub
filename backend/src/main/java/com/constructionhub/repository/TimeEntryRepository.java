package com.constructionhub.repository;

import com.constructionhub.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByJobIdOrderByEntryDateDesc(Long jobId);
    List<TimeEntry> findByWorkerIdOrderByEntryDateDesc(Long workerId);
    List<TimeEntry> findByJobIdAndWorkerId(Long jobId, Long workerId);

    @Query("SELECT COALESCE(SUM(te.hours * te.worker.hourlyRate), 0) FROM TimeEntry te WHERE te.job.id = :jobId")
    BigDecimal calculateLaborCostByJobId(Long jobId);

    @Query("SELECT COALESCE(SUM(te.hours), 0) FROM TimeEntry te WHERE te.job.id = :jobId AND te.worker.id = :workerId")
    BigDecimal calculateHoursByJobAndWorker(Long jobId, Long workerId);
}
