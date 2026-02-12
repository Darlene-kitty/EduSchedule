package cm.iusjc.reservation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache pour les statistiques d'occupation
    @Cacheable(value = "occupancyStats", key = "#resourceId + '_' + #startDate + '_' + #endDate")
    public OccupancyStats getOccupancyStats(Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {
        // Cette méthode sera appelée seulement si les données ne sont pas en cache
        log.debug("Calculating occupancy stats for resource {} from {} to {}", resourceId, startDate, endDate);
        
        // Logique de calcul des statistiques d'occupation
        // (sera implémentée avec les données réelles)
        return OccupancyStats.builder()
                .resourceId(resourceId)
                .totalReservations(0L)
                .totalMinutes(0L)
                .averageDuration(0.0)
                .occupancyRate(0.0)
                .build();
    }
    
    // Cache pour les créneaux libres
    @Cacheable(value = "freeSlots", key = "#resourceId + '_' + #date")
    public List<TimeSlot> getFreeSlots(Long resourceId, LocalDateTime date) {
        log.debug("Calculating free slots for resource {} on {}", resourceId, date);
        
        // Logique de calcul des créneaux libres
        // (sera implémentée avec les données réelles)
        return List.of();
    }
    
    // Cache générique avec TTL personnalisé
    public void cacheWithTtl(String key, Object value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl.toSeconds(), TimeUnit.SECONDS);
            log.debug("Cached value with key: {} for {} seconds", key, ttl.toSeconds());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize value for caching: {}", e.getMessage());
        }
    }
    
    // Récupération depuis le cache générique
    @SuppressWarnings("unchecked")
    public <T> T getFromCache(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached.toString(), type);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize cached value for key {}: {}", key, e.getMessage());
        }
        return null;
    }
    
    // Invalidation de cache
    @CacheEvict(value = {"conflictQueries", "reservationsByResource", "freeSlots"}, allEntries = true)
    public void invalidateReservationCaches() {
        log.info("Invalidated all reservation-related caches");
    }
    
    @CacheEvict(value = "occupancyStats", allEntries = true)
    public void invalidateStatsCaches() {
        log.info("Invalidated all statistics caches");
    }
    
    // Invalidation spécifique par ressource
    public void invalidateCacheForResource(Long resourceId) {
        String pattern = "*" + resourceId + "*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("Invalidated caches for resource: {}", resourceId);
    }
    
    // Préchargement du cache pour les ressources populaires
    public void preloadPopularResources(List<Long> resourceIds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        
        resourceIds.parallelStream().forEach(resourceId -> {
            try {
                // Précharger les créneaux libres pour aujourd'hui
                getFreeSlots(resourceId, now);
                
                // Précharger les statistiques pour la semaine
                getOccupancyStats(resourceId, now.minusDays(7), now);
                
                log.debug("Preloaded cache for resource: {}", resourceId);
            } catch (Exception e) {
                log.error("Failed to preload cache for resource {}: {}", resourceId, e.getMessage());
            }
        });
        
        log.info("Preloaded cache for {} popular resources", resourceIds.size());
    }
    
    // Nettoyage des caches expirés
    public void cleanupExpiredCaches() {
        // Cette méthode peut être appelée périodiquement pour nettoyer les caches
        log.info("Cleaning up expired caches");
        
        // Redis gère automatiquement l'expiration, mais on peut ajouter une logique personnalisée ici
        // Par exemple, supprimer les caches de ressources inactives
    }
    
    // Classes pour les données cachées
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