package com.nmichail.taxi.mapper;

import com.nmichail.taxi.dto.DriverResponse;
import com.nmichail.taxi.model.Driver;
import com.nmichail.taxi.model.DriverStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DriverMapperTest {

    @Test
    @DisplayName("toResponse EXPECT response id equals entity id")
    void toResponse_expect_responseIdMatchesEntity() {
        Driver givenDriver = new Driver();
        givenDriver.id = 7L;
        givenDriver.name = "n";
        givenDriver.email = "e@e";
        givenDriver.phone = "p";
        givenDriver.licenseNumber = "L";
        givenDriver.status = DriverStatus.AVAILABLE;
        givenDriver.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        DriverResponse whenResponse = DriverMapper.toResponse(givenDriver);

        assertThat(whenResponse.id()).isEqualTo(7L);
    }

    @Test
    @DisplayName("toResponse EXPECT response name equals entity name")
    void toResponse_expect_responseNameMatchesEntity() {
        Driver givenDriver = new Driver();
        givenDriver.id = 1L;
        givenDriver.name = "Alice";
        givenDriver.email = "e@e";
        givenDriver.phone = "p";
        givenDriver.licenseNumber = "L";
        givenDriver.status = DriverStatus.AVAILABLE;
        givenDriver.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        DriverResponse whenResponse = DriverMapper.toResponse(givenDriver);

        assertThat(whenResponse.name()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("toResponse EXPECT response email equals entity email")
    void toResponse_expect_responseEmailMatchesEntity() {
        Driver givenDriver = new Driver();
        givenDriver.id = 1L;
        givenDriver.name = "n";
        givenDriver.email = "a@b.c";
        givenDriver.phone = "p";
        givenDriver.licenseNumber = "L";
        givenDriver.status = DriverStatus.AVAILABLE;
        givenDriver.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        DriverResponse whenResponse = DriverMapper.toResponse(givenDriver);

        assertThat(whenResponse.email()).isEqualTo("a@b.c");
    }

    @Test
    @DisplayName("toResponse EXPECT response phone equals entity phone")
    void toResponse_expect_responsePhoneMatchesEntity() {
        Driver givenDriver = new Driver();
        givenDriver.id = 1L;
        givenDriver.name = "n";
        givenDriver.email = "e@e";
        givenDriver.phone = "+100";
        givenDriver.licenseNumber = "L";
        givenDriver.status = DriverStatus.AVAILABLE;
        givenDriver.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        DriverResponse whenResponse = DriverMapper.toResponse(givenDriver);

        assertThat(whenResponse.phone()).isEqualTo("+100");
    }

    @Test
    @DisplayName("toResponse EXPECT response license number equals entity license number")
    void toResponse_expect_responseLicenseMatchesEntity() {
        Driver givenDriver = new Driver();
        givenDriver.id = 1L;
        givenDriver.name = "n";
        givenDriver.email = "e@e";
        givenDriver.phone = "p";
        givenDriver.licenseNumber = "XY-99";
        givenDriver.status = DriverStatus.AVAILABLE;
        givenDriver.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        DriverResponse whenResponse = DriverMapper.toResponse(givenDriver);

        assertThat(whenResponse.licenseNumber()).isEqualTo("XY-99");
    }

    @Test
    @DisplayName("toResponse EXPECT response status equals entity status")
    void toResponse_expect_responseStatusMatchesEntity() {
        Driver givenDriver = new Driver();
        givenDriver.id = 1L;
        givenDriver.name = "n";
        givenDriver.email = "e@e";
        givenDriver.phone = "p";
        givenDriver.licenseNumber = "L";
        givenDriver.status = DriverStatus.BUSY;
        givenDriver.createdAt = Instant.parse("2020-01-01T00:00:00Z");

        DriverResponse whenResponse = DriverMapper.toResponse(givenDriver);

        assertThat(whenResponse.status()).isEqualTo(DriverStatus.BUSY);
    }
}
