package com.nmichail.taxi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTripStatsResponse(LocalDate date, long tripsCount, BigDecimal averagePrice) {
}