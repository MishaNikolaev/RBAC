package com.nmichail.taxi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateTripRatingRequest(@NotNull @Min(1) @Max(5) Integer rating) {
}