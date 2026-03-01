package com.nmichail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportGenerator")
class ReportGeneratorTest {

    private ReportGenerator reportGenerator;
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        reportGenerator = new ReportGenerator();
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager();
    }

    @Nested
    @DisplayName("generateUserReport")
    class GenerateUserReport {
        @Test
        void generateUserReport_emptyManagers_EXPECT_noUsersMessage() {
            String report = reportGenerator.generateUserReport(userManager, assignmentManager);
            assertTrue(report.contains("User Report"));
            assertTrue(report.contains("No users"));
        }

        @Test
        void generateUserReport_withUsers_EXPECT_tableWithUsernames() {
            userManager.add(User.validate("admin", "Admin User", "admin@test.com"));
            userManager.add(User.validate("john", "John Doe", "john@test.com"));
            String report = reportGenerator.generateUserReport(userManager, assignmentManager);
            assertTrue(report.contains("admin"));
            assertTrue(report.contains("john"));
            assertTrue(report.contains("Admin User"));
            assertTrue(report.contains("|"));
        }
    }

    @Nested
    @DisplayName("generateRoleReport")
    class GenerateRoleReport {
        @Test
        void generateRoleReport_empty_EXPECT_noRolesMessage() {
            String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
            assertTrue(report.contains("Role Report"));
            assertTrue(report.contains("No roles"));
        }

        @Test
        void generateRoleReport_withRoles_EXPECT_roleNamesInReport() {
            Role r = new Role("Admin", "Administrator", Set.of(
                    new Permission("READ", "users", "Read users")
            ));
            roleManager.add(r);
            String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
            assertTrue(report.contains("Admin"));
            assertTrue(report.contains("Administrator"));
        }
    }
}