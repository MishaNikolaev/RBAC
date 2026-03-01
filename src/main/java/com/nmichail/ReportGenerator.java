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
        sb.append(FormatUtils.formatHeader("User Report")).append("\n\n");
        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users.\n");
            return sb.toString();
        }
        String[] headers = {"Username", "Full Name", "Email"};
        List<String[]> rows = new ArrayList<>();
        for (User user : users) {
            rows.add(new String[]{user.username(), user.fullName(), user.email()});
        }
        sb.append(FormatUtils.formatTable(headers, rows)).append("\n\n");
        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();
            if (!assignments.isEmpty()) {
                sb.append(String.format("%s — Roles (%d): ", user.username(), assignments.size()));
                sb.append(assignments.stream()
                        .map(a -> a.role().name + " [" + a.assignmentType() + "]")
                        .collect(Collectors.joining(", ")));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("Role Report")).append("\n\n");
        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("No roles.\n");
            return sb.toString();
        }
        String[] headers = {"Role", "Description", "Users assigned", "Permissions"};
        List<String[]> rows = new ArrayList<>();
        for (Role role : roles) {
            long count = assignmentManager.findByRole(role).stream().filter(RoleAssignment::isActive).count();
            rows.add(new String[]{
                    role.name,
                    FormatUtils.truncate(role.description, 40),
                    String.valueOf(count),
                    String.valueOf(role.getPermissions().size())
            });
        }
        sb.append(FormatUtils.formatTable(headers, rows)).append("\n");
        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("Permission matrix")).append("\n\n");
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
        String[] headers = new String[resourceList.size() + 1];
        headers[0] = "User";
        for (int i = 0; i < resourceList.size(); i++) {
            headers[i + 1] = resourceList.get(i);
        }
        List<String[]> rows = new ArrayList<>();
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            Map<String, Set<String>> byResource = perms.stream()
                    .collect(Collectors.groupingBy(Permission::resource,
                            Collectors.mapping(Permission::name, Collectors.toSet())));
            String[] row = new String[headers.length];
            row[0] = user.username();
            for (int i = 0; i < resourceList.size(); i++) {
                Set<String> names = byResource.getOrDefault(resourceList.get(i), Set.of());
                row[i + 1] = names.isEmpty() ? "—" : String.join(",", new TreeSet<>(names));
            }
            rows.add(row);
        }
        sb.append(FormatUtils.formatTable(headers, rows)).append("\n");
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