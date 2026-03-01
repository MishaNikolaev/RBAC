package com.nmichail;

public record User(
        String username,
        String fullName,
        String email
){
    public static User validate(String username, String fullName, String email) {
        ValidationUtils.requireNonEmpty(username, "username");
        ValidationUtils.requireNonEmpty(fullName, "fullName");
        ValidationUtils.requireNonEmpty(email, "email");
        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("you should use only latin symbols, digits, underscore, length 3-20 symbols");
        }
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("email must contain @ and a dot after @");
        }
        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }


}