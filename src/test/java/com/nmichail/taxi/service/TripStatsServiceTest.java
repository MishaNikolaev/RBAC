package com.nmichail.taxi.service;

import com.nmichail.taxi.dto.DailyTripStatsResponse;
import com.nmichail.taxi.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripStatsServiceTest {

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripStatsService tripStatsService;

    @Test
    @DisplayName("getDailyTripStats EXPECT tripsCount equals repository value")
    void getDailyTripStats_expect_tripsCountFromRepository() {
        LocalDate givenDate = LocalDate.of(2026, 5, 10);
        when(tripRepository.countByCreatedAtBetween(any(), any())).thenReturn(7L);
        when(tripRepository.averagePriceByCreatedAtBetween(any(), any())).thenReturn(BigDecimal.ONE);

        DailyTripStatsResponse whenStats = tripStatsService.getDailyTripStats(givenDate);

        assertThat(whenStats.tripsCount()).isEqualTo(7L);
    }

    @Test
    @DisplayName("getDailyTripStats EXPECT averagePrice zero when repository returns null")
    void getDailyTripStats_expect_averagePriceZeroWhenRepositoryReturnsNull() {
        LocalDate givenDate = LocalDate.of(2026, 5, 10);
        when(tripRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(tripRepository.averagePriceByCreatedAtBetween(any(), any())).thenReturn(null);

        DailyTripStatsResponse whenStats = tripStatsService.getDailyTripStats(givenDate);

        assertThat(whenStats.averagePrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getDailyTripStats EXPECT averagePrice equals repository value")
    void getDailyTripStats_expect_averagePriceFromRepository() {
        LocalDate givenDate = LocalDate.of(2026, 5, 10);
        when(tripRepository.countByCreatedAtBetween(any(), any())).thenReturn(1L);
        when(tripRepository.averagePriceByCreatedAtBetween(any(), any())).thenReturn(new BigDecimal("4.25"));

        DailyTripStatsResponse whenStats = tripStatsService.getDailyTripStats(givenDate);

        assertThat(whenStats.averagePrice()).isEqualByComparingTo(new BigDecimal("4.25"));
    }

    @Test
    @DisplayName("getDailyTripStats EXPECT response date equals requested date")
    void getDailyTripStats_expect_responseDateMatchesInput() {
        LocalDate givenDate = LocalDate.of(2026, 3, 1);
        when(tripRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(tripRepository.averagePriceByCreatedAtBetween(any(), any())).thenReturn(BigDecimal.ZERO);

        DailyTripStatsResponse whenStats = tripStatsService.getDailyTripStats(givenDate);

        assertThat(whenStats.date()).isEqualTo(givenDate);
    }
}