package com.nmichail.taxi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisCacheConfig {

    public static final String AVAILABLE_DRIVERS_CACHE = "availableDrivers";

    private static RedisSerializer<Object> redisValueSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();
    }

    @Bean
    RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(redisValueSerializer()))
                .entryTtl(Duration.ofMinutes(5));
    }

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory cf, RedisCacheConfiguration redisCacheConfiguration) {
        return RedisCacheManager.builder(cf).cacheDefaults(redisCacheConfiguration).build();
    }
}