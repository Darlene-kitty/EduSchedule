package cm.iusjc.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Préfixes pour les clés de cache
    private static final String TEACHER_AVAILABILITY_PREFIX = "teacher:availability:";
    private static final String TEACHER_STATS_PREFIX = "teacher:stats:";
    private static final String CONFLICT_CHECK_PREFIX = "conflict:check:";
    private static final String MULTI_SCHOOL_PREFIX = "multi:school:";
    
    // === Méthodes génériques ===
    
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cache set: {} with TTL: {}", key, ttl);
        } catch (Exception e) {
            log.warn("Failed to set cache key {}: {}", key, e.getMessage());
        }
    }
    
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isInstance(value)) {
                log.debug("Cache hit: {}", key);
                return type.cast(value);
            }
            log.debug("Cache miss: {}", key);
            return null;
        } catch (Exception e) {
            log.warn("Failed to get cache key {}: {}", key, e.getMessage());
            return null;
        }
    }
    
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Cache deleted: {}", key);
        } catch (Exception e) {
            log.warn("Failed to delete cache key {}: {}", key, e.getMessage());
        }
    }
    
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Cache deleted pattern: {} ({} keys)", pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("Failed to delete cache pattern {}: {}", pattern, e.getMessage());
        }
    }
    
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Failed to check cache key existence {}: {}", key, e.getMessage());
            return false;
        }
    }
    
    public void expire(String key, Duration ttl) {
        try {
            redisTemplate.expire(key, ttl);
            log.debug("Cache expiration set: {} with TTL: {}", key, ttl);
        } catch (Exception e) {
            log.warn("Failed to set expiration for cache key {}: {}", key, e.getMessage());
        }
    }
    
    // === Méthodes spécifiques aux disponibilités ===
    
    public void cacheTeacherAvailabilities(Long teacherId, Object availabilities) {
        String key = TEACHER_AVAILABILITY_PREFIX + teacherId;
        set(key, availabilities, Duration.ofMinutes(5));
    }
    
    public <T> T getTeacherAvailabilities(Long teacherId, Class<T> type) {
        String key = TEACHER_AVAILABILITY_PREFIX + teacherId;
        return get(key, type);
    }
    
    public void invalidateTeacherAvailabilities(Long teacherId) {
        String pattern = TEACHER_AVAILABILITY_PREFIX + teacherId + "*";
        deletePattern(pattern);
    }
    
    // === Méthodes spécifiques aux statistiques ===
    
    public void cacheTeacherStats(Long teacherId, Object stats) {
        String key = TEACHER_STATS_PREFIX + teacherId;
        set(key, stats, Duration.ofMinutes(10));
    }
    
    public <T> T getTeacherStats(Long teacherId, Class<T> type) {
        String key = TEACHER_STATS_PREFIX + teacherId;
        return get(key, type);
    }
    
    public void invalidateTeacherStats(Long teacherId) {
        String key = TEACHER_STATS_PREFIX + teacherId;
        delete(key);
    }
    
    // === Méthodes spécifiques aux vérifications de conflits ===
    
    public void cacheConflictCheck(String checkId, boolean hasConflict) {
        String key = CONFLICT_CHECK_PREFIX + checkId;
        set(key, hasConflict, Duration.ofMinutes(1));
    }
    
    public Boolean getConflictCheck(String checkId) {
        String key = CONFLICT_CHECK_PREFIX + checkId;
        return get(key, Boolean.class);
    }
    
    public void invalidateConflictChecks() {
        String pattern = CONFLICT_CHECK_PREFIX + "*";
        deletePattern(pattern);
    }
    
    // === Méthodes spécifiques aux assignations multi-écoles ===
    
    public void cacheMultiSchoolTeachers(Object teachers) {
        String key = MULTI_SCHOOL_PREFIX + "teachers";
        set(key, teachers, Duration.ofMinutes(30));
    }
    
    public <T> T getMultiSchoolTeachers(Class<T> type) {
        String key = MULTI_SCHOOL_PREFIX + "teachers";
        return get(key, type);
    }
    
    public void invalidateMultiSchoolCache() {
        String pattern = MULTI_SCHOOL_PREFIX + "*";
        deletePattern(pattern);
    }
    
    // === Méthodes de maintenance ===
    
    public void warmUpCache() {
        log.info("Starting cache warm-up...");
        // TODO: Implémenter le préchauffage du cache avec les données les plus utilisées
        log.info("Cache warm-up completed");
    }
    
    public void clearAllCache() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.info("All cache cleared");
        } catch (Exception e) {
            log.error("Failed to clear all cache: {}", e.getMessage());
        }
    }
    
    public long getCacheSize() {
        try {
            return redisTemplate.getConnectionFactory().getConnection().dbSize();
        } catch (Exception e) {
            log.warn("Failed to get cache size: {}", e.getMessage());
            return -1;
        }
    }
    
    public void evictExpiredKeys() {
        log.debug("Evicting expired cache keys...");
        // Redis gère automatiquement l'éviction, mais on peut forcer un nettoyage
        try {
            redisTemplate.getConnectionFactory().getConnection().eval(
                "return redis.call('eval', \"for i=1,#KEYS do redis.call('expire',KEYS[i],0) end\", 0)".getBytes(),
                org.springframework.data.redis.connection.ReturnType.INTEGER,
                0,
                new byte[0][]
            );
        } catch (Exception e) {
            log.warn("Failed to evict expired keys: {}", e.getMessage());
        }
    }
    
    // === Méthodes de monitoring ===
    
    public CacheStats getCacheStats() {
        try {
            long size = getCacheSize();
            // TODO: Implémenter des métriques plus détaillées (hit rate, miss rate, etc.)
            return new CacheStats(size, 0, 0, 0);
        } catch (Exception e) {
            log.warn("Failed to get cache stats: {}", e.getMessage());
            return new CacheStats(0, 0, 0, 0);
        }
    }
    
    // Classe pour les statistiques du cache
    public static class CacheStats {
        private final long totalKeys;
        private final long hitCount;
        private final long missCount;
        private final long evictionCount;
        
        public CacheStats(long totalKeys, long hitCount, long missCount, long evictionCount) {
            this.totalKeys = totalKeys;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.evictionCount = evictionCount;
        }
        
        public long getTotalKeys() { return totalKeys; }
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public long getEvictionCount() { return evictionCount; }
        
        public double getHitRate() {
            long total = hitCount + missCount;
            return total > 0 ? (double) hitCount / total : 0.0;
        }
    }
}