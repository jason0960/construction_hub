package com.constructionhub.repository;

import com.constructionhub.entity.CrewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CrewAssignmentRepository extends JpaRepository<CrewAssignment, Long> {
    List<CrewAssignment> findByJobId(Long jobId);
    List<CrewAssignment> findByWorkerId(Long workerId);
    Optional<CrewAssignment> findByJobIdAndWorkerId(Long jobId, Long workerId);

    @Query("SELECT ca FROM CrewAssignment ca WHERE ca.id = :id AND ca.job.organization.id = :orgId")
    Optional<CrewAssignment> findByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);

    @Query("SELECT ca FROM CrewAssignment ca JOIN FETCH ca.job WHERE ca.worker.id = :workerId AND ca.status IN ('ASSIGNED', 'ACTIVE')")
    List<CrewAssignment> findActiveAssignmentsByWorkerId(Long workerId);

    @Query("SELECT ca FROM CrewAssignment ca JOIN FETCH ca.worker WHERE ca.job.id = :jobId AND ca.status IN ('ASSIGNED', 'ACTIVE')")
    List<CrewAssignment> findActiveAssignmentsByJobId(Long jobId);
}
