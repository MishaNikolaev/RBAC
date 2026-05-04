package com.nmichail.taxi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "passengers")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 200)
    public String name;

    @Column(nullable = false, length = 320)
    public String email;

    @Column(length = 50)
    public String phone;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}