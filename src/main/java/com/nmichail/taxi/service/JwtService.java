package com.nmichail.taxi.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(
            @Value("${taxi.security.jwt.secret}") String secret,
            @Value("${taxi.security.jwt.ttl-seconds:3600}") long ttlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String issueToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            String sub = parseSignedClaims(token).getSubject();
            return (sub == null || sub.isBlank()) ? null : sub;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseSignedClaims(token);
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                return false;
            }
            if (!subject.equals(userDetails.getUsername())) {
                return false;
            }
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseSignedClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}