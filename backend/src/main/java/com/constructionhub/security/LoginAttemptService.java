package com.constructionhub.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks failed login attempts per email to prevent credential-stuffing and brute-force attacks.
 * After {@code MAX_ATTEMPTS} failures within {@code WINDOW_MS}, further login attempts for that
 * email are blocked until the window expires.
 */
@Component
@Slf4j
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 900_000; // 15 minutes

    private final Map<String, AttemptBucket> attempts = new ConcurrentHashMap<>();

    /**
     * @return true if the email is currently blocked due to too many failed attempts.
     */
    public boolean isBlocked(String email) {
        String key = normalizeEmail(email);
        AttemptBucket bucket = attempts.get(key);
        if (bucket == null) return false;
        if (System.currentTimeMillis() - bucket.windowStart > WINDOW_MS) {
            attempts.remove(key);
            return false;
        }
        boolean blocked = bucket.count.get() >= MAX_ATTEMPTS;
        if (blocked) {
            log.warn("Login blocked due to too many failures: email={} attempts={}", key, bucket.count.get());
        }
        return blocked;
    }

    /**
     * Record a failed login attempt for the given email.
     */
    public void recordFailure(String email) {
        String key = normalizeEmail(email);
        attempts.compute(key, (k, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new AttemptBucket(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });
        AttemptBucket bucket = attempts.get(key);
        log.warn("Failed login attempt: email={} attemptCount={}", key, bucket != null ? bucket.count.get() : 1);
    }

    /**
     * Clear failed attempts on successful login.
     */
    public void recordSuccess(String email) {
        String key = normalizeEmail(email);
        if (attempts.containsKey(key)) {
            log.info("Successful login after previous failures: email={}", key);
        }
        attempts.remove(key);
    }

    /**
     * Evict expired entries. Called periodically by the rate limit filter's scheduled task.
     */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(e -> now - e.getValue().windowStart > WINDOW_MS);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.toLowerCase().trim();
    }

    private static class AttemptBucket {
        final long windowStart;
        final AtomicInteger count;

        AttemptBucket(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
