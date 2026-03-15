package com.constructionhub.repository;

import com.constructionhub.entity.Job;
import com.constructionhub.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    List<Job> findByOrganizationIdAndDeletedAtIsNullOrderByUpdatedAtDesc(Long organizationId);
    List<Job> findByOrganizationIdAndStatusAndDeletedAtIsNull(Long organizationId, JobStatus status);
    Optional<Job> findByIdAndOrganizationId(Long id, Long organizationId);
}
