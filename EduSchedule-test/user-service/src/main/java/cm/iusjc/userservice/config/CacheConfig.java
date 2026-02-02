package cm.iusjc.userservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
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

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Sérialiseurs
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuration par défaut
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL par défaut: 10 minutes
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // Configurations spécifiques par cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Cache des disponibilités des enseignants (5 minutes)
        cacheConfigurations.put("teacherAvailability", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Cache des créneaux disponibles (3 minutes)
        cacheConfigurations.put("availableSlots", defaultConfig.entryTtl(Duration.ofMinutes(3)));
        
        // Cache des assignations multi-écoles (15 minutes)
        cacheConfigurations.put("teacherAssignments", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Cache des enseignants multi-écoles (30 minutes)
        cacheConfigurations.put("multiSchoolTeachers", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Cache des vérifications de disponibilité (1 minute)
        cacheConfigurations.put("availabilityCheck", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // Cache des statistiques (10 minutes)
        cacheConfigurations.put("teacherStats", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Cache des utilisateurs (20 minutes)
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        
        // Cache des écoles (1 heure)
        cacheConfigurations.put("schools", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}