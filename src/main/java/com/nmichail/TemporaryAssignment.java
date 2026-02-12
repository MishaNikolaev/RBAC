package com.nmichail;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {

    String expiresAt;
    final boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return isActive(LocalDate.now().toString());
    }

    public boolean isActive(String asOfDate) {
        return asOfDate != null && asOfDate.compareTo(expiresAt) <= 0;
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public boolean isExpired() {
        return isExpired(LocalDate.now().toString());
    }

    public boolean isExpired(String asOfDate) {
        return asOfDate == null || asOfDate.compareTo(expiresAt) > 0;
    }

    public void extend(String newExpirationDate) {
        this.expiresAt = newExpirationDate;
    }

    public String getTimeRemaining() {
        try {
            LocalDate end = LocalDate.parse(expiresAt);
            long days = ChronoUnit.DAYS.between(LocalDate.now(), end);
            if (days < 0) return "Expired";
            if (days == 0) return "Expires today";
            return days + " day(s) remaining";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public String getExpiresAt() { return expiresAt; }
    public boolean isAutoRenew() { return autoRenew; }

    @Override
    public String summary() {
        return super.summary() + " (expires: " + expiresAt + ")";
    }
}
