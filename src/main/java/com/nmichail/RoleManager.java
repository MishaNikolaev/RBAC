package com.nmichail;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> byId = new HashMap<>();
    private final Map<String, Role> byName = new HashMap<>();

    private Predicate<Role> roleAssignedChecker = null;
    private AuditLog auditLog;

    public void setRoleAssignedChecker(Predicate<Role> checker) {
        this.roleAssignedChecker = checker;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        if (byId.containsKey(role.id)) {
            throw new IllegalArgumentException("role with same id already exists");
        }
        if (byName.containsKey(role.name)) {
            throw new IllegalArgumentException("role with name '" + role.name + "' already exists");
        }
        byId.put(role.id, role);
        byName.put(role.name, role);
        if (auditLog != null) {
            auditLog.log("ROLE_CREATE", "system", role.name, role.description);
        }
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) {
            return false;
        }
        if (roleAssignedChecker != null && roleAssignedChecker.test(role)) {
            throw new IllegalStateException("role is assigned to users");
        }
        boolean removed = byId.remove(role.id, role);
        if (removed) {
            byName.remove(role.name, role);
            if (auditLog != null) {
                auditLog.log("ROLE_DELETE", "system", role.name, "");
            }
        }
        return removed;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public int count() {
        return byId.size();
    }

    @Override
    public void clear() {
        byId.clear();
        byName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        return byId.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = new ArrayList<>(byId.values());
        if (filter != null) {
            result.removeIf(r -> !filter.test(r));
        }
        if (sorter != null) {
            result.sort(sorter);
        } else {
            result.sort(Comparator.comparing(Role::name));
        }
        return result;
    }

    public boolean exists(String name) {
        return name != null && byName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = byName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("role does not exist: " + roleName);
        }
        if (permission == null) {
            throw new IllegalArgumentException("permission cannot be null");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = byName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("role does not exist: " + roleName);
        }
        if (permission == null) {
            throw new IllegalArgumentException("permission cannot be null");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return byId.values().stream()
                .filter(r -> r.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    public void updateRole(String currentName, String newName, String newDescription) {
        ValidationUtils.requireNonEmpty(currentName, "currentName");
        ValidationUtils.requireNonEmpty(newName, "newName");
        ValidationUtils.requireNonEmpty(newDescription, "newDescription");

        Role role = byName.get(currentName);
        if (role == null) {
            throw new IllegalArgumentException("role does not exist: " + currentName);
        }
        if (!currentName.equals(newName) && byName.containsKey(newName)) {
            throw new IllegalArgumentException("role with name '" + newName + "' already exists");
        }

        String oldName = role.name;
        role.name = newName;
        role.description = newDescription;

        if (!oldName.equals(newName)) {
            byName.remove(oldName);
            byName.put(newName, role);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return Objects.equals(byId, that.byId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(byId);
    }
}
