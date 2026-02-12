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

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "New team member");
        System.out.println("\nAssignmentMetadata:");
        System.out.println(meta.format());

        AssignmentMetadata fixedMeta = new AssignmentMetadata("admin", "2026-02-07T19:00:00Z", "Initial setup");
        AbstractRoleAssignment assignment = new PermanentAssignment(u1, admin, fixedMeta);
        System.out.println(assignment.summary());

        AssignmentMetadata tempMeta = new AssignmentMetadata("admin", "2026-02-01T10:00:00Z", "Temporary access");
        TemporaryAssignment tempFuture = new TemporaryAssignment(u1, admin, tempMeta, "2026-12-31", false);
        System.out.println("\nTemporaryAssignment (expires 2026-12-31)");
        System.out.println("summary(): " + tempFuture.summary());
        System.out.println("isExpired(): " + tempFuture.isExpired());
        System.out.println("isExpired(\"2026-06-01\"): " + tempFuture.isExpired("2026-06-01"));
        System.out.println("isExpired(\"2027-01-01\"): " + tempFuture.isExpired("2027-01-01"));
        System.out.println("isActive(): " + tempFuture.isActive());
        System.out.println("isAutoRenew(): " + tempFuture.isAutoRenew());
        System.out.println("getTimeRemaining(): " + tempFuture.getTimeRemaining());

        System.out.println("\nextend");
        System.out.println("getExpiresAt: " + tempFuture.getExpiresAt());
        tempFuture.extend("2027-06-30");
        System.out.println("getExpiresAt after extend: " + tempFuture.getExpiresAt());
        System.out.println("getTimeRemaining: " + tempFuture.getTimeRemaining());

        TemporaryAssignment tempPast = new TemporaryAssignment(u1, admin, tempMeta, "2026-01-01", true);
        System.out.println("\nTemporaryAssignment");
        System.out.println("isExpired(): " + tempPast.isExpired());
        System.out.println("isActive(): " + tempPast.isActive());
        System.out.println("isAutoRenew(): " + tempPast.isAutoRenew());
        System.out.println("getTimeRemaining(): " + tempPast.getTimeRemaining());
    }
}