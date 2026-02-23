package com.nmichail;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.unmodifiableSet;

public class Role {
    public final String id;
    public final String name;
    public final String description;
    public final Set<Permission> permissions;

    public Role(String name, String description, Set<Permission> permissions) {
        this.id = "role_" + UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }


    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream().anyMatch(p -> p.matches(permissionName, resource));
    }
    public Set<Permission> getPermissions() {
        return unmodifiableSet(new HashSet<>(permissions));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return format();
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");
        for (Permission p : permissions) {
            sb.append(" - ").append(p.format()).append("\n");
        }
        return sb.toString().trim();
    }
}