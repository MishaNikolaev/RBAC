package com.nmichail.taxi.service;

import com.nmichail.taxi.model.AppUser;
import com.nmichail.taxi.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        var authorities = Arrays.stream(u.roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return User.builder()
                .username(u.username)
                .password(u.passwordHash)
                .authorities(authorities)
                .build();
    }
}