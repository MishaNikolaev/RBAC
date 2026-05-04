package com.nmichail.taxi.service;

import com.nmichail.taxi.model.Trip;
import com.nmichail.taxi.model.TripStatus;
import com.nmichail.taxi.repository.TripRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final BigDecimal tariffPerKm;

    public TripService(
            TripRepository tripRepository,
            UserService userService,
            NotificationService notificationService,
            @Value("${taxi.pricing.tariff-per-km:1.0}") BigDecimal tariffPerKm
    ) {
        this.tripRepository = tripRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.tariffPerKm = tariffPerKm;
    }

    @Transactional
    public Trip createTrip(long passengerId, String origin, String destination, double distanceKm) {
        userService.getPassenger(passengerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passenger not found: " + passengerId));

        Optional<Long> leasedDriverId = userService.leaseAvailableDriverId();

        Trip t = new Trip();
        t.passengerId = passengerId;
        t.driverId = leasedDriverId.orElse(null);
        t.status = leasedDriverId.isPresent() ? TripStatus.ASSIGNED : TripStatus.CREATED;
        t.origin = origin;
        t.destination = destination;
        t.distanceKm = BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP);
        t.price = t.distanceKm.multiply(tariffPerKm).setScale(2, RoundingMode.HALF_UP);
        t.createdAt = Instant.now();
        t.updatedAt = t.createdAt;
        Trip saved = tripRepository.save(t);

        notificationService.enqueueTripStatusNotifications(saved);
        return saved;
    }

    public Optional<Trip> getTrip(long id) {
        return tripRepository.findById(id);
    }

    public List<Trip> getPassengerTrips(long passengerId) {
        return tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
    }

    @Transactional
    public Trip updateStatus(long tripId, TripStatus status) {
        Trip t = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));
        t.status = status;
        t.updatedAt = Instant.now();
        Trip saved = tripRepository.save(t);
        notificationService.enqueueTripStatusNotifications(saved);
        return saved;
    }

    @Transactional
    public Trip rateTrip(long tripId, int rating) {
        Trip t = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));
        if (t.status != TripStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip is not completed: " + tripId);
        }
        t.rating = rating;
        t.updatedAt = Instant.now();
        return tripRepository.save(t);
    }
}