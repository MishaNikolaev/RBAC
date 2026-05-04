package com.nmichail.taxi.repository;

import com.nmichail.taxi.model.NotificationTask;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationTaskJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public Optional<NotificationTask> tryLockNextPendingTask() {
        Long id = jdbcTemplate.query(
                "select id from notification_tasks " +
                        "where status = 'PENDING' and attempts < 3 " +
                        "order by created_at " +
                        "limit 1",
                rs -> rs.next() ? rs.getLong(1) : null
        );
        if (id == null) {
            return Optional.empty();
        }
        int updated = jdbcTemplate.update(
                "update notification_tasks set status = 'IN_PROGRESS' where id = ? and status = 'PENDING'",
                id
        );
        if (updated != 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(entityManager.find(NotificationTask.class, id));
    }

    public void markSent(long taskId) {
        jdbcTemplate.update("update notification_tasks set status = 'SENT' where id = ?", taskId);
    }

    public void markFailed(long taskId, String reason) {
        jdbcTemplate.update(
                "update notification_tasks set status = 'FAILED', attempts = attempts + 1, message = message || ' | fail=' || ? where id = ?",
                reason != null ? reason : "error",
                taskId
        );
    }
}