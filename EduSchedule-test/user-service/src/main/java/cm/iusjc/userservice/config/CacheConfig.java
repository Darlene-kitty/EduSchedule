package cm.iusjc.userservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        // Do NOT call afterPropertiesSet() here — it eagerly opens a Redis connection
        // and will crash the context if Redis is unavailable at startup.
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            // Test Redis connectivity before wiring it as cache manager
            connectionFactory.getConnection().ping();

            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                            .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                            .fromSerializer(new GenericJackson2JsonRedisSerializer()));

            Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
            cacheConfigurations.put("teacherAvailability", defaultConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigurations.put("availableSlots", defaultConfig.entryTtl(Duration.ofMinutes(3)));
            cacheConfigurations.put("teacherAssignments", defaultConfig.entryTtl(Duration.ofMinutes(15)));
            cacheConfigurations.put("multiSchoolTeachers", defaultConfig.entryTtl(Duration.ofMinutes(30)));
            cacheConfigurations.put("availabilityCheck", defaultConfig.entryTtl(Duration.ofMinutes(1)));
            cacheConfigurations.put("teacherStats", defaultConfig.entryTtl(Duration.ofMinutes(10)));
            cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(20)));
            cacheConfigurations.put("schools", defaultConfig.entryTtl(Duration.ofHours(1)));
            cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofHours(1)));

            log.info("Redis available - using RedisCacheManager");
            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .build();

        } catch (Exception e) {
            log.warn("Redis unavailable ({}), falling back to in-memory cache", e.getMessage());
            return new ConcurrentMapCacheManager(
                    "teacherAvailability", "availableSlots", "teacherAssignments",
                    "multiSchoolTeachers", "availabilityCheck", "teacherStats",
                    "users", "schools", "roles"
            );
        }
    }
}