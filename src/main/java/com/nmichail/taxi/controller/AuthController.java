package com.nmichail.taxi.controller;

import com.nmichail.taxi.dto.LoginRequest;
import com.nmichail.taxi.dto.LoginResponse;
import com.nmichail.taxi.model.AppUser;
import com.nmichail.taxi.repository.AppUserRepository;
import com.nmichail.taxi.service.JwtService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Auth", description = "Аутентификация: выдача JWT")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Operation(summary = "Вход", description = "По username/password из БД; в ответе — JWT для остальных эндпоинтов.")
    @SecurityRequirements
    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AppUser u = appUserRepository.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));
        if (!passwordEncoder.matches(req.password(), u.passwordHash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
        List<String> roles = Arrays.stream(u.roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        String token = jwtService.issueToken(u.username, roles);
        return new LoginResponse(token);
    }
}