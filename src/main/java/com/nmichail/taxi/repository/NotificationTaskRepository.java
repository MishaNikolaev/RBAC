package com.nmichail.taxi.repository;

import com.nmichail.taxi.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByTripIdOrderByCreatedAtAsc(Long tripId);

    @Query(value = "select id from notification_tasks "
            + "where status = 'PENDING' and attempts < 3 "
            + "order by created_at "
            + "limit 1",
            nativeQuery = true)
    Long findNextPendingTaskId();

    @Modifying
    @Query(value = "update notification_tasks "
            + "set status = 'IN_PROGRESS' "
            + "where id = :id and status = 'PENDING'",
            nativeQuery = true)
    int claimPendingTask(@Param("id") Long id);

    @Modifying
    @Query(value = "update notification_tasks set status = 'SENT' where id = :taskId", nativeQuery = true)
    void markSent(@Param("taskId") long taskId);

    @Modifying
    @Query(value = "update notification_tasks "
            + "set status = 'FAILED', attempts = attempts + 1, "
            + "message = message || ' | fail=' || :reason "
            + "where id = :taskId",
            nativeQuery = true)
    void markFailed(@Param("taskId") long taskId, @Param("reason") String reason);
}