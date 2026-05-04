package com.nmichail.taxi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    private static RequestMatcher openapiAndSwaggerDocs() {
        return request -> {
            String uri = request.getRequestURI();
            if (uri == null || uri.isEmpty()) {
                return false;
            }
            String p = uri.replaceAll("/{2,}", "/");
            if (!p.startsWith("/")) {
                p = "/" + p;
            }
            return p.startsWith("/swagger-ui")
                    || p.startsWith("/v3/api-docs")
                    || "/v3/api-docs".equals(p)
                    || p.startsWith("/webjars/")
                    || "/scalar".equals(p)
                    || p.startsWith("/scalar/");
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(openapiAndSwaggerDocs()).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}