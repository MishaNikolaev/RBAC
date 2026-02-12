package com.nmichail;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

abstract class AbstractRoleAssignment implements RoleAssignment {
    private static final DateTimeFormatter SUMMARY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    final String assignmentId;
    final User user;
    final Role role;
    final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = "assignment_" + UUID.randomUUID();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() { return assignmentId; }

    @Override
    public User user() { return user; }

    @Override
    public Role role() { return role; }

    @Override
    public AssignmentMetadata metadata() { return metadata; }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    public String summary() {
        String at = SUMMARY_TIME.format(Instant.parse(metadata.assignedAt()));
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(assignmentType()).append("] ")
                .append(role.name).append(" assigned to ").append(user.username())
                .append(" by ").append(metadata.assignedBy()).append(" at ").append(at);
        if (metadata.reason() != null && !metadata.reason().isBlank()) {
            sb.append("\nReason: ").append(metadata.reason());
        }
        sb.append("\nStatus: ").append(isActive() ? "ACTIVE" : "INACTIVE");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment other = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, other.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }
}