package com.nmichail.taxi.mapper;

import com.nmichail.taxi.dto.PassengerResponse;
import com.nmichail.taxi.model.Passenger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PassengerMapperTest {

    @Test
    @DisplayName("toResponse EXPECT response id equals entity id")
    void toResponse_expect_responseIdMatchesEntity() {
        Passenger givenPassenger = new Passenger();
        givenPassenger.id = 42L;
        givenPassenger.name = "n";
        givenPassenger.email = "e@e";
        givenPassenger.phone = "p";
        givenPassenger.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        PassengerResponse whenResponse = PassengerMapper.toResponse(givenPassenger);

        assertThat(whenResponse.id()).isEqualTo(42L);
    }

    @Test
    @DisplayName("toResponse EXPECT response name equals entity name")
    void toResponse_expect_responseNameMatchesEntity() {
        Passenger givenPassenger = new Passenger();
        givenPassenger.id = 1L;
        givenPassenger.name = "Bob";
        givenPassenger.email = "e@e";
        givenPassenger.phone = "p";
        givenPassenger.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        PassengerResponse whenResponse = PassengerMapper.toResponse(givenPassenger);

        assertThat(whenResponse.name()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("toResponse EXPECT response email equals entity email")
    void toResponse_expect_responseEmailMatchesEntity() {
        Passenger givenPassenger = new Passenger();
        givenPassenger.id = 1L;
        givenPassenger.name = "n";
        givenPassenger.email = "x@y.z";
        givenPassenger.phone = "p";
        givenPassenger.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        PassengerResponse whenResponse = PassengerMapper.toResponse(givenPassenger);

        assertThat(whenResponse.email()).isEqualTo("x@y.z");
    }

    @Test
    @DisplayName("toResponse EXPECT response phone equals entity phone")
    void toResponse_expect_responsePhoneMatchesEntity() {
        Passenger givenPassenger = new Passenger();
        givenPassenger.id = 1L;
        givenPassenger.name = "n";
        givenPassenger.email = "e@e";
        givenPassenger.phone = "999";
        givenPassenger.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        PassengerResponse whenResponse = PassengerMapper.toResponse(givenPassenger);

        assertThat(whenResponse.phone()).isEqualTo("999");
    }
}