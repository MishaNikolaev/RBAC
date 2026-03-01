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

        AuditLog auditLog = new AuditLog();
        UserManager userManager = new UserManager();
        userManager.setAuditLog(auditLog);
        RoleManager roleManager = new RoleManager();
        roleManager.setAuditLog(auditLog);
        AssignmentManager assignmentManager = new AssignmentManager();
        assignmentManager.setAuditLog(auditLog);

        userManager.add(u1);
        roleManager.add(admin);
        assignmentManager.add(assignment);
        assignmentManager.add(tempFuture);

        ReportGenerator reportGenerator = new ReportGenerator();
        System.out.println("\n Command: report-users");
        String userReport = reportGenerator.generateUserReport(userManager, assignmentManager);
        System.out.println(userReport);
        try {
            reportGenerator.exportToFile(userReport, "build/report-users.txt");
        } catch (java.io.IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }

        System.out.println("Command: report-roles");
        String roleReport = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        System.out.println(roleReport);
        try {
            reportGenerator.exportToFile(roleReport, "build/report-roles.txt");
        } catch (java.io.IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }

        System.out.println("Command: report-matrix");
        String matrixReport = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        System.out.println(matrixReport);
        try {
            reportGenerator.exportToFile(matrixReport, "build/report-matrix.txt");
        } catch (java.io.IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }

        assignmentManager.revokeAssignment(tempFuture.assignmentId());
        roleManager.remove(admin);

        System.out.println("\nCommand: audit-log");
        auditLog.printLog();

        try {
            auditLog.saveToFile("build/audit.log");
            System.out.println("Log saved to build/audit.log");
        } catch (java.io.IOException e) {
            System.out.println("Exception saving log: " + e.getMessage());
        }
    }
}