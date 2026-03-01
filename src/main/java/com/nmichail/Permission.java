package com.nmichail;

public record Permission(
        String name,
        String resource,
        String description
) {
    public Permission {
        ValidationUtils.requireNonEmpty(name, "name");
        ValidationUtils.requireNonEmpty(resource, "resource");
        ValidationUtils.requireNonEmpty(description, "description");
        name = ValidationUtils.normalizeString(name).toUpperCase();
        if (name.contains(" ")) {
            throw new IllegalArgumentException("name must not contain spaces");
        }
        resource = ValidationUtils.normalizeString(resource).toLowerCase();
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        String np = (namePattern == null || namePattern.isBlank()) ? ".*" : namePattern;
        String rp = (resourcePattern == null || resourcePattern.isBlank()) ? ".*" : resourcePattern;
        return name.matches(np) && resource.matches(rp);
    }
}