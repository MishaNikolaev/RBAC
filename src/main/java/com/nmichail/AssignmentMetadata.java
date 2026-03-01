package com.nmichail;

import java.time.Instant;

public record AssignmentMetadata(
        String assignedBy,
        String assignedAt,
        String reason
) {
    public static AssignmentMetadata now(String assignedBy, String reason) {
        return new AssignmentMetadata(assignedBy, Instant.now().toString(), reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Assigned by: ").append(assignedBy).append("\n");
        sb.append("Assigned at: ").append(assignedAt).append("\n");
        if (reason != null && !reason.isBlank()) {
            sb.append("Reason: ").append(reason);
        }
        return sb.toString();
    }
}