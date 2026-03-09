package com.nmichail;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "wizard".equalsIgnoreCase(args[0])) {
            runWizard();
            return;
        }
        if (args.length > 0 && "console".equalsIgnoreCase(args[0])) {
            runConsole();
            return;
        }

        User u1 = User.validate("misha_nikolaev", "Misha Nikolaev", "nmichail@example.com");
        System.out.println(u1.format());

        try {
            User.validate(null, "Name", "a@b.co");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        try {
            User.validate("username", "Name", null);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        try {
            User.validate("user", "Name", "some_email.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
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

    private static void runWizard() {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();
        ReportGenerator reportGenerator = new ReportGenerator();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println();
            ConsoleUtils.printSection("Interactive RBAC");
            ConsoleUtils.printBox("Welcome!", "Create users (saved in session), then view/save reports.");
            System.out.flush();
            runWizardStep(scanner, userManager, roleManager, assignmentManager, reportGenerator);
        }
    }

    private static void runWizardStep(Scanner scanner,
                                      UserManager userManager,
                                      RoleManager roleManager,
                                      AssignmentManager assignmentManager,
                                      ReportGenerator reportGenerator) {

        String username = ConsoleUtils.promptString(scanner, "Username (Latin, 3-20 chars)", true);
        String fullName = ConsoleUtils.promptString(scanner, "Full name", true);
        String email = ConsoleUtils.promptString(scanner, "Email", true);

        User user;
        try {
            user = User.validate(username, fullName, email);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("latin")) {
                msg = msg + "\nUse Latin only";
            }
            ConsoleUtils.printBox("Validation error", msg);
            runWizardStep(scanner, userManager, roleManager, assignmentManager, reportGenerator);
            return;
        }

        try {
            userManager.add(user);
        } catch (IllegalArgumentException e) {
            ConsoleUtils.printBox("Cannot add user", e.getMessage() + "\nTry another username.");
            System.out.flush();
            runWizardStep(scanner, userManager, roleManager, assignmentManager, reportGenerator);
            return;
        }
        ConsoleUtils.printBox("User created and saved", user.format() + "\nTotal users in session: " + userManager.count());
        System.out.flush();

        String reportType = ConsoleUtils.promptChoice(scanner, "Choose report to generate and save:",
                List.of("report-users", "report-roles", "report-matrix"));
        String report;
        String filename;
        switch (reportType) {
            case "report-users" -> {
                report = reportGenerator.generateUserReport(userManager, assignmentManager);
                filename = "build/wizard-report-users.txt";
            }
            case "report-roles" -> {
                report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
                filename = "build/wizard-report-roles.txt";
            }
            default -> {
                report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
                filename = "build/wizard-report-matrix.txt";
            }
        }
        try {
            reportGenerator.exportToFile(report, filename);
            ConsoleUtils.printBox("Report saved", "File: " + filename);
            System.out.println("\n--- Report content ---\n" + report + "\n--- End ---");
        } catch (java.io.IOException e) {
            ConsoleUtils.printBox("Save failed", e.getMessage());
            System.out.println("\n--- Report preview ---\n" + report.substring(0, Math.min(800, report.length())) + (report.length() > 800 ? "\n..." : "") + "\n--- End ---");
        }
        System.out.flush();

        int action = ConsoleUtils.promptInt(scanner, "Action (1=exit, 2=create another user, 3=info)", 1, 3);
        if (action == 1) {
            ConsoleUtils.printBox("Goodbye", "Users this session: " + userManager.count() + ". Run again: ./gradlew run --args=wizard");
            System.out.flush();
            return;
        }
        if (action == 3) {
            ConsoleUtils.printBox("Info", "This wizard uses ConsoleUtils.promptString, promptInt, promptYesNo, promptChoice. Users kept in memory; reports saved to build/.");
            System.out.flush();
        }
        runWizardStep(scanner, userManager, roleManager, assignmentManager, reportGenerator);
    }

    private static void runConsole() {
        RBACSystem system = new RBACSystem();
        system.initialize();
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        System.out.println("RBAC Console. Type 'help' for commands, 'exit' to quit.");
        System.out.println("Tip: run with --console=plain to avoid Gradle progress bar.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("rbac> ");
                System.out.flush();
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine();
                if (line == null) {
                    break;
                }
                parser.parseAndExecute(line, scanner, system);
            }
        }
    }
}