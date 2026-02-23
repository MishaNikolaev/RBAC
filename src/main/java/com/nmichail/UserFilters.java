package com.nmichail;

public class UserFilters {

    public static UserFilter byUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }
        return user -> user.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        if (substring == null || substring.isBlank()) {
            throw new IllegalArgumentException("substring cannot be null or blank");
        }
        return user -> user.username().toLowerCase()
                .contains(substring.toLowerCase());
    }


    public static UserFilter byEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        return user -> user.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain cannot be null or blank");
        }
        return user -> user.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring) {
        if (substring == null || substring.isBlank()) {
            throw new IllegalArgumentException("substring cannot be null or blank");
        }
        return user -> user.fullName().toLowerCase()
                .contains(substring.toLowerCase());
    }

}
