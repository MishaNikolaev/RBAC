package com.nmichail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RBACSystem")
class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Nested
    @DisplayName("getters and currentUser")
    class GettersAndCurrentUser {
        @Test
        void getters_call_EXPECT_nonNullManagers() {
            assertNotNull(system.getUserManager());
            assertNotNull(system.getRoleManager());
            assertNotNull(system.getAssignmentManager());
            assertNotNull(system.getAuditLog());
        }

        @Test
        void setCurrentUser_getCurrentUser_EXPECT_setValue() {
            system.setCurrentUser("admin");
            assertEquals("admin", system.getCurrentUser());
        }

        @Test
        void setCurrentUser_nullOrBlank_EXPECT_system() {
            system.setCurrentUser("admin");
            system.setCurrentUser(null);
            assertEquals("system", system.getCurrentUser());
            system.setCurrentUser("  ");
            assertEquals("system", system.getCurrentUser());
        }
    }

    @Nested
    @DisplayName("initialize")
    class Initialize {
        @Test
        void initialize_EXPECT_oneUserThreeRolesOneAssignment() {
            system.initialize();
            assertEquals(1, system.getUserManager().count());
            assertEquals(3, system.getRoleManager().count());
            assertEquals(1, system.getAssignmentManager().count());
        }

        @Test
        void initialize_EXPECT_adminUserAndAdminRole() {
            system.initialize();
            assertTrue(system.getUserManager().exists("admin"));
            assertTrue(system.getRoleManager().exists("Admin"));
        }
    }

    @Nested
    @DisplayName("generateStatistics")
    class GenerateStatistics {
        @Test
        void generateStatistics_EXPECT_containsMetricLabelsAndValues() {
            system.initialize();
            String stats = system.generateStatistics();
            assertTrue(stats.contains("RBAC statistics"));
            assertTrue(stats.contains("Users"));
            assertTrue(stats.contains("Roles"));
            assertTrue(stats.contains("Assignments"));
            assertTrue(stats.contains("1"));
            assertTrue(stats.contains("3"));
        }
    }

    @Nested
    @DisplayName("saveToFile and loadFromFile")
    class SaveAndLoad {
        @Test
        void saveToFile_loadFromFile_roundtrip_EXPECT_dataRestored(@TempDir Path tempDir) throws IOException {
            system.initialize();
            Path path = tempDir.resolve("rbac.txt");
            system.saveToFile(path.toString());
            RBACSystem loaded = new RBACSystem();
            loaded.loadFromFile(path.toString());
            assertEquals(system.getUserManager().count(), loaded.getUserManager().count());
            assertEquals(system.getRoleManager().count(), loaded.getRoleManager().count());
            assertEquals(system.getAssignmentManager().count(), loaded.getAssignmentManager().count());
            assertTrue(loaded.getUserManager().exists("admin"));
            assertTrue(loaded.getRoleManager().exists("Admin"));
        }

        @Test
        void loadFromFile_missingFile_EXPECT_throws() {
            assertThrows(IOException.class, () -> system.loadFromFile("/nonexistent/path/rbac.txt"));
        }
    }
}
