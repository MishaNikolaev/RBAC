package com.nmichail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentFilters {

    public static AssignmentFilter byUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUserName(String username) {
        ValidationUtils.requireNonEmpty(username, "username");
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        ValidationUtils.requireNonEmpty(roleName, "roleName");
        return assignment -> assignment.role().name().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return assignment -> assignment.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        LocalDateTime afterDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return assignment -> LocalDateTime.parse(assignment.metadata().assignedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .isAfter(afterDate);
    }

    public static AssignmentFilter expiringBefore(String date) {
        LocalDateTime beforeDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return assignment -> assignment instanceof TemporaryAssignment &&
                LocalDateTime.parse(((TemporaryAssignment) assignment).expiresAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .isBefore(beforeDate);
    }

}