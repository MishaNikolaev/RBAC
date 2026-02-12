package com.nmichail;

public class Main {
    public static void main(String[] args) {

        User u1 = User.validate("misha_nikolaev", "Misha Nikolaev", "nmichail@example.com");
        System.out.println(u1.format());

        try {
            User.validate(null, "Name", "a@b.co");
        }
        catch (IllegalArgumentException e) {
            System.out.println( e.getMessage());
        }

        try {
            User.validate("username", "Name", null);
        }
        catch (IllegalArgumentException e) {
            System.out.println( e.getMessage());
        }

        try {
            User.validate("user", "Name", "some_email.com");
        }
        catch (IllegalArgumentException e) {
            System.out.println( e.getMessage());
        }
    }
}