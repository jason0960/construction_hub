package com.constructionhub.repository;

import com.constructionhub.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByOrganizationIdAndDeletedAtIsNullOrderByNameAsc(Long organizationId);
    Optional<Client> findByIdAndOrganizationId(Long id, Long organizationId);
}
