package com.nmichail;

public record Permission(
        String name,
        String resource,
        String description
) {
    public Permission {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description cannot be empty");
        }
        name = name.toUpperCase().trim();
        if (name.contains(" ")) {
            throw new IllegalArgumentException("name must not contain spaces");
        }
        resource = resource.toLowerCase().trim();
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