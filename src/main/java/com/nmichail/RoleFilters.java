package com.nmichail;

public class RoleFilters {

    public static RoleFilter byName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }
        return role -> role.name().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        if (substring == null || substring.isBlank()) {
            throw new IllegalArgumentException("substring cannot be null or blank");
        }
        return role -> role.name().toLowerCase()
                .contains(substring.toLowerCase());
    }


    public static RoleFilter hasPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission cannot be null");
        }
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource.isBlank()) {
            throw new IllegalArgumentException("permissonName cannot be null and resource cannot be blank");
        }
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of permissions must be bigger than null");
        }
        return role -> role.getPermissions().size() >= n;
    }
}
