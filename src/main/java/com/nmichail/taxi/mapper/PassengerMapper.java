package com.nmichail.taxi.mapper;

import com.nmichail.taxi.dto.PassengerResponse;
import com.nmichail.taxi.model.Passenger;

public final class PassengerMapper {
    private PassengerMapper() {
    }

    public static PassengerResponse toResponse(Passenger p) {
        return new PassengerResponse(p.id, p.name, p.email, p.phone);
    }
}