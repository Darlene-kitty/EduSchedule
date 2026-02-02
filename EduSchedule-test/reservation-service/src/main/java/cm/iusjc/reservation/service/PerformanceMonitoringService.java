package cm.iusjc.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringService {
    
    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    
    // Compteurs de performance
    private final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private final Map<String, Timer> operationTimers = new ConcurrentHashMap<>();
    
    // Métriques de cache
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // Seuils d'alerte
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    private static final long VERY_SLOW_QUERY_THRESHOLD_MS = 5000;
    
    public void recordOperation(String operationName, long durationMs) {
        // Enregistrer le compteur
        operationCounts.computeIfAbsent(operationName, k -> new AtomicLong(0)).incrementAndGet();
        
        // Enregistrer le timer
        Timer timer = operationTimers.computeIfAbsent(operationName, 
            k -> Timer.builder("reservation.operation")
                    .tag("operation", operationName)
                    .register(meterRegistry));
        
        timer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // Alertes pour les opérations lentes
        if (durationMs > VERY_SLOW_QUERY_THRESHOLD_MS) {
            log.error("🚨 VERY SLOW OPERATION: {} took {}ms", operationName, durationMs);
        } else if (durationMs > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("⚠️ SLOW OPERATION: {} took {}ms", operationName, durationMs);
        }
    }
    
    public void recordCacheHit(String cacheName) {
        cacheHits.incrementAndGet();
        meterRegistry.counter("reservation.cache.hits", "cache", cacheName).increment();
    }
    
    public void recordCacheMiss(String cacheName) {
        cacheMisses.incrementAndGet();
        meterRegistry.counter("reservation.cache.misses", "cache", cacheName).increment();
    }
    
    // Rapport de performance périodique
    @Scheduled(fixedRate = 300000) // Toutes les 5 minutes
    public void generatePerformanceReport() {
        log.info("=== PERFORMANCE REPORT ===");
        
        // Statistiques des opérations
        operationCounts.forEach((operation, count) -> {
            Timer timer = operationTimers.get(operation);
            if (timer != null) {
                double avgMs = timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
                double maxMs = timer.max(java.util.concurrent.TimeUnit.MILLISECONDS);
                log.info("Operation {}: {} calls, avg: {:.2f}ms, max: {:.2f}ms", 
                        operation, count.get(), avgMs, maxMs);
            }
        });
        
        // Statistiques de cache
        long totalCacheOperations = cacheHits.get() + cacheMisses.get();
        if (totalCacheOperations > 0) {
            double hitRate = (double) cacheHits.get() / totalCacheOperations * 100;
            log.info("Cache hit rate: {:.2f}% ({} hits, {} misses)", 
                    hitRate, cacheHits.get(), cacheMisses.get());
        }
        
        // Informations sur les caches
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.info("Cache {}: active", cacheName);
            }
        });
        
        log.info("=== END PERFORMANCE REPORT ===");
    }
    
    // Nettoyage périodique des métriques anciennes
    @Scheduled(fixedRate = 3600000) // Toutes les heures
    public void cleanupOldMetrics() {
        // Réinitialiser les compteurs pour éviter l'accumulation
        operationCounts.clear();
        cacheHits.set(0);
        cacheMisses.set(0);
        
        log.info("Cleaned up old performance metrics");
    }
    
    // Méthodes pour obtenir les métriques actuelles
    public Map<String, Object> getCurrentMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Métriques d'opérations
        Map<String, Object> operations = new HashMap<>();
        operationCounts.forEach((operation, count) -> {
            Timer timer = operationTimers.get(operation);
            if (timer != null) {
                Map<String, Object> operationMetrics = new HashMap<>();
                operationMetrics.put("count", count.get());
                operationMetrics.put("avgMs", timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
                operationMetrics.put("maxMs", timer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
                operations.put(operation, operationMetrics);
            }
        });
        metrics.put("operations", operations);
        
        // Métriques de cache
        Map<String, Object> cache = new HashMap<>();
        cache.put("hits", cacheHits.get());
        cache.put("misses", cacheMisses.get());
        long total = cacheHits.get() + cacheMisses.get();
        cache.put("hitRate", total > 0 ? (double) cacheHits.get() / total * 100 : 0);
        metrics.put("cache", cache);
        
        return metrics;
    }
    
    // Alertes de performance
    public void checkPerformanceAlerts() {
        // Vérifier le taux de cache hit
        long totalCacheOps = cacheHits.get() + cacheMisses.get();
        if (totalCacheOps > 100) { // Seulement si on a assez de données
            double hitRate = (double) cacheHits.get() / totalCacheOps * 100;
            if (hitRate < 70) { // Seuil d'alerte : moins de 70% de hit rate
                log.warn("🚨 LOW CACHE HIT RATE: {:.2f}% - Consider cache optimization", hitRate);
            }
        }
        
        // Vérifier les opérations lentes
        operationTimers.forEach((operation, timer) -> {
            double avgMs = timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
            if (avgMs > SLOW_QUERY_THRESHOLD_MS) {
                log.warn("🚨 SLOW AVERAGE OPERATION: {} avg: {:.2f}ms", operation, avgMs);
            }
        });
    }
}