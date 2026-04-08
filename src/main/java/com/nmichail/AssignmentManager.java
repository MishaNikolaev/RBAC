package com.nmichail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final ConcurrentMap<String, RoleAssignment> assignments = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private AuditLog auditLog;

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public void add(RoleAssignment assignment) {
        RoleAssignment prev = assignments.putIfAbsent(assignment.assignmentId(), assignment);
        if (prev != null) {
            throw new IllegalArgumentException("id already exists");
        }
        if (auditLog != null) {
            auditLog.log("ASSIGNMENT_CREATE", assignment.metadata().assignedBy(),
                    assignment.user().username() + " -> " + assignment.role().name,
                    assignment.assignmentType());
        }
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        if (assignments.remove(assignment.assignmentId(), assignment)) {
            if (auditLog != null) {
                auditLog.log("ASSIGNMENT_DELETE", "system", assignment.assignmentId(), assignment.role().name);
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(assignment -> assignment.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(assignment -> assignment.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        return assignments.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(assignment -> !assignment.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .anyMatch(a -> a.role().hasPermission(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        ValidationUtils.requireNonEmpty(assignmentId, "assignmentId");
        synchronized (lock) {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new IllegalArgumentException("assignment does not exist: " + assignmentId);
            }
            if (assignment instanceof PermanentAssignment pa) {
                synchronized (pa) {
                    pa.revoke();
                }
                if (auditLog != null) {
                    auditLog.log("ASSIGNMENT_REVOKE", "system", assignmentId, "permanent revoked");
                }
            } else {
                assignments.remove(assignmentId);
                if (auditLog != null) {
                    auditLog.log("ASSIGNMENT_REVOKE", "system", assignmentId, "temporary removed");
                }
            }
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        ValidationUtils.requireNonEmpty(assignmentId, "assignmentId");
        ValidationUtils.requireNonEmpty(newExpirationDate, "newExpirationDate");
        RoleAssignment assignment = assignments.get(assignmentId);
        if (!(assignment instanceof TemporaryAssignment ta)) {
            throw new IllegalArgumentException("Assigment isn't temporary");
        }
        synchronized (ta) {
            ta.extend(newExpirationDate);
        }
    }
}