package com.nexus.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationDeduplicationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeduplicationService.class);

    private final Map<String, Instant> processedEventsCache = new ConcurrentHashMap<>();
    private final Duration ttl;

    public NotificationDeduplicationService(
            @Value("${app.notification.deduplication-ttl-minutes:60}") long ttlMinutes) {
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    /**
     * Checks if an event has already been processed.
     * Returns true if newly registered (not duplicate), false if already processed.
     */
    public boolean registerEvent(String eventKey) {
        Instant now = Instant.now();
        Instant existing = processedEventsCache.putIfAbsent(eventKey, now);

        if (existing == null) {
            return true; // First time seeing this event
        }

        // Check if existing key has expired
        if (Duration.between(existing, now).compareTo(ttl) > 0) {
            processedEventsCache.put(eventKey, now);
            return true;
        }

        log.warn("Deduplication filter: Duplicate event detected with key '{}'. Skipping dispatch.", eventKey);
        return false;
    }

    public int getCachedEventCount() {
        return processedEventsCache.size();
    }

    /**
     * Periodic cleanup of expired entries from in-memory cache every 15 minutes.
     */
    @Scheduled(fixedRate = 900000)
    public void evictExpiredKeys() {
        Instant threshold = Instant.now().minus(ttl);
        int initialSize = processedEventsCache.size();
        processedEventsCache.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
        int removed = initialSize - processedEventsCache.size();
        if (removed > 0) {
            log.info("Deduplication cache eviction completed: removed {} expired event keys, active cache size: {}",
                    removed, processedEventsCache.size());
        }
    }
}
