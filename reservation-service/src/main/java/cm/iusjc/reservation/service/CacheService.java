package cm.iusjc.reservation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CacheService - Redis disabled, using simple in-memory cache.
 * Cache annotations removed; Spring's simple cache handles @Cacheable elsewhere.
 */
@Service
@Slf4j
public class CacheService {

    public OccupancyStats getOccupancyStats(Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {
        return OccupancyStats.builder()
                .resourceId(resourceId)
                .totalReservations(0L)
                .totalMinutes(0L)
                .averageDuration(0.0)
                .occupancyRate(0.0)
                .build();
    }

    public List<TimeSlot> getFreeSlots(Long resourceId, LocalDateTime date) {
        return List.of();
    }

    public void cacheWithTtl(String key, Object value, Duration ttl) {
        log.debug("cacheWithTtl called (no-op, Redis disabled): key={}", key);
    }

    public <T> T getFromCache(String key, Class<T> type) {
        return null;
    }

    public void invalidateReservationCaches() {
        log.info("invalidateReservationCaches called (no-op, Redis disabled)");
    }

    public void invalidateStatsCaches() {
        log.info("invalidateStatsCaches called (no-op, Redis disabled)");
    }

    public void invalidateCacheForResource(Long resourceId) {
        log.debug("invalidateCacheForResource called (no-op, Redis disabled): resourceId={}", resourceId);
    }

    public void preloadPopularResources(List<Long> resourceIds) {
        log.debug("preloadPopularResources called (no-op, Redis disabled)");
    }

    public void cleanupExpiredCaches() {
        log.debug("cleanupExpiredCaches called (no-op, Redis disabled)");
    }

    @lombok.Builder
    @lombok.Data
    public static class OccupancyStats {
        private Long resourceId;
        private Long totalReservations;
        private Long totalMinutes;
        private Double averageDuration;
        private Double occupancyRate;
        private LocalDateTime calculatedAt;
    }

    @lombok.Builder
    @lombok.Data
    public static class TimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean available;
        private String description;
    }
}
