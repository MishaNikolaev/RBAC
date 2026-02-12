package com.nmichail;

import java.util.HashSet;
import java.util.Set;

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

        Permission readUsers = new Permission("READ", "users", "Can view user list");
        System.out.println("\nPermission format(): " + readUsers.format());

        Set<Permission> perms = Set.of(
                new Permission("READ", "users", "Can view user list"),
                new Permission("WRITE", "users", "Can create and edit users"),
                new Permission("DELETE", "users", "Can delete users")
        );
        Role admin = new Role("Administrator", "Full system access", new HashSet(perms));
        System.out.println("\n" + admin.format());
    }
}