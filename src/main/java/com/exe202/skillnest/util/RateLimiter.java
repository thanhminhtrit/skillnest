package com.exe202.skillnest.util;

import com.exe202.skillnest.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private final Map<String, LocalDateTime> lastRequestMap = new ConcurrentHashMap<>();

    private static final int COOLDOWN_SECONDS = 10; // 10 seconds between AI matching calls per user

    /**
     * Check if user can make request. Throws exception if too frequent.
     * @param key unique key, e.g. "ai-matching:userId"
     */
    public void checkRateLimit(String key) {
        LocalDateTime lastRequest = lastRequestMap.get(key);
        LocalDateTime now = LocalDateTime.now();

        if (lastRequest != null && lastRequest.plusSeconds(COOLDOWN_SECONDS).isAfter(now)) {
            long waitSeconds = java.time.Duration.between(now, lastRequest.plusSeconds(COOLDOWN_SECONDS)).getSeconds();
            throw new BadRequestException(
                    "Too many AI matching requests. Please wait " + (waitSeconds + 1) + " seconds before trying again.");
        }

        lastRequestMap.put(key, now);

        // Clean up old entries (simple cleanup, runs occasionally)
        if (lastRequestMap.size() > 10000) {
            LocalDateTime cutoff = now.minusMinutes(5);
            lastRequestMap.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        }
    }
}
