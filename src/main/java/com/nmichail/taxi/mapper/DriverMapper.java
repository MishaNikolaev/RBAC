package com.nmichail.taxi.mapper;

import com.nmichail.taxi.dto.DriverResponse;
import com.nmichail.taxi.model.Driver;

public final class DriverMapper {
    private DriverMapper() {
    }

    public static DriverResponse toResponse(Driver d) {
        return new DriverResponse(d.id, d.name, d.email, d.phone, d.licenseNumber, d.status);
    }
}