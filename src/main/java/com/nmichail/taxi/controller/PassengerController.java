package com.nmichail.taxi.controller;

import com.nmichail.taxi.dto.CreatePassengerRequest;
import com.nmichail.taxi.dto.PassengerResponse;
import com.nmichail.taxi.mapper.PassengerMapper;
import com.nmichail.taxi.model.Passenger;
import com.nmichail.taxi.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Passengers", description = "Пассажиры")
@RestController
@RequiredArgsConstructor
public class PassengerController {

    private final UserService userService;

    @PostMapping("/passengers")
    @ResponseStatus(HttpStatus.CREATED)
    public PassengerResponse create(@Valid @RequestBody CreatePassengerRequest req) {
        Passenger p = userService.createPassenger(req.name(), req.email(), req.phone());
        return PassengerMapper.toResponse(p);
    }

    @GetMapping("/passengers/{id}")
    public PassengerResponse get(@PathVariable long id) {
        Passenger p = userService.getPassenger(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found: " + id));
        return PassengerMapper.toResponse(p);
    }
}