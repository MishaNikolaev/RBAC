package com.nmichail.taxi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    public String passwordHash;

    @Column(nullable = false, length = 500)
    public String roles;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}