package com.nmichail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AssignmentManager")
class AssignmentManagerTest {

    private AssignmentManager manager;
    private User user1;
    private User user2;
    private Role roleAdmin;
    private Role roleViewer;
    private AssignmentMetadata meta;
    private PermanentAssignment permAssignment;
    private TemporaryAssignment tempAssignment;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();

        user1 = User.validate("alice_01", "Alice", "alice@example.com");
        user2 = User.validate("bob_user", "Bob", "bob@example.com");

        roleAdmin = new Role("Admin", "Admin role", new java.util.HashSet<>(Set.of(
                new Permission("READ", "users", "View"),
                new Permission("WRITE", "users", "Edit")
        )));
        roleViewer = new Role("Viewer", "View only", new java.util.HashSet<>(Set.of(new Permission("READ", "users", "View"))));

        meta = new AssignmentMetadata("admin", "2026-01-01T10:00:00Z", "Setup");
        permAssignment = new PermanentAssignment(user1, roleAdmin, meta);
        tempAssignment = new TemporaryAssignment(user2, roleViewer, meta, "2027-12-31", false);
    }

    @Nested
    @DisplayName("Repository methods")
    class RepositoryMethods {

        @Test
        void add_addsAssignment() {
            manager.add(permAssignment);
            assertEquals(1, manager.count());
            assertEquals(Optional.of(permAssignment), manager.findById(permAssignment.assignmentId()));
        }

        @Test
        void remove_removesAssignment() {
            manager.add(permAssignment);
            assertTrue(manager.remove(permAssignment));
            assertEquals(0, manager.count());
            assertTrue(manager.findById(permAssignment.assignmentId()).isEmpty());
        }

        @Test
        void remove_returnsFalseWhenNotPresent() {
            manager.add(permAssignment);
            TemporaryAssignment other = new TemporaryAssignment(user2, roleViewer, meta, "2028-01-01", false);
            assertFalse(manager.remove(other));
            assertEquals(1, manager.count());
        }

        @Test
        void findById_returnsAssignment() {
            manager.add(permAssignment);
            assertEquals(Optional.of(permAssignment), manager.findById(permAssignment.assignmentId()));
            assertTrue(manager.findById("unknown").isEmpty());
        }

        @Test
        void findAll_returnsAll() {
            manager.add(permAssignment);
            manager.add(tempAssignment);
            List<RoleAssignment> all = manager.findAll();
            assertEquals(2, all.size());
        }

        @Test
        void count_returnsSize() {
            assertEquals(0, manager.count());
            manager.add(permAssignment);
            assertEquals(1, manager.count());
        }

        @Test
        void clear_removesAll() {
            manager.add(permAssignment);
            manager.add(tempAssignment);
            manager.clear();
            assertEquals(0, manager.count());
        }
    }

    @Nested
    @DisplayName("Additional methods")
    class AdditionalMethods {

        @Test
        void findByUser_returnsAssignmentsForUser() {
            manager.add(permAssignment);
            manager.add(tempAssignment);
            List<RoleAssignment> alice = manager.findByUser(user1);
            assertEquals(1, alice.size());
            assertEquals(permAssignment, alice.get(0));
        }

        @Test
        void findByRole_returnsAssignmentsForRole() {
            manager.add(permAssignment);
            PermanentAssignment perm2 = new PermanentAssignment(user2, roleAdmin, meta);
            manager.add(perm2);
            List<RoleAssignment> forAdmin = manager.findByRole(roleAdmin);
            assertEquals(2, forAdmin.size());
        }

        @Test
        void findByFilter_filters() {
            manager.add(permAssignment);
            manager.add(tempAssignment);
            List<RoleAssignment> active = manager.findByFilter(AssignmentFilters.activeOnly());
            assertEquals(2, active.size());
        }

        @Test
        void findAll_withFilterAndSorter() {
            manager.add(permAssignment);
            manager.add(tempAssignment);
            List<RoleAssignment> result = manager.findAll(
                    AssignmentFilters.byUser(user1),
                    AssignmentSorters.byUsername()
            );
            assertEquals(1, result.size());
            assertEquals(permAssignment.assignmentId(), result.get(0).assignmentId());
        }

        @Test
        void getActiveAssignments_returnsOnlyActive() {
            manager.add(permAssignment);
            manager.add(tempAssignment);
            List<RoleAssignment> active = manager.getActiveAssignments();
            assertEquals(2, active.size());
            manager.revokeAssignment(permAssignment.assignmentId());
            active = manager.getActiveAssignments();
            assertEquals(1, active.size());
        }

        @Test
        void getExpiredAssignments_returnsInactive() {
            manager.add(permAssignment);
            manager.revokeAssignment(permAssignment.assignmentId());
            List<RoleAssignment> expired = manager.getExpiredAssignments();
            assertEquals(1, expired.size());
        }

        @Test
        void userHasRole_returnsTrueWhenActiveAssignmentExists() {
            manager.add(permAssignment);
            assertTrue(manager.userHasRole(user1, roleAdmin));
            assertFalse(manager.userHasRole(user1, roleViewer));
        }

        @Test
        void userHasRole_returnsFalseWhenAssignmentRevoked() {
            manager.add(permAssignment);
            manager.revokeAssignment(permAssignment.assignmentId());
            assertFalse(manager.userHasRole(user1, roleAdmin));
        }

        @Test
        void userHasPermission_returnsTrueWhenRoleHasPermission() {
            manager.add(permAssignment);
            assertTrue(manager.userHasPermission(user1, "READ", "users"));
            assertTrue(manager.userHasPermission(user1, "WRITE", "users"));
            assertFalse(manager.userHasPermission(user1, "DELETE", "users"));
        }

        @Test
        void getUserPermissions_aggregatesFromAllActiveRoles() {
            manager.add(permAssignment);
            PermanentAssignment perm2 = new PermanentAssignment(user1, roleViewer, meta);
            manager.add(perm2);
            Set<Permission> perms = manager.getUserPermissions(user1);
            assertTrue(perms.size() >= 2);
            assertTrue(perms.stream().anyMatch(p -> p.name().equals("READ") && p.resource().equals("users")));
        }

        @Test
        void revokeAssignment_permanentMarksRevoked() {
            manager.add(permAssignment);
            manager.revokeAssignment(permAssignment.assignmentId());
            assertFalse(permAssignment.isActive());
            assertEquals(1, manager.count());
        }

        @Test
        void revokeAssignment_temporaryRemovesFromMap() {
            manager.add(tempAssignment);
            String id = tempAssignment.assignmentId();
            manager.revokeAssignment(id);
            assertTrue(manager.findById(id).isEmpty());
            assertEquals(0, manager.count());
        }

        @Test
        void extendTemporaryAssignment_extendsExpiration() {
            manager.add(tempAssignment);
            manager.extendTemporaryAssignment(tempAssignment.assignmentId(), "2028-06-30");
            assertEquals("2028-06-30", ((TemporaryAssignment) manager.findById(tempAssignment.assignmentId()).orElseThrow()).getExpiresAt());
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        void add_throwsWhenDuplicateId() {
            manager.add(permAssignment);
            assertThrows(IllegalArgumentException.class, () -> manager.add(permAssignment));
        }

        @Test
        void extendTemporaryAssignment_throwsWhenIdNotFound() {
            assertThrows(IllegalArgumentException.class, () -> manager.extendTemporaryAssignment("no-such-id", "2028-01-01"));
        }

        @Test
        void extendTemporaryAssignment_throwsWhenNotTemporary() {
            manager.add(permAssignment);
            assertThrows(IllegalArgumentException.class, () ->
                    manager.extendTemporaryAssignment(permAssignment.assignmentId(), "2028-01-01"));
        }
    }
}
