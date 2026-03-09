package com.nmichail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        String[] headers = {"Metric", "Value"};
        String[][] data = {
                {"Users", String.valueOf(userManager.count())},
                {"Roles", String.valueOf(roleManager.count())},
                {"Assignments", String.valueOf(assignmentManager.count())}
        };
        String table = FormatUtils.formatTable(headers, java.util.List.of(data[0], data[1], data[2]));
        return FormatUtils.formatHeader("RBAC statistics") + System.lineSeparator() + System.lineSeparator() + table;
    }

    public void saveToFile(String filename) throws IOException {
        Path path = Path.of(filename);
        List<String> lines = new ArrayList<>();

        for (User u : userManager.findAll()) {
            lines.add("USER|" + u.username() + "|" + u.fullName() + "|" + u.email());
        }
        for (Role r : roleManager.findAll()) {
            lines.add("ROLE|" + r.name + "|" + r.description);
            for (Permission p : r.getPermissions()) {
                lines.add("PERM|" + r.name + "|" + p.name() + "|" + p.resource() + "|" + p.description());
            }
        }
        for (RoleAssignment a : assignmentManager.findAll()) {
            String type = a.assignmentType();
            String expires = "";
            String autoRenew = "";
            if (a instanceof TemporaryAssignment ta) {
                expires = ta.expiresAt();
                autoRenew = String.valueOf(ta.autoRenew());
            }
            AssignmentMetadata m = a.metadata();
            lines.add("ASSIGN|" + type
                    + "|" + a.user().username()
                    + "|" + a.role().name
                    + "|" + expires
                    + "|" + autoRenew
                    + "|" + m.assignedBy()
                    + "|" + m.assignedAt()
                    + "|" + m.reason());
        }

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public void loadFromFile(String filename) throws IOException {
        Path path = Path.of(filename);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filename);
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        userManager.clear();
        roleManager.clear();
        assignmentManager.clear();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2) continue;
            String tag = parts[0];
            if ("USER".equals(tag) && parts.length >= 4) {
                String username = parts[1].trim();
                String fullName = parts[2].trim();
                String email = parts[3].trim();
                userManager.add(User.validate(username, fullName, email));
            } else if ("ROLE".equals(tag) && parts.length >= 3) {
                String name = parts[1].trim();
                String desc = parts[2].trim();
                Role role = new Role(name, desc, new HashSet<>());
                roleManager.add(role);
            } else if ("PERM".equals(tag) && parts.length >= 5) {
                String roleName = parts[1].trim();
                String pname = parts[2].trim();
                String resource = parts[3].trim();
                String desc = parts[4].trim();
                if (roleManager.exists(roleName)) {
                    roleManager.addPermissionToRole(roleName, new Permission(pname, resource, desc));
                }
            } else if ("ASSIGN".equals(tag) && parts.length >= 9) {
                String type = parts[1].trim();
                String username = parts[2].trim();
                String roleName = parts[3].trim();
                String expires = parts[4].trim();
                String autoRenew = parts[5].trim();
                String assignedBy = parts[6].trim();
                String assignedAt = parts[7].trim();
                String reason = parts[8].trim();

                User user = userManager.findByUsername(username).orElse(null);
                Role role = roleManager.findByName(roleName).orElse(null);
                if (user == null || role == null) continue;
                AssignmentMetadata meta = new AssignmentMetadata(assignedBy, assignedAt, reason);
                RoleAssignment a;
                if ("TEMPORARY".equals(type)) {
                    boolean ar = Boolean.parseBoolean(autoRenew);
                    a = new TemporaryAssignment(user, role, meta, expires, ar);
                } else {
                    a = new PermanentAssignment(user, role, meta);
                }
                assignmentManager.add(a);
            }
        }
    }
}