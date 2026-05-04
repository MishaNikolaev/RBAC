package com.nmichail.taxi.repository;

import com.nmichail.taxi.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    List<NotificationTask> findByTripIdOrderByCreatedAtAsc(Long tripId);
}