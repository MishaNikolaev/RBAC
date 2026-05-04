package com.nmichail.taxi.dto;

import com.nmichail.taxi.model.TripStatus;

import java.math.BigDecimal;

public record TripResponse(
        long id,
        long passengerId,
        Long driverId,
        TripStatus status,
        String origin,
        String destination,
        BigDecimal distanceKm,
        BigDecimal price,
        Integer rating
) {
}