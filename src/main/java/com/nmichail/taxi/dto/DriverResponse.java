package com.nmichail.taxi.dto;

import com.nmichail.taxi.model.DriverStatus;

public record DriverResponse(long id, String name, String email, String phone, String licenseNumber, DriverStatus status) {
}