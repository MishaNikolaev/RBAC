package com.nmichail.taxi.service;

import com.nmichail.taxi.dto.DriverResponse;
import com.nmichail.taxi.model.Driver;
import com.nmichail.taxi.model.DriverStatus;
import com.nmichail.taxi.model.Passenger;
import com.nmichail.taxi.repository.DriverRepository;
import com.nmichail.taxi.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final JdbcTemplate jdbcTemplate;
    private final CachedAvailableDriversService cachedAvailableDriversService;
    private final AvailableDriversCacheEvictor availableDriversCacheEvictor;

    public Passenger createPassenger(String name, String email, String phone) {
        Passenger p = new Passenger();
        p.name = name;
        p.email = email;
        p.phone = phone;
        p.createdAt = Instant.now();
        return passengerRepository.save(p);
    }

    public Optional<Passenger> getPassenger(long id) {
        return passengerRepository.findById(id);
    }

    public Driver createDriver(String name, String email, String phone, String licenseNumber) {
        Driver d = new Driver();
        d.name = name;
        d.email = email;
        d.phone = phone;
        d.licenseNumber = licenseNumber;
        d.status = DriverStatus.AVAILABLE;
        d.createdAt = Instant.now();
        Driver saved = driverRepository.save(d);
        availableDriversCacheEvictor.invalidate();
        return saved;
    }

    public Optional<Driver> getDriver(long id) {
        return driverRepository.findById(id);
    }

    public List<DriverResponse> listAvailableDrivers() {
        return cachedAvailableDriversService.listAvailable();
    }

    @Transactional
    public Driver updateDriverStatus(long driverId, DriverStatus status) {
        Driver d = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found: " + driverId));
        d.status = status;
        Driver saved = driverRepository.save(d);
        availableDriversCacheEvictor.invalidate();
        return saved;
    }

    @Transactional
    public Optional<Long> leaseAvailableDriverId() {
        Long driverId = jdbcTemplate.query(
                "select id from drivers where status = 'AVAILABLE' order by id for update skip locked limit 1",
                rs -> rs.next() ? rs.getLong(1) : null
        );
        if (driverId == null) {
            return Optional.empty();
        }
        int updated = jdbcTemplate.update("update drivers set status = 'BUSY' where id = ? and status = 'AVAILABLE'", driverId);
        if (updated != 1) {
            return Optional.empty();
        }
        availableDriversCacheEvictor.invalidate();
        return Optional.of(driverId);
    }
}