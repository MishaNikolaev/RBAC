package com.nmichail.taxi.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private final JwtService jwtService = new JwtService(SECRET, 3600);

    @Test
    @DisplayName("issueToken then extractUsername EXPECT subject matches username")
    void issueTokenThenExtractUsername_expect_returnsSubject() {
        String givenToken = jwtService.issueToken("alice", List.of("ROLE_USER"));

        String whenUsername = jwtService.extractUsername(givenToken);

        assertThat(whenUsername).isEqualTo("alice");
    }

    @Test
    @DisplayName("extractUsername EXPECT null for malformed token")
    void extractUsername_expect_returnsNullWhenTokenMalformed() {

        String whenUsername = jwtService.extractUsername("not-a-jwt");

        assertThat(whenUsername).isNull();
    }

    @Test
    @DisplayName("isTokenValid EXPECT true when subject matches and token not expired")
    void isTokenValid_expect_trueWhenSubjectMatchesAndNotExpired() {
        String givenToken = jwtService.issueToken("bob", List.of("ROLE_ADMIN"));
        UserDetails givenUser = User.builder().username("bob").password("p").roles("ADMIN").build();

        boolean whenValid = jwtService.isTokenValid(givenToken, givenUser);

        assertThat(whenValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid EXPECT false when subject does not match UserDetails")
    void isTokenValid_expect_falseWhenSubjectDiffersFromUserDetails() {
        String givenToken = jwtService.issueToken("carol", List.of("ROLE_USER"));
        UserDetails givenUser = User.builder().username("dave").password("p").roles("USER").build();

        boolean whenValid = jwtService.isTokenValid(givenToken, givenUser);

        assertThat(whenValid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid EXPECT false for expired token signed with same secret")
    void isTokenValid_expect_falseWhenTokenExpired() {
        SecretKey givenKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String givenToken = Jwts.builder()
                .subject("erin")
                .issuedAt(Date.from(Instant.now().minusSeconds(10_000)))
                .expiration(Date.from(Instant.now().minusSeconds(5_000)))
                .signWith(givenKey)
                .compact();
        UserDetails givenUser = User.builder().username("erin").password("p").roles("USER").build();

        boolean whenValid = jwtService.isTokenValid(givenToken, givenUser);

        assertThat(whenValid).isFalse();
    }
}
