package com.nmichail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditLog")
class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Nested
    @DisplayName("log and getAll")
    class LogAndGetAll {
        @Test
        void getAll_noEntries_EXPECT_emptyList() {
            assertTrue(auditLog.getAll().isEmpty());
        }

        @Test
        void log_givenActionAndDetails_EXPECT_entryInList() {
            auditLog.log("USER_CREATE", "admin", "alice", "new user");
            List<AuditEntry> all = auditLog.getAll();
            assertEquals(1, all.size());
            assertEquals("USER_CREATE", all.get(0).action());
            assertEquals("admin", all.get(0).performer());
            assertEquals("alice", all.get(0).target());
            assertEquals("new user", all.get(0).details());
            assertNotNull(all.get(0).timestamp());
        }

        @Test
        void log_nullDetails_EXPECT_storedAsEmpty() {
            auditLog.log("ACTION", "p", "t", null);
            assertEquals("", auditLog.getAll().get(0).details());
        }
    }

    @Nested
    @DisplayName("getByPerformer")
    class GetByPerformer {
        @Test
        void getByPerformer_givenPerformer_EXPECT_matchingEntries() {
            auditLog.log("A1", "admin", "x", "");
            auditLog.log("A2", "user", "y", "");
            auditLog.log("A3", "admin", "z", "");
            List<AuditEntry> byAdmin = auditLog.getByPerformer("admin");
            assertEquals(2, byAdmin.size());
            assertTrue(byAdmin.stream().allMatch(e -> "admin".equals(e.performer())));
        }

        @Test
        void getByPerformer_nullOrBlank_EXPECT_emptyList() {
            auditLog.log("A", "p", "t", "");
            assertTrue(auditLog.getByPerformer(null).isEmpty());
            assertTrue(auditLog.getByPerformer("").isEmpty());
        }
    }

    @Nested
    @DisplayName("getByAction")
    class GetByAction {
        @Test
        void getByAction_givenAction_EXPECT_matchingEntries() {
            auditLog.log("USER_CREATE", "admin", "u1", "");
            auditLog.log("ROLE_DELETE", "admin", "r1", "");
            auditLog.log("USER_CREATE", "admin", "u2", "");
            List<AuditEntry> byCreate = auditLog.getByAction("USER_CREATE");
            assertEquals(2, byCreate.size());
        }
    }

    @Nested
    @DisplayName("saveToFile")
    class SaveToFile {
        @Test
        void saveToFile_givenEntries_EXPECT_fileWithContent() throws IOException {
            auditLog.log("ACTION", "perf", "tgt", "det");
            java.nio.file.Path path = Files.createTempFile("audit", ".txt");
            try {
                auditLog.saveToFile(path.toString());
                List<String> lines = Files.readAllLines(path);
                assertEquals(1, lines.size());
                assertTrue(lines.get(0).contains("ACTION") && lines.get(0).contains("perf"));
            } finally {
                Files.deleteIfExists(path);
            }
        }
    }
}