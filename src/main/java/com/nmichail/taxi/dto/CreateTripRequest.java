package com.nmichail.taxi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTripRequest(
        @NotNull Long passengerId,
        @NotBlank @Size(max = 500) String origin,
        @NotBlank @Size(max = 500) String destination,
        @NotNull @Positive Double distanceKm
) {
}