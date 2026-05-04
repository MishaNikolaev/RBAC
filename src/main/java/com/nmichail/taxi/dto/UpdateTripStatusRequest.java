package com.nmichail.taxi.dto;

import com.nmichail.taxi.model.TripStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTripStatusRequest(@NotNull TripStatus status) {
}