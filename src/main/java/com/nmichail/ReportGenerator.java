package com.nmichail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("User Report\n\n");
        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users.\n");
            return sb.toString();
        }
        for (User user : users) {
            sb.append(String.format("User: %s (%s) <%s>%n", user.username(), user.fullName(), user.email()));
            List<RoleAssignment> assignments = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();
            if (assignments.isEmpty()) {
                sb.append("  Roles: (none)\n");
            } else {
                sb.append(String.format("  Roles (%d):%n", assignments.size()));
                for (RoleAssignment a : assignments) {
                    sb.append(String.format("    - %s [%s]%n", a.role().name, a.assignmentType()));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("Role Report\n\n");
        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("No roles.\n");
            return sb.toString();
        }
        for (Role role : roles) {
            long count = assignmentManager.findByRole(role).stream().filter(RoleAssignment::isActive).count();
            sb.append(String.format("Role: %s [ID: %s]%n", role.name, role.id));
            sb.append(String.format("  Description: %s%n", role.description));
            sb.append(String.format("  Users assigned: %d%n", count));
            sb.append(String.format("  Permissions: %d%n%n", role.getPermissions().size()));
        }
        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("Permission matrix\n\n");
        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users.\n");
            return sb.toString();
        }
        Set<String> resources = new TreeSet<>();
        for (User u : users) {
            assignmentManager.getUserPermissions(u).stream()
                    .map(Permission::resource)
                    .forEach(resources::add);
        }
        if (resources.isEmpty()) {
            sb.append("No permissions or resources.\n");
            return sb.toString();
        }
        List<String> resourceList = new ArrayList<>(resources);
        int colWidth = 18;
        sb.append(String.format("%-" + colWidth + "s", "User"));
        for (String res : resourceList) {
            sb.append(" | ").append(res.length() <= colWidth ? String.format("%-" + colWidth + "s", res) : res.substring(0, colWidth - 1) + "…");
        }
        sb.append("\n");
        sb.append("-".repeat(Math.max(0, colWidth + (colWidth + 3) * resourceList.size()))).append("\n");
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            Map<String, Set<String>> byResource = perms.stream()
                    .collect(Collectors.groupingBy(Permission::resource,
                            Collectors.mapping(Permission::name, Collectors.toSet())));
            String uname = user.username().length() <= colWidth ? String.format("%-" + colWidth + "s", user.username()) : user.username().substring(0, colWidth - 1) + "…";
            sb.append(uname);
            for (String res : resourceList) {
                Set<String> names = byResource.getOrDefault(res, Set.of());
                String cell = names.isEmpty() ? "—" : String.join(",", new TreeSet<>(names));
                if (cell.length() > colWidth) cell = cell.substring(0, colWidth - 1) + "…";
                sb.append(" | ").append(String.format("%-" + colWidth + "s", cell));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void exportToFile(String report, String filename) throws IOException {
        Path path = Path.of(filename);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, report, StandardCharsets.UTF_8);
    }
}