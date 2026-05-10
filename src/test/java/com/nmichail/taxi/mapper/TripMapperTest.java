package com.nmichail.taxi.mapper;

import com.nmichail.taxi.dto.TripResponse;
import com.nmichail.taxi.model.Trip;
import com.nmichail.taxi.model.TripStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TripMapperTest {

    @Test
    @DisplayName("toResponse EXPECT response id equals entity id")
    void toResponse_expect_responseIdMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.id = 100L;

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("toResponse EXPECT response passengerId equals entity passengerId")
    void toResponse_expect_responsePassengerIdMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.passengerId = 2L;

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.passengerId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toResponse EXPECT response driverId equals entity driverId")
    void toResponse_expect_responseDriverIdMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.driverId = 9L;

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.driverId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("toResponse EXPECT response status equals entity status")
    void toResponse_expect_responseStatusMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.status = TripStatus.COMPLETED;

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.status()).isEqualTo(TripStatus.COMPLETED);
    }

    @Test
    @DisplayName("toResponse EXPECT response origin equals entity origin")
    void toResponse_expect_responseOriginMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.origin = "A";

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.origin()).isEqualTo("A");
    }

    @Test
    @DisplayName("toResponse EXPECT response destination equals entity destination")
    void toResponse_expect_responseDestinationMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.destination = "B";

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.destination()).isEqualTo("B");
    }

    @Test
    @DisplayName("toResponse EXPECT response distanceKm equals entity distanceKm")
    void toResponse_expect_responseDistanceMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.distanceKm = new BigDecimal("3.50");

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.distanceKm()).isEqualByComparingTo(new BigDecimal("3.50"));
    }

    @Test
    @DisplayName("toResponse EXPECT response price equals entity price")
    void toResponse_expect_responsePriceMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.price = new BigDecimal("12.00");

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.price()).isEqualByComparingTo(new BigDecimal("12.00"));
    }

    @Test
    @DisplayName("toResponse EXPECT response rating equals entity rating")
    void toResponse_expect_responseRatingMatchesEntity() {
        Trip givenTrip = baseTrip();
        givenTrip.rating = 5;

        TripResponse whenResponse = TripMapper.toResponse(givenTrip);

        assertThat(whenResponse.rating()).isEqualTo(5);
    }

    private static Trip baseTrip() {
        Trip t = new Trip();
        t.id = 1L;
        t.passengerId = 1L;
        t.driverId = null;
        t.status = TripStatus.CREATED;
        t.origin = "o";
        t.destination = "d";
        t.distanceKm = BigDecimal.ONE;
        t.price = BigDecimal.TEN;
        t.rating = null;
        t.createdAt = Instant.parse("2020-01-01T00:00:00Z");
        t.updatedAt = Instant.parse("2020-01-01T00:00:00Z");
        return t;
    }
}