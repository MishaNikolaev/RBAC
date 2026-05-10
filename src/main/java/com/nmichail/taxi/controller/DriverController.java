package com.nmichail.taxi.controller;

import com.nmichail.taxi.dto.CreateDriverRequest;
import com.nmichail.taxi.dto.DriverResponse;
import com.nmichail.taxi.dto.UpdateDriverStatusRequest;
import com.nmichail.taxi.mapper.DriverMapper;
import com.nmichail.taxi.model.Driver;
import com.nmichail.taxi.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Drivers", description = "Водители: CRUD, статус, список доступных")
@RestController
@RequiredArgsConstructor
public class DriverController {

    private final UserService userService;

    @PostMapping("/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse create(@Valid @RequestBody CreateDriverRequest req) {
        Driver d = userService.createDriver(req.name(), req.email(), req.phone(), req.licenseNumber());
        return DriverMapper.toResponse(d);
    }

    @GetMapping("/drivers/available")
    public List<DriverResponse> listAvailable() {
        return userService.listAvailableDrivers();
    }

    @GetMapping("/drivers/{id}")
    public DriverResponse get(@PathVariable long id) {
        Driver d = userService.getDriver(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found: " + id));
        return DriverMapper.toResponse(d);
    }

    @PatchMapping("/drivers/{id}/status")
    public DriverResponse updateStatus(@PathVariable long id, @Valid @RequestBody UpdateDriverStatusRequest req) {
        return DriverMapper.toResponse(userService.updateDriverStatus(id, req.status()));
    }
}