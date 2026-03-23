package cm.iusjc.reservation.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // TTL par défaut : 30 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Configuration spécifique par cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Cache pour les conflits - courte durée car données critiques
        cacheConfigurations.put("conflictQueries", 
                defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Cache pour les réservations par ressource - durée moyenne
        cacheConfigurations.put("reservationsByResource", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Cache pour les statistiques - longue durée
        cacheConfigurations.put("reservationStats", 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Cache pour les créneaux libres - courte durée
        cacheConfigurations.put("freeSlots", 
                defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Cache pour les réservations à venir - durée moyenne
        cacheConfigurations.put("upcomingReservations", 
                defaultConfig.entryTtl(Duration.ofMinutes(20)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}