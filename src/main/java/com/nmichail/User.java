package com.nmichail;

public record User(
        String username,
        String fullName,
        String email
){
    private static final String USERNAME_REGEX = "[a-zA-Z0-9_]{3,20}";
    private static final String EMAIL_REGEX = ".*@[^@]*\\.[^@]*";

    public static User validate(String username, String fullName, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        if (!username.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("you should use only latin symbols, digits, underscore, length 3-20 symbols");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("email must contain @ and a dot after @");
        }
        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }


}