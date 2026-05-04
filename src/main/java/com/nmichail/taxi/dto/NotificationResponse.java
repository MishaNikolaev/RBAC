package com.nmichail.taxi.dto;

import com.nmichail.taxi.model.NotificationTaskStatus;
import com.nmichail.taxi.model.RecipientType;

public record NotificationResponse(
        long id,
        long tripId,
        RecipientType recipientType,
        long recipientId,
        String message,
        NotificationTaskStatus status,
        int attempts
) {
}