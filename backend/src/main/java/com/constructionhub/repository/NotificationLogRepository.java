package com.constructionhub.repository;

import com.constructionhub.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByUserIdOrderBySentAtDesc(Long userId);
    List<NotificationLog> findByUserIdAndReadAtIsNullOrderBySentAtDesc(Long userId);
    long countByUserIdAndReadAtIsNull(Long userId);
}
