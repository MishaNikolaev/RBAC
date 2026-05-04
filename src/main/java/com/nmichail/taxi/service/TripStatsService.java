package com.nmichail.taxi.service;

import com.nmichail.taxi.dto.DailyTripStatsResponse;
import com.nmichail.taxi.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class TripStatsService {

    private final TripRepository tripRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public TripStatsService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public DailyTripStatsResponse getDailyTripStats(LocalDate date) {
        Instant start = date.atStartOfDay(zoneId).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zoneId).toInstant();

        long tripsCount = tripRepository.countByCreatedAtBetween(start, end);
        BigDecimal avgPrice = tripRepository.averagePriceByCreatedAtBetween(start, end);
        return new DailyTripStatsResponse(date, tripsCount, avgPrice == null ? BigDecimal.ZERO : avgPrice);
    }
}