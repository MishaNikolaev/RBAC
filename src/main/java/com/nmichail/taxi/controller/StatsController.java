package com.nmichail.taxi.controller;

import com.nmichail.taxi.dto.DailyTripStatsResponse;
import com.nmichail.taxi.service.TripStatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Statistics", description = "Агрегаты по поездкам")
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final TripStatsService tripStatsService;

    @GetMapping("/stats/trips/daily")
    public DailyTripStatsResponse dailyTripStats(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tripStatsService.getDailyTripStats(date);
    }
}