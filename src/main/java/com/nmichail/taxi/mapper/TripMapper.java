package com.nmichail.taxi.mapper;

import com.nmichail.taxi.dto.TripResponse;
import com.nmichail.taxi.model.Trip;

public final class TripMapper {
    private TripMapper() {
    }

    public static TripResponse toResponse(Trip t) {
        return new TripResponse(t.id, t.passengerId, t.driverId, t.status, t.origin, t.destination, t.distanceKm, t.price, t.rating);
    }
}