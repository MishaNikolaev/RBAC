package com.nmichail;

import java.util.*;

public final class CommandRegistry {

    private CommandRegistry() {
    }

    public static void registerAll(CommandParser parser) {
        Objects.requireNonNull(parser, "parser");

        parser.registerCommand("user-list", "List users",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    List<User> users = um.findAll();
                    if (users.isEmpty()) {
                        System.out.println("No users");
                        return;
                    }
                    String[] headers = {"Username", "Full name", "Email"};
                    List<String[]> rows = new ArrayList<>();
                    for (User u : users) {
                        rows.add(new String[]{u.username(), u.fullName(), u.email()});
                    }
                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("user-create", "Create new user",
                (scanner, system) -> {
                    try {
                        String username = ConsoleUtils.promptString(scanner, "Username", true);
                        String fullName = ConsoleUtils.promptString(scanner, "Full name", true);
                        String email = ConsoleUtils.promptString(scanner, "Email", true);
                        User user = User.validate(username, fullName, email);
                        system.getUserManager().add(user);
                        ConsoleUtils.printBox("User created", user.format());
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Error creating user", e.getMessage());
                    }
                });

        parser.registerCommand("user-view", "View user details with roles and permissions",
                (scanner, system) -> {
                    String username = ConsoleUtils.promptString(scanner, "Username", true);
                    UserManager um = system.getUserManager();
                    AssignmentManager am = system.getAssignmentManager();
                    Optional<User> userOpt = um.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        ConsoleUtils.printBox("User not found", "Username: " + username);
                        return;
                    }
                    User user = userOpt.get();
                    StringBuilder sb = new StringBuilder();
                    sb.append(user.format()).append("\n\n");

                    List<RoleAssignment> assignments = am.findByUser(user);
                    if (assignments.isEmpty()) {
                        sb.append("Assignments: none\n");
                    } else {
                        sb.append("Assignments:\n");
                        for (RoleAssignment a : assignments) {
                            sb.append(" - ")
                                    .append(a.role().name)
                                    .append(" [").append(a.assignmentType()).append("] ")
                                    .append(a.isActive() ? "(active)" : "(inactive)")
                                    .append("\n");
                        }
                    }

                    Set<Permission> perms = am.getUserPermissions(user);
                    if (perms.isEmpty()) {
                        sb.append("\nPermissions: none");
                    } else {
                        sb.append("\nPermissions:\n");
                        Map<String, List<Permission>> byRes = new TreeMap<>();
                        for (Permission p : perms) {
                            byRes.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                        }
                        for (Map.Entry<String, List<Permission>> e : byRes.entrySet()) {
                            sb.append("  ").append(e.getKey()).append(":\n");
                            for (Permission p : e.getValue()) {
                                sb.append("    - ").append(p.name()).append(" : ").append(p.description()).append("\n");
                            }
                        }
                    }
                    ConsoleUtils.printBox("User details", sb.toString());
                });

        parser.registerCommand("user-update", "Update user full name and email",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    String username = ConsoleUtils.promptString(scanner, "Username", true);
                    if (!um.exists(username)) {
                        ConsoleUtils.printBox("User not found", "Username: " + username);
                        return;
                    }
                    String fullName = ConsoleUtils.promptString(scanner, "New full name", true);
                    String email = ConsoleUtils.promptString(scanner, "New email", true);
                    try {
                        um.update(username, fullName, email);
                        ConsoleUtils.printBox("User updated", username);
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Update failed", e.getMessage());
                    }
                });

