package com.nmichail.taxi.repository;

import com.nmichail.taxi.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT AVG(t.price) FROM Trip t WHERE t.createdAt BETWEEN :start AND :end")
    BigDecimal averagePriceByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);
}