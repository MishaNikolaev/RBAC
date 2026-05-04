package com.nmichail.taxi.config;

import com.nmichail.taxi.model.AppUser;
import com.nmichail.taxi.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class DefaultAdminInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultUsername;
    private final String defaultPassword;

    public DefaultAdminInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${taxi.security.default-admin.username:admin}") String defaultUsername,
            @Value("${taxi.security.default-admin.password:admin}") String defaultPassword
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        appUserRepository.findByUsername(defaultUsername).ifPresentOrElse(u -> {
        }, () -> {
            AppUser u = new AppUser();
            u.username = defaultUsername;
            u.passwordHash = passwordEncoder.encode(defaultPassword);
            u.roles = "ADMIN";
            u.createdAt = Instant.now();
            appUserRepository.save(u);
        });
    }
}