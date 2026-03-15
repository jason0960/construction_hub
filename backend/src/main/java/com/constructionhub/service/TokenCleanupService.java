package com.constructionhub.service;

import com.constructionhub.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Runs every hour to clean up expired refresh tokens.
     * ShedLock ensures only one instance runs in a distributed environment.
     */
    @Scheduled(cron = "0 0 * * * *")
    @SchedulerLock(name = "cleanupExpiredTokens", lockAtMostFor = "PT10M", lockAtLeastFor = "PT5M")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired refresh tokens", deleted);
        }
    }
}
