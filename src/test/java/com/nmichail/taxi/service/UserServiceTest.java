package com.nmichail.taxi.service;

import com.nmichail.taxi.dto.DriverResponse;
import com.nmichail.taxi.model.Driver;
import com.nmichail.taxi.model.DriverStatus;
import com.nmichail.taxi.model.Passenger;
import com.nmichail.taxi.repository.DriverRepository;
import com.nmichail.taxi.repository.PassengerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CachedAvailableDriversService cachedAvailableDriversService;

    @Mock
    private AvailableDriversCacheEvictor availableDriversCacheEvictor;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("createPassenger EXPECT saved passenger name equals argument")
    void createPassenger_expect_savedNameMatchesArgument() {
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> {
            Passenger p = invocation.getArgument(0);
            p.id = 11L;
            return p;
        });

        Passenger whenPassenger = userService.createPassenger("Alan", "a@b.c", "1");

        assertThat(whenPassenger.name).isEqualTo("Alan");
    }

    @Test
    @DisplayName("createPassenger EXPECT saved passenger email equals argument")
    void createPassenger_expect_savedEmailMatchesArgument() {
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> {
            Passenger p = invocation.getArgument(0);
            p.id = 11L;
            return p;
        });

        Passenger whenPassenger = userService.createPassenger("x", "mail@test.dev", "1");

        assertThat(whenPassenger.email).isEqualTo("mail@test.dev");
    }

    @Test
    @DisplayName("createPassenger EXPECT saved passenger phone equals argument")
    void createPassenger_expect_savedPhoneMatchesArgument() {
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> {
            Passenger p = invocation.getArgument(0);
            p.id = 11L;
            return p;
        });

        Passenger whenPassenger = userService.createPassenger("x", "e@e", "+900");

        assertThat(whenPassenger.phone).isEqualTo("+900");
    }

    @Test
    @DisplayName("getPassenger EXPECT same instance as repository returns")
    void getPassenger_expect_returnsSameInstanceAsRepository() {
        Passenger givenPassenger = new Passenger();
        givenPassenger.id = 3L;
        when(passengerRepository.findById(3L)).thenReturn(Optional.of(givenPassenger));

        Optional<Passenger> whenPassenger = userService.getPassenger(3L);

        assertThat(whenPassenger).containsSame(givenPassenger);
    }

    @Test
    @DisplayName("createDriver EXPECT new driver status is AVAILABLE")
    void createDriver_expect_statusAvailable() {
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver d = invocation.getArgument(0);
            d.id = 88L;
            return d;
        });

        Driver whenDriver = userService.createDriver("d", "d@e", "ph", "LIC-1");

        assertThat(whenDriver.status).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    @DisplayName("createDriver EXPECT available drivers cache evictor invalidate is called")
    void createDriver_expect_cacheEvictorInvalidate() {
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver d = invocation.getArgument(0);
            d.id = 1L;
            return d;
        });

        userService.createDriver("d", "d@e", "ph", "LIC");

        verify(availableDriversCacheEvictor).invalidate();
    }

    @Test
    @DisplayName("getDriver EXPECT same instance as repository returns")
    void getDriver_expect_returnsSameInstanceAsRepository() {
        Driver givenDriver = new Driver();
        givenDriver.id = 4L;
        when(driverRepository.findById(4L)).thenReturn(Optional.of(givenDriver));

        Optional<Driver> whenDriver = userService.getDriver(4L);

        assertThat(whenDriver).containsSame(givenDriver);
    }

    @Test
    @DisplayName("listAvailableDrivers EXPECT same instance as cached service returns")
    void listAvailableDrivers_expect_returnsSameInstanceAsCachedService() {
        List<DriverResponse> givenResponses = List.of(new DriverResponse(1L, "a", "a@b", "p", "L", DriverStatus.AVAILABLE));
        when(cachedAvailableDriversService.listAvailable()).thenReturn(givenResponses);

        List<DriverResponse> whenList = userService.listAvailableDrivers();

        assertThat(whenList).isSameAs(givenResponses);
    }

    @Test
    @DisplayName("updateDriverStatus EXPECT HTTP 404 when driver not found")
    void updateDriverStatus_expect_notFoundWhenMissingDriver() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException whenEx = assertThrows(ResponseStatusException.class,
                () -> userService.updateDriverStatus(99L, DriverStatus.BUSY));

        assertThat(whenEx.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("updateDriverStatus EXPECT saved driver status is BUSY")
    void updateDriverStatus_expect_savedStatusBusy() {
        Driver givenDriver = new Driver();
        givenDriver.id = 12L;
        givenDriver.status = DriverStatus.AVAILABLE;
        when(driverRepository.findById(12L)).thenReturn(Optional.of(givenDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        Driver whenDriver = userService.updateDriverStatus(12L, DriverStatus.BUSY);

        assertThat(whenDriver.status).isEqualTo(DriverStatus.BUSY);
    }

    @Test
    @DisplayName("updateDriverStatus EXPECT cache evictor invalidate after success")
    void updateDriverStatus_expect_cacheEvictorInvalidate() {
        Driver givenDriver = new Driver();
        givenDriver.id = 12L;
        givenDriver.status = DriverStatus.AVAILABLE;
        when(driverRepository.findById(12L)).thenReturn(Optional.of(givenDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateDriverStatus(12L, DriverStatus.BUSY);

        verify(availableDriversCacheEvictor).invalidate();
    }

    @Test
    @DisplayName("leaseAvailableDriverId EXPECT empty Optional when no AVAILABLE row")
    void leaseAvailableDriverId_expect_emptyWhenQueryReturnsNoRow() throws Exception {
        ResultSet givenRs = mock(ResultSet.class);
        when(givenRs.next()).thenReturn(false);
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class))).thenAnswer(invocation -> {
            ResultSetExtractor<?> extractor = invocation.getArgument(1);
            return extractor.extractData(givenRs);
        });

        Optional<Long> whenId = userService.leaseAvailableDriverId();

        assertThat(whenId).isEmpty();
    }

    @Test
    @DisplayName("leaseAvailableDriverId EXPECT empty Optional when update affects no row")
    void leaseAvailableDriverId_expect_emptyWhenUpdateNotExactlyOneRow() throws Exception {
        ResultSet givenRs = mock(ResultSet.class);
        when(givenRs.next()).thenReturn(true);
        when(givenRs.getLong(1)).thenReturn(5L);
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class))).thenAnswer(invocation -> {
            ResultSetExtractor<?> extractor = invocation.getArgument(1);
            return extractor.extractData(givenRs);
        });
        when(jdbcTemplate.update(anyString(), eq(5L))).thenReturn(0);

        Optional<Long> whenId = userService.leaseAvailableDriverId();

        assertThat(whenId).isEmpty();
    }

    @Test
    @DisplayName("leaseAvailableDriverId EXPECT contains driver id when update affects one row")
    void leaseAvailableDriverId_expect_containsIdWhenLeaseSucceeds() throws Exception {
        ResultSet givenRs = mock(ResultSet.class);
        when(givenRs.next()).thenReturn(true);
        when(givenRs.getLong(1)).thenReturn(77L);
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class))).thenAnswer(invocation -> {
            ResultSetExtractor<?> extractor = invocation.getArgument(1);
            return extractor.extractData(givenRs);
        });
        when(jdbcTemplate.update(anyString(), eq(77L))).thenReturn(1);

        Optional<Long> whenId = userService.leaseAvailableDriverId();

        assertThat(whenId).contains(77L);
    }

    @Test
    @DisplayName("leaseAvailableDriverId EXPECT cache evictor invalidate on successful lease")
    void leaseAvailableDriverId_expect_cacheEvictorInvalidateWhenLeaseSucceeds() throws Exception {
        ResultSet givenRs = mock(ResultSet.class);
        when(givenRs.next()).thenReturn(true);
        when(givenRs.getLong(1)).thenReturn(77L);
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class))).thenAnswer(invocation -> {
            ResultSetExtractor<?> extractor = invocation.getArgument(1);
            return extractor.extractData(givenRs);
        });
        when(jdbcTemplate.update(anyString(), eq(77L))).thenReturn(1);

        userService.leaseAvailableDriverId();

        verify(availableDriversCacheEvictor).invalidate();
    }
}