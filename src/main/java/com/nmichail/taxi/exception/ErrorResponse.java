package com.nmichail.taxi.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String message
) {
}