        parser.registerCommand("user-delete", "Delete user and all assignments",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    AssignmentManager am = system.getAssignmentManager();
                    String username = ConsoleUtils.promptString(scanner, "Username", true);
                    Optional<User> userOpt = um.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        ConsoleUtils.printBox("User not found", "Username: " + username);
                        return;
                    }
                    boolean confirm = ConsoleUtils.promptYesNo(scanner,
                            "Are you sure you want to delete user '" + username + "'?");
                    if (!confirm) {
                        ConsoleUtils.printBox("Cancelled", "User not deleted");
                        return;
                    }
                    User user = userOpt.get();
                    for (RoleAssignment a : new ArrayList<>(am.findByUser(user))) {
                        am.revokeAssignment(a.assignmentId());
                    }
                    um.remove(user);
                    ConsoleUtils.printBox("User deleted", username);
                });

        parser.registerCommand("user-search", "Search users by filters (username/email/domain/full name)",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    if (um.count() == 0) {
                        ConsoleUtils.printBox("User search", "No users in system.");
                        return;
                    }

                    List<String> options = List.of(
                            "Username contains",
                            "Email contains",
                            "Email domain",
                            "Full name contains"
                    );
                    String choice = ConsoleUtils.promptChoice(scanner, "Choose user filter", options);

                    UserFilter filter;
                    switch (choice) {
                        case "Username contains" -> {
                            String term = ConsoleUtils.promptString(scanner, "Substring in username", true);
                            filter = UserFilters.byUsernameContains(term);
                        }
                        case "Email contains" -> {
                            String term = ConsoleUtils.promptString(scanner, "Substring in email", true);
                            filter = user -> {
                                String norm = ValidationUtils.normalizeString(term).toLowerCase();
                                return user.email().toLowerCase().contains(norm);
                            };
                        }
                        case "Email domain" -> {
                            String domain = ConsoleUtils.promptString(scanner, "Email domain", true);
                            filter = UserFilters.byEmailDomain(domain);
                        }
                        case "Full name contains" -> {
                            String term = ConsoleUtils.promptString(scanner, "Substring in full name", true);
                            filter = UserFilters.byFullNameContains(term);
                        }
                        default -> {
                            ConsoleUtils.printBox("User search", "Unknown filter choice: " + choice);
                            return;
                        }
                    }

                    List<User> result = um.findByFilter(filter);
                    if (result.isEmpty()) {
                        ConsoleUtils.printBox("User search", "No users matched selected filter.");
                        return;
                    }

                    String[] headers = {"Username", "Full name", "Email"};
                    List<String[]> rows = new ArrayList<>();
                    for (User u : result) {
                        rows.add(new String[]{u.username(), u.fullName(), u.email()});
                    }
                    String table = FormatUtils.formatTable(headers, rows);
                    ConsoleUtils.printBox("User search results", table);
                });

        parser.registerCommand("role-list", "List all roles",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    List<Role> roles = rm.findAll();
                    if (roles.isEmpty()) {
                        System.out.println("No roles.");
                        return;
                    }
                    String[] headers = {"Name", "Description", "ID", "Permissions"};
                    List<String[]> rows = new ArrayList<>();
                    for (Role r : roles) {
                        rows.add(new String[]{
                                r.name,
                                FormatUtils.truncate(r.description, 40),
                                r.id,
                                String.valueOf(r.getPermissions().size())
                        });
                    }
                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("role-create", "Create new role",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    String name = ConsoleUtils.promptString(scanner, "Role name", true);
                    String description = ConsoleUtils.promptString(scanner, "Description", true);
                    Set<Permission> perms = new HashSet<>();
                    while (ConsoleUtils.promptYesNo(scanner, "Add permission to this role?")) {
                        String pname = ConsoleUtils.promptString(scanner, "Permission name", true);
                        String resource = ConsoleUtils.promptString(scanner, "Resource", true);
                        String desc = ConsoleUtils.promptString(scanner, "Permission description", true);
                        try {
                            perms.add(new Permission(pname, resource, desc));
                        } catch (Exception e) {
                            ConsoleUtils.printBox("Permission invalid", e.getMessage());
                        }
                    }
                    try {
                        Role role = new Role(name, description, perms);
                        rm.add(role);
                        ConsoleUtils.printBox("Role created", role.format());
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Role create failed", e.getMessage());
                    }
                });

        parser.registerCommand("role-view", "View role details",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    String name = ConsoleUtils.promptString(scanner, "Role name", true);
                    Optional<Role> roleOpt = rm.findByName(name);
                    if (roleOpt.isEmpty()) {
                        ConsoleUtils.printBox("Role not found", name);
                        return;
                    }
                    ConsoleUtils.printBox("Role", roleOpt.get().format());
                });

        parser.registerCommand("role-update", "Update role name and description",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    String currentName = ConsoleUtils.promptString(scanner, "Current role name", true);
                    Optional<Role> roleOpt = rm.findByName(currentName);
                    if (roleOpt.isEmpty()) {
                        ConsoleUtils.printBox("Role not found", currentName);
                        return;
                    }
                    Role role = roleOpt.get();
                    String newName = ConsoleUtils.promptString(scanner, "New role name", true);
                    String newDescription = ConsoleUtils.promptString(scanner, "New description", true);
                    try {
                        rm.updateRole(currentName, newName, newDescription);
                        ConsoleUtils.printBox("Role updated",
                                "Old name: " + currentName + "\nNew name: " + role.name + "\nDescription: " + role.description);
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Role update failed", e.getMessage());
                    }
                });

        parser.registerCommand("role-delete", "Delete role if not assigned",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    String name = ConsoleUtils.promptString(scanner, "Role name", true);
                    Optional<Role> roleOpt = rm.findByName(name);
                    if (roleOpt.isEmpty()) {
                        ConsoleUtils.printBox("Role not found", name);
                        return;
                    }
                    Role role = roleOpt.get();
                    try {
                        boolean confirm = ConsoleUtils.promptYesNo(scanner,
                                "Delete role '" + name + "'? This may affect assignments.");
                        if (!confirm) {
                            ConsoleUtils.printBox("Cancelled", "Role not deleted");
                            return;
                        }
                        rm.remove(role);
                        ConsoleUtils.printBox("Role deleted", name);
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Cannot delete role", e.getMessage());
                    }
                });

        parser.registerCommand("role-add-permission", "Add permission to role",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    String roleName = ConsoleUtils.promptString(scanner, "Role name", true);
                    String pname = ConsoleUtils.promptString(scanner, "Permission name", true);
                    String resource = ConsoleUtils.promptString(scanner, "Resource", true);
                    String desc = ConsoleUtils.promptString(scanner, "Permission description", true);
                    try {
                        Permission p = new Permission(pname, resource, desc);
                        rm.addPermissionToRole(roleName, p);
                        ConsoleUtils.printBox("Permission added", p.format());
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Add failed", e.getMessage());
                    }
                });

        parser.registerCommand("role-remove-permission", "Remove permission from role",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    String roleName = ConsoleUtils.promptString(scanner, "Role name", true);
                    Optional<Role> roleOpt = rm.findByName(roleName);
                    if (roleOpt.isEmpty()) {
                        ConsoleUtils.printBox("Role not found", roleName);
                        return;
                    }
                    Role role = roleOpt.get();
                    List<Permission> perms = new ArrayList<>(role.getPermissions());
                    if (perms.isEmpty()) {
                        ConsoleUtils.printBox("No permissions", "Role has no permissions.");
                        return;
                    }
                    for (int i = 0; i < perms.size(); i++) {
                        System.out.println("  " + (i + 1) + ". " + perms.get(i).format());
                    }
                    int index = ConsoleUtils.promptInt(scanner, "Choose permission number", 1, perms.size());
                    Permission toRemove = perms.get(index - 1);
                    rm.removePermissionFromRole(roleName, toRemove);
                    ConsoleUtils.printBox("Permission removed", toRemove.format());
                });

        parser.registerCommand("role-search", "Search roles by name or permission",
                (scanner, system) -> ConsoleUtils.printBox("Not implemented",
                        "Role search filters are not implemented yet."));

        parser.registerCommand("assign-role", "Assign role to user (permanent or temporary)",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    RoleManager rm = system.getRoleManager();
                    AssignmentManager am = system.getAssignmentManager();

                    String username = ConsoleUtils.promptString(scanner, "Username", true);
                    Optional<User> userOpt = um.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        ConsoleUtils.printBox("User not found", username);
                        return;
                    }
                    List<Role> roles = rm.findAll();
                    if (roles.isEmpty()) {
                        ConsoleUtils.printBox("No roles", "Create roles first.");
                        return;
                    }
                    Role chosen = ConsoleUtils.promptChoice(scanner, "Choose role", roles);
                    boolean temporary = ConsoleUtils.promptYesNo(scanner, "Temporary assignment?");
                    String reason = ConsoleUtils.promptString(scanner, "Reason", false);
                    AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);
                    RoleAssignment assignment;
                    if (temporary) {
                        String date = ConsoleUtils.promptString(scanner, "Expiration date (YYYY-MM-DD)", true);
                        assignment = new TemporaryAssignment(userOpt.get(), chosen, meta, date, false);
                    } else {
                        assignment = new PermanentAssignment(userOpt.get(), chosen, meta);
                    }
                    am.add(assignment);
                    ConsoleUtils.printBox(
                            "Assignment created",
                            assignment.assignmentType() + " " + assignment.role().name + " -> " + assignment.user().username()
                    );
                });

        parser.registerCommand("revoke-role", "Revoke role assignment by ID",
                (scanner, system) -> {
                    AssignmentManager am = system.getAssignmentManager();
                    String id = ConsoleUtils.promptString(scanner, "Assignment ID", true);
                    try {
                        am.revokeAssignment(id);
                        ConsoleUtils.printBox("Assignment revoked", id);
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Revoke failed", e.getMessage());
                    }
                });

        parser.registerCommand("assignment-list", "List all assignments",
                (scanner, system) -> {
                    AssignmentManager am = system.getAssignmentManager();
                    List<RoleAssignment> list = am.findAll();
                    if (list.isEmpty()) {
                        System.out.println("No assignments.");
                        return;
                    }
                    String[] headers = {"ID", "User", "Role", "Type", "Status"};
                    List<String[]> rows = new ArrayList<>();
                    for (RoleAssignment a : list) {
                        rows.add(new String[]{
                                a.assignmentId(),
                                a.user().username(),
                                a.role().name,
                                a.assignmentType(),
                                a.isActive() ? "active" : "inactive"
                        });
                    }
                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("assignment-active", "List active assignments",
                (scanner, system) -> {
                    AssignmentManager am = system.getAssignmentManager();
                    List<RoleAssignment> list = am.getActiveAssignments();
                    if (list.isEmpty()) {
                        System.out.println("No active assignments.");
                        return;
                    }
                    String[] headers = {"ID", "User", "Role", "Type"};
                    List<String[]> rows = new ArrayList<>();
                    for (RoleAssignment a : list) {
                        rows.add(new String[]{
                                a.assignmentId(),
                                a.user().username(),
                                a.role().name,
                                a.assignmentType()
                        });
                    }
                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("assignment-expired", "List expired (inactive) assignments",
                (scanner, system) -> {
                    AssignmentManager am = system.getAssignmentManager();
                    List<RoleAssignment> list = am.getExpiredAssignments();
                    if (list.isEmpty()) {
                        System.out.println("No expired assignments.");
                        return;
                    }
                    String[] headers = {"ID", "User", "Role", "Type"};
                    List<String[]> rows = new ArrayList<>();
                    for (RoleAssignment a : list) {
                        rows.add(new String[]{
                                a.assignmentId(),
                                a.user().username(),
                                a.role().name,
                                a.assignmentType()
                        });
                    }
                    System.out.println(FormatUtils.formatTable(headers, rows));
                });

        parser.registerCommand("assignment-extend", "Extend temporary assignment",
                (scanner, system) -> {
                    AssignmentManager am = system.getAssignmentManager();
                    String id = ConsoleUtils.promptString(scanner, "Assignment ID", true);
                    String newDate = ConsoleUtils.promptString(scanner, "New expiration date", true);
                    try {
                        am.extendTemporaryAssignment(id, newDate);
                        ConsoleUtils.printBox("Assignment extended", id + " -> " + newDate);
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Extend failed", e.getMessage());
                    }
                });

        parser.registerCommand("assignment-list-user", "List assignments for specific user",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    AssignmentManager am = system.getAssignmentManager();
                    String username = ConsoleUtils.promptString(scanner, "Username", true);
                    Optional<User> userOpt = um.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        ConsoleUtils.printBox("User not found", username);
                        return;
                    }
                    List<RoleAssignment> list = am.findByUser(userOpt.get());
                    if (list.isEmpty()) {
                        ConsoleUtils.printBox("Assignments", "No assignments for user " + username);
                        return;
                    }
                    String[] headers = {"ID", "Role", "Type", "Status", "Assigned by"};
                    List<String[]> rows = new ArrayList<>();
                    for (RoleAssignment a : list) {
                        rows.add(new String[]{
                                a.assignmentId(),
                                a.role().name,
                                a.assignmentType(),
                                a.isActive() ? "active" : "inactive",
                                a.metadata().assignedBy()
                        });
                    }
                    String table = FormatUtils.formatTable(headers, rows);
                    ConsoleUtils.printBox("Assignments for " + username, table);
                });

        parser.registerCommand("assignment-list-role", "List users for specific role",
                (scanner, system) -> {
                    RoleManager rm = system.getRoleManager();
                    AssignmentManager am = system.getAssignmentManager();
                    String roleName = ConsoleUtils.promptString(scanner, "Role name", true);
                    Optional<Role> roleOpt = rm.findByName(roleName);
                    if (roleOpt.isEmpty()) {
                        ConsoleUtils.printBox("Role not found", roleName);
                        return;
                    }
                    Role role = roleOpt.get();
                    List<RoleAssignment> list = am.findByRole(role);
                    if (list.isEmpty()) {
                        ConsoleUtils.printBox("Assignments", "No users have role " + roleName);
                        return;
                    }
                    String[] headers = {"User", "Assignment ID", "Type", "Status"};
                    List<String[]> rows = new ArrayList<>();
                    for (RoleAssignment a : list) {
                        rows.add(new String[]{
                                a.user().username(),
                                a.assignmentId(),
                                a.assignmentType(),
                                a.isActive() ? "active" : "inactive"
                        });
                    }
                    String table = FormatUtils.formatTable(headers, rows);
                    ConsoleUtils.printBox("Users with role " + roleName, table);
                });

        parser.registerCommand("assignment-search", "Search assignments by filters",
                (scanner, system) -> {
                    AssignmentManager am = system.getAssignmentManager();
                    if (am.count() == 0) {
                        ConsoleUtils.printBox("Assignment search", "No assignments in system.");
                        return;
                    }

                    List<String> options = List.of(
                            "By user",
                            "By role",
                            "By type (PERMANENT/TEMPORARY)",
                            "Active only",
                            "Inactive only"
                    );
                    String choice = ConsoleUtils.promptChoice(scanner, "Choose assignment filter", options);

                    AssignmentFilter filter;
                    switch (choice) {
                        case "By user" -> {
                            String username = ConsoleUtils.promptString(scanner, "Username", true);
                            filter = AssignmentFilters.byUserName(username);
                        }
                        case "By role" -> {
                            String roleName = ConsoleUtils.promptString(scanner, "Role name", true);
                            filter = AssignmentFilters.byRoleName(roleName);
                        }
                        case "By type (PERMANENT/TEMPORARY)" -> {
                            String type = ConsoleUtils.promptString(scanner, "Type (PERMANENT or TEMPORARY)", true);
                            filter = AssignmentFilters.byType(type.toUpperCase(Locale.ROOT));
                        }
                        case "Active only" -> filter = AssignmentFilters.activeOnly();
                        case "Inactive only" -> filter = AssignmentFilters.inactiveOnly();
                        default -> {
                            ConsoleUtils.printBox("Assignment search", "Unknown filter: " + choice);
                            return;
                        }
                    }

                    List<RoleAssignment> list = am.findByFilter(filter);
                    if (list.isEmpty()) {
                        ConsoleUtils.printBox("Assignment search", "No assignments matched the filter.");
                        return;
                    }
                    String[] headers = {"ID", "User", "Role", "Type", "Status"};
                    List<String[]> rows = new ArrayList<>();
                    for (RoleAssignment a : list) {
                        rows.add(new String[]{
                                a.assignmentId(),
                                a.user().username(),
                                a.role().name,
                                a.assignmentType(),
                                a.isActive() ? "active" : "inactive"
                        });
                    }
                    String table = FormatUtils.formatTable(headers, rows);
                    ConsoleUtils.printBox("Assignment search results", table);
                });

        parser.registerCommand("permissions-user", "List permissions of specific user",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    AssignmentManager am = system.getAssignmentManager();
                    String username = ConsoleUtils.promptString(scanner, "Username", true);
                    Optional<User> userOpt = um.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        ConsoleUtils.printBox("User not found", username);
                        return;
                    }
                    User user = userOpt.get();
                    Set<Permission> perms = am.getUserPermissions(user);
                    if (perms.isEmpty()) {
                        ConsoleUtils.printBox("Permissions", "No permissions for user " + username);
                        return;
                    }
                    Map<String, List<Permission>> byRes = new TreeMap<>();
                    for (Permission p : perms) {
                        byRes.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, List<Permission>> e : byRes.entrySet()) {
                        sb.append(e.getKey()).append(":\n");
                        for (Permission p : e.getValue()) {
                            sb.append("  - ").append(p.name()).append(" : ").append(p.description()).append("\n");
                        }
                    }
                    ConsoleUtils.printBox("Permissions for " + username, sb.toString());
                });

        parser.registerCommand("permissions-check", "Check if user has specific permission",
                (scanner, system) -> {
                    UserManager um = system.getUserManager();
                    AssignmentManager am = system.getAssignmentManager();
                    String username = ConsoleUtils.promptString(scanner, "Username", true);
                    Optional<User> userOpt = um.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        ConsoleUtils.printBox("User not found", username);
                        return;
                    }
                    String pname = ConsoleUtils.promptString(scanner, "Permission name", true);
                    String resource = ConsoleUtils.promptString(scanner, "Resource", true);
                    boolean has = am.userHasPermission(userOpt.get(), pname, resource);
                    String msg = has ? "User HAS permission" : "User DOES NOT have permission";
                    ConsoleUtils.printBox("Permission check", msg);
                });

        parser.registerCommand("help", "Show help for commands",
                (scanner, system) -> parser.printHelp());

        parser.registerCommand("stats", "Show RBAC statistics",
                (scanner, system) -> {
                    AssignmentManager am = system.getAssignmentManager();
                    String base = system.generateStatistics();
                    long active = am.getActiveAssignments().size();
                    long expired = am.getExpiredAssignments().size();
                    List<RoleAssignment> all = am.findAll();
                    UserManager um = system.getUserManager();

                    double avgRolesPerUser = um.count() == 0 ? 0.0 : (double) all.size() / um.count();

                    Map<String, Long> byRole = new HashMap<>();
                    for (RoleAssignment a : all) {
                        byRole.merge(a.role().name, 1L, Long::sum);
                    }
                    List<Map.Entry<String, Long>> top = byRole.entrySet().stream()
                            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                            .limit(3)
                            .toList();

                    StringBuilder extra = new StringBuilder();
                    extra.append("Active assignments: ").append(active).append("\n");
                    extra.append("Expired assignments: ").append(expired).append("\n");
                    extra.append("Average roles per user: ").append(String.format(Locale.ROOT, "%.2f", avgRolesPerUser)).append("\n");
                    extra.append("Top roles by assignments:\n");
                    if (top.isEmpty()) {
                        extra.append("  (no roles assigned)\n");
                    } else {
                        for (Map.Entry<String, Long> e : top) {
                            extra.append("  - ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
                        }
                    }

                    ConsoleUtils.printBox("RBAC statistics", base + "\n\n" + extra);
                });

        parser.registerCommand("clear", "Clear screen",
                (scanner, system) -> {
                    for (int i = 0; i < 30; i++) {
                        System.out.println();
                    }
                });

        parser.registerCommand("exit", "Exit program",
                (scanner, system) -> {
                    boolean confirm = ConsoleUtils.promptYesNo(scanner, "Exit program?");
                    if (!confirm) {
                        ConsoleUtils.printBox("Exit cancelled", "");
                        return;
                    }
                    ConsoleUtils.printBox("Goodbye", "Exiting RBAC console.");
                    System.exit(0);
                });

        parser.registerCommand("save", "Save data to file",
                (scanner, system) -> {
                    String filename = ConsoleUtils.promptString(scanner, "Save to file", true);
                    try {
                        system.saveToFile(filename);
                        ConsoleUtils.printBox("Save successful", "Data saved to " + filename);
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Save failed", e.getMessage());
                    }
                });

        parser.registerCommand("load", "Load data from file",
                (scanner, system) -> {
                    String filename = ConsoleUtils.promptString(scanner, "Load from file", true);
                    try {
                        system.loadFromFile(filename);
                        ConsoleUtils.printBox("Load successful", "Data loaded from " + filename);
                    } catch (Exception e) {
                        ConsoleUtils.printBox("Load failed", e.getMessage());
                    }
                });
    }
}