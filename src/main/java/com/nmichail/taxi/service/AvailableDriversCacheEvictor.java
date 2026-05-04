package com.nmichail.taxi.service;

import com.nmichail.taxi.config.RedisCacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class AvailableDriversCacheEvictor {

    @CacheEvict(cacheNames = RedisCacheConfig.AVAILABLE_DRIVERS_CACHE, allEntries = true)
    public void invalidate() {
    }
}