package com.nmichail;

public class RoleFilters {

    public static RoleFilter byName(String name) {
        ValidationUtils.requireNonEmpty(name, "name");
        return role -> role.name().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        ValidationUtils.requireNonEmpty(substring, "substring");
        return role -> role.name().toLowerCase()
                .contains(ValidationUtils.normalizeString(substring).toLowerCase());
    }

    public static RoleFilter hasPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission cannot be null");
        }
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        ValidationUtils.requireNonEmpty(permissionName, "permissionName");
        ValidationUtils.requireNonEmpty(resource, "resource");
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of permissions must be bigger than null");
        }
        return role -> role.getPermissions().size() >= n;
    }
}
