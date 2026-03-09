package com.nmichail;

import java.util.HashSet;
import java.util.Set;

public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;

    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.auditLog = new AuditLog();

        this.userManager.setAuditLog(auditLog);
        this.roleManager.setAuditLog(auditLog);
        this.assignmentManager.setAuditLog(auditLog);

        this.currentUser = "system";

        this.roleManager.setRoleAssignedChecker(role ->
                assignmentManager.findByRole(role).stream().anyMatch(RoleAssignment::isActive));
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setCurrentUser(String username) {
        if (username == null || username.isBlank()) {
            this.currentUser = "system";
        } else {
            this.currentUser = username;
        }
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        userManager.clear();
        roleManager.clear();
        assignmentManager.clear();

        Permission readUsers = new Permission("READ", "users", "Can view users");
        Permission writeUsers = new Permission("WRITE", "users", "Can create and edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");

        Permission readRoles = new Permission("READ", "roles", "Can view roles");
        Permission writeRoles = new Permission("WRITE", "roles", "Can create and edit roles");
        Permission deleteRoles = new Permission("DELETE", "roles", "Can delete roles");

        Permission readReports = new Permission("READ", "reports", "Can view reports");

        Set<Permission> adminPerms = new HashSet<>();
        adminPerms.add(readUsers);
        adminPerms.add(writeUsers);
        adminPerms.add(deleteUsers);
        adminPerms.add(readRoles);
        adminPerms.add(writeRoles);
        adminPerms.add(deleteRoles);
        adminPerms.add(readReports);

        Set<Permission> managerPerms = new HashSet<>();
        managerPerms.add(readUsers);
        managerPerms.add(writeUsers);
        managerPerms.add(readRoles);
        managerPerms.add(readReports);

        Set<Permission> viewerPerms = new HashSet<>();
        viewerPerms.add(readUsers);
        viewerPerms.add(readRoles);
        viewerPerms.add(readReports);

        Role adminRole = new Role("Admin", "System administrator", adminPerms);
        Role managerRole = new Role("Manager", "Manager with limited write access", managerPerms);
        Role viewerRole = new Role("Viewer", "Read-only access", viewerPerms);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        User adminUser = User.validate("admin", "RBAC Administrator", "admin@example.com");
        userManager.add(adminUser);

        AssignmentMetadata metadata = AssignmentMetadata.now(currentUser, "Initial admin setup");
        RoleAssignment adminAssignment = new PermanentAssignment(adminUser, adminRole, metadata);
        assignmentManager.add(adminAssignment);
    }

    public String generateStatistics() {
        String[] headers = {"CoolThing", "Value"};
        String[][] data = {
                {"Users", String.valueOf(userManager.count())},
                {"Roles", String.valueOf(roleManager.count())},
                {"Assignments", String.valueOf(assignmentManager.count())}
        };
        String table = FormatUtils.formatTable(headers, java.util.List.of(data[0], data[1], data[2]));
        return FormatUtils.formatHeader("RBAC statistics") + System.lineSeparator() + System.lineSeparator() + table;
    }
}