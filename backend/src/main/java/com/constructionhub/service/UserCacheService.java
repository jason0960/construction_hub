package com.constructionhub.service;

import com.constructionhub.entity.User;
import com.constructionhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Cached wrapper around UserRepository lookups.
 * Used by JwtAuthenticationFilter to avoid hitting the DB on every request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#userId")
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void evict(Long userId) {
        log.debug("User cache evicted: userId={}", userId);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void evictAll() {
        log.info("User cache fully cleared");
    }
}
