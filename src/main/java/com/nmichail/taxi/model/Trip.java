package com.nmichail.taxi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "passenger_id", nullable = false)
    public Long passengerId;

    @Column(name = "driver_id")
    public Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public TripStatus status;

    @Column(nullable = false, length = 500)
    public String origin;

    @Column(nullable = false, length = 500)
    public String destination;

    @Column(name = "distance_km", precision = 10, scale = 2, nullable = false)
    public BigDecimal distanceKm;

    @Column(precision = 12, scale = 2)
    public BigDecimal price;

    public Integer rating;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}