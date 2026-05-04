package com.nmichail.taxi.service;

import com.nmichail.taxi.config.RedisCacheConfig;
import com.nmichail.taxi.dto.DriverResponse;
import com.nmichail.taxi.mapper.DriverMapper;
import com.nmichail.taxi.model.DriverStatus;
import com.nmichail.taxi.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedAvailableDriversService {

    private final DriverRepository driverRepository;

    @Cacheable(cacheNames = RedisCacheConfig.AVAILABLE_DRIVERS_CACHE, key = "'all'")
    public List<DriverResponse> listAvailable() {
        return driverRepository.findByStatusOrderById(DriverStatus.AVAILABLE).stream()
                .map(DriverMapper::toResponse)
                .toList();
    }
}