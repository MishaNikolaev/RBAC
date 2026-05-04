package com.nmichail.taxi.repository;

import com.nmichail.taxi.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}