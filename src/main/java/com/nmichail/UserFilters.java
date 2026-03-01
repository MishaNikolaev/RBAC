package com.nmichail;

public class UserFilters {

    public static UserFilter byUsername(String username) {
        ValidationUtils.requireNonEmpty(username, "username");
        return user -> user.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        ValidationUtils.requireNonEmpty(substring, "substring");
        return user -> user.username().toLowerCase()
                .contains(ValidationUtils.normalizeString(substring).toLowerCase());
    }

    public static UserFilter byEmail(String email) {
        ValidationUtils.requireNonEmpty(email, "email");
        return user -> user.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        ValidationUtils.requireNonEmpty(domain, "domain");
        return user -> user.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring) {
        ValidationUtils.requireNonEmpty(substring, "substring");
        return user -> user.fullName().toLowerCase()
                .contains(ValidationUtils.normalizeString(substring).toLowerCase());
    }

}
