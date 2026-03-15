package com.constructionhub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for auth endpoints.
 * Limits requests per minute per IP on /api/auth/login, /api/auth/register, and /api/auth/refresh.
 * Expired buckets are evicted every 5 minutes to prevent memory leaks.
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, RateBucket> buckets = new ConcurrentHashMap<>();
    private final LoginAttemptService loginAttemptService;

    public RateLimitFilter(
            @Value("${app.rate-limit.max-requests:10}") int maxRequests,
            LoginAttemptService loginAttemptService) {
        this.maxRequests = maxRequests;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/api/auth/login")
                && !path.equals("/api/auth/register")
                && !path.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String key = clientIp + ":" + request.getServletPath();

        RateBucket bucket = buckets.compute(key, (k, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateBucket(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (bucket.count.get() > maxRequests) {
            long retryAfterSeconds = Math.max(1, (WINDOW_MS - (System.currentTimeMillis() - bucket.windowStart)) / 1000);
            log.warn("Rate limit exceeded: ip={} path={} count={} retryAfter={}s", clientIp, request.getServletPath(), bucket.count.get(), retryAfterSeconds);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Evict expired buckets every 5 minutes to prevent unbounded memory growth. */
    @Scheduled(fixedRate = 300_000)
    public void evictExpiredBuckets() {
        long now = System.currentTimeMillis();
        int before = buckets.size();
        buckets.entrySet().removeIf(e -> now - e.getValue().windowStart > WINDOW_MS);
        loginAttemptService.evictExpired();
        int evicted = before - buckets.size();
        if (evicted > 0) {
            log.info("Rate limit bucket eviction: evicted={} remaining={}", evicted, buckets.size());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateBucket {
        final long windowStart;
        final AtomicInteger count;

        RateBucket(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
