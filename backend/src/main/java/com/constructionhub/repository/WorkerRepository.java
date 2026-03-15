package com.constructionhub.repository;

import com.constructionhub.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByOrganizationIdOrderByLastNameAsc(Long organizationId);
    Optional<Worker> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<Worker> findByUserId(Long userId);
    List<Worker> findByOrganizationIdAndStatusOrderByLastNameAsc(Long organizationId, com.constructionhub.entity.WorkerStatus status);
}
