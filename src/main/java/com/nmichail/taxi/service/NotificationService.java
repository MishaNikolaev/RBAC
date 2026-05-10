package com.nmichail.taxi.service;

import com.nmichail.taxi.model.NotificationTask;
import com.nmichail.taxi.model.NotificationTaskStatus;
import com.nmichail.taxi.model.RecipientType;
import com.nmichail.taxi.model.Trip;
import com.nmichail.taxi.repository.NotificationTaskRepository;
import com.nmichail.taxi.config.NotificationWorkNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationTaskRepository taskRepository;
    private final ObjectProvider<NotificationWorkNotifier> workNotifier;

    public NotificationTask enqueue(long tripId, RecipientType recipientType, long recipientId, String message) {
        NotificationTask t = new NotificationTask();
        t.tripId = tripId;
        t.recipientType = recipientType;
        t.recipientId = recipientId;
        t.message = message;
        t.status = NotificationTaskStatus.PENDING;
        t.attempts = 0;
        t.createdAt = Instant.now();
        NotificationTask saved = taskRepository.save(t);
        workNotifier.ifAvailable(NotificationWorkNotifier::notifyWorkers);
        return saved;
    }

    public List<NotificationTask> getByTrip(long tripId) {
        return taskRepository.findByTripIdOrderByCreatedAtAsc(tripId);
    }

    public void enqueueTripStatusNotifications(Trip trip) {
        enqueue(trip.id, RecipientType.PASSENGER, trip.passengerId, "Trip #" + trip.id + " status: " + trip.status);
        if (trip.driverId != null) {
            enqueue(trip.id, RecipientType.DRIVER, trip.driverId, "Trip #" + trip.id + " status: " + trip.status);
        }
    }

    @Transactional
    public Optional<NotificationTask> tryLockNextPendingTask() {
        Long id = taskRepository.findNextPendingTaskId();
        if (id == null) {
            return Optional.empty();
        }
        int updated = taskRepository.claimPendingTask(id);
        if (updated != 1) {
            return Optional.empty();
        }
        return taskRepository.findById(id);
    }

    @Transactional
    public void markSent(long taskId) {
        taskRepository.markSent(taskId);
    }

    @Transactional
    public void markFailed(long taskId, String reason) {
        taskRepository.markFailed(taskId, reason != null ? reason : "error");
    }
}