package com.nmichail.taxi.dto;

import com.nmichail.taxi.model.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDriverStatusRequest(@NotNull DriverStatus status) {
}