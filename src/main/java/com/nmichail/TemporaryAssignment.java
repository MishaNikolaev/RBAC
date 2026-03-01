package com.nmichail;

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
        return isActive(DateUtils.getCurrentDate());
    }

    public boolean isActive(String asOfDate) {
        return asOfDate != null && !DateUtils.isAfter(asOfDate, expiresAt);
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public String expiresAt() {
        return expiresAt;
    }

    public boolean autoRenew() {
        return autoRenew;
    }

    public boolean isExpired() {
        return isExpired(DateUtils.getCurrentDate());
    }

    public boolean isExpired(String asOfDate) {
        return asOfDate == null || DateUtils.isAfter(asOfDate, expiresAt);
    }

    public void extend(String newExpirationDate) {
        ValidationUtils.requireNonEmpty(newExpirationDate, "newExpirationDate");
        if (!ValidationUtils.isValidDate(newExpirationDate)) {
            throw new IllegalArgumentException("invalid date format, expected yyyy-MM-dd: " + newExpirationDate);
        }
        this.expiresAt = newExpirationDate.trim();
    }

    public String getTimeRemaining() {
        String relative = DateUtils.formatRelativeTime(expiresAt);
        if ("today".equals(relative)) return "Expires today";
        if (relative.startsWith("in ")) return relative.replace("in ", "") + " remaining";
        return "Expired";
    }

    public String getExpiresAt() { return expiresAt; }
    public boolean isAutoRenew() { return autoRenew; }

    @Override
    public String summary() {
        return super.summary() + " (expires: " + expiresAt + ")";
    }
}