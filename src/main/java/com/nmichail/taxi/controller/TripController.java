package com.nmichail.taxi.controller;

import com.nmichail.taxi.dto.CreateTripRequest;
import com.nmichail.taxi.dto.TripResponse;
import com.nmichail.taxi.dto.UpdateTripRatingRequest;
import com.nmichail.taxi.dto.UpdateTripStatusRequest;
import com.nmichail.taxi.mapper.TripMapper;
import com.nmichail.taxi.model.Trip;
import com.nmichail.taxi.service.TripService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Trips", description = "Поездки: создание, статус, рейтинг, список пассажира")
@RestController
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse create(@Valid @RequestBody CreateTripRequest req) {
        Trip t = tripService.createTrip(req.passengerId(), req.origin(), req.destination(), req.distanceKm());
        return TripMapper.toResponse(t);
    }

    @GetMapping("/trips/{id}")
    public TripResponse get(@PathVariable long id) {
        Trip t = tripService.getTrip(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + id));
        return TripMapper.toResponse(t);
    }

    @GetMapping("/trips")
    public List<TripResponse> getByPassenger(@RequestParam("passenger_id") long passengerId) {
        return tripService.getPassengerTrips(passengerId).stream().map(TripMapper::toResponse).toList();
    }

    @PatchMapping("/trips/{id}/status")
    public TripResponse updateStatus(@PathVariable long id, @Valid @RequestBody UpdateTripStatusRequest req) {
        return TripMapper.toResponse(tripService.updateStatus(id, req.status()));
    }

    @PatchMapping("/trips/{id}/rating")
    public TripResponse rate(@PathVariable long id, @Valid @RequestBody UpdateTripRatingRequest req) {
        return TripMapper.toResponse(tripService.rateTrip(id, req.rating()));
    }
}