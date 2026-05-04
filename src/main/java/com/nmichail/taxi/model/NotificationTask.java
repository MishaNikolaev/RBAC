package com.nmichail.taxi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notification_tasks")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "trip_id", nullable = false)
    public Long tripId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 30)
    public RecipientType recipientType;

    @Column(name = "recipient_id", nullable = false)
    public Long recipientId;

    @Column(nullable = false, length = 2000)
    public String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public NotificationTaskStatus status;

    @Column(nullable = false)
    public int attempts;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}