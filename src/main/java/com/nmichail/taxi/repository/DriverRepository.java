package com.nmichail.taxi.repository;

import com.nmichail.taxi.model.Driver;
import com.nmichail.taxi.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByStatusOrderById(DriverStatus status);
}