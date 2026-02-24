package com.nmichail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleManager")
class RoleManagerTest {

    private RoleManager manager;
    private Role roleAdmin;
    private Role roleViewer;
    private Permission permRead;
    private Permission permWrite;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
        Set<Permission> adminPerms = new HashSet<>(Set.of(
                new Permission("READ", "users", "View users"),
                new Permission("WRITE", "users", "Edit users")
        ));
        roleAdmin = new Role("Administrator", "Full access", adminPerms);
        roleViewer = new Role("Viewer", "Read only", new HashSet<>(Set.of(new Permission("READ", "users", "View"))));
        permRead = new Permission("READ", "reports", "View reports");
        permWrite = new Permission("WRITE", "reports", "Edit reports");
    }

    @Nested
    @DisplayName("Repository methods")
    class RepositoryMethods {

        @Test
        void add_addsRole() {
            manager.add(roleAdmin);
            assertEquals(1, manager.count());
            assertTrue(manager.exists("Administrator"));
        }

        @Test
        void remove_removesRole() {
            manager.add(roleAdmin);
            assertTrue(manager.remove(roleAdmin));
            assertEquals(0, manager.count());
            assertFalse(manager.exists("Administrator"));
        }

        @Test
        void remove_returnsFalseWhenRoleNull() {
            manager.add(roleAdmin);
            assertFalse(manager.remove(null));
            assertEquals(1, manager.count());
        }

        @Test
        void findById_returnsRoleById() {
            manager.add(roleAdmin);
            Optional<Role> found = manager.findById(roleAdmin.id);
            assertTrue(found.isPresent());
            assertEquals(roleAdmin, found.get());
            assertEquals(Optional.empty(), manager.findById("unknown"));
        }

        @Test
        void findAll_returnsAllRoles() {
            manager.add(roleAdmin);
            manager.add(roleViewer);
            List<Role> all = manager.findAll();
            assertEquals(2, all.size());
            assertTrue(all.contains(roleAdmin));
            assertTrue(all.contains(roleViewer));
        }

        @Test
        void count_returnsSize() {
            assertEquals(0, manager.count());
            manager.add(roleAdmin);
            assertEquals(1, manager.count());
        }

        @Test
        void clear_removesAll() {
            manager.add(roleAdmin);
            manager.add(roleViewer);
            manager.clear();
            assertEquals(0, manager.count());
            assertFalse(manager.exists("Administrator"));
        }
    }

    @Nested
    @DisplayName("Additional methods")
    class AdditionalMethods {

        @Test
        void findByName_returnsOptional() {
            manager.add(roleAdmin);
            assertEquals(Optional.of(roleAdmin), manager.findByName("Administrator"));
            assertEquals(Optional.empty(), manager.findByName("Nobody"));
        }

        @Test
        void findByFilter_filtersRoles() {
            manager.add(roleAdmin);
            manager.add(roleViewer);
            List<Role> result = manager.findByFilter(RoleFilters.byName("Administrator"));
            assertEquals(1, result.size());
            assertEquals(roleAdmin, result.get(0));
        }

        @Test
        void findAll_withFilterAndSorter() {
            manager.add(roleAdmin);
            manager.add(roleViewer);
            List<Role> result = manager.findAll(RoleFilters.hasAtLeastNPermissions(1), (a, b) -> a.name.compareTo(b.name));
            assertEquals(2, result.size());
        }

        @Test
        void exists_returnsTrueWhenNamePresent() {
            assertFalse(manager.exists("Administrator"));
            manager.add(roleAdmin);
            assertTrue(manager.exists("Administrator"));
        }

        @Test
        void exists_returnsFalseWhenNameNull() {
            assertFalse(manager.exists(null));
        }

        @Test
        void addPermissionToRole_addsPermission() {
            manager.add(roleAdmin);
            int sizeBefore = roleAdmin.getPermissions().size();
            manager.addPermissionToRole("Administrator", permRead);
            assertTrue(roleAdmin.hasPermission("READ", "reports"));
            assertEquals(sizeBefore + 1, roleAdmin.getPermissions().size());
        }

        @Test
        void removePermissionFromRole_removesPermission() {
            manager.add(roleAdmin);
            manager.addPermissionToRole("Administrator", permRead);
            manager.removePermissionFromRole("Administrator", permRead);
            assertFalse(roleAdmin.hasPermission("READ", "reports"));
        }

        @Test
        void findRolesWithPermission_returnsMatchingRoles() {
            manager.add(roleAdmin);
            manager.add(roleViewer);
            List<Role> withRead = manager.findRolesWithPermission("READ", "users");
            assertEquals(2, withRead.size());
            List<Role> withWrite = manager.findRolesWithPermission("WRITE", "users");
            assertEquals(1, withWrite.size());
            assertEquals(roleAdmin, withWrite.get(0));
        }
    }

    @Nested
    @DisplayName("Two maps sync")
    class TwoMapsSync {

        @Test
        void findByIdAndFindByName_returnSameRole() {
            manager.add(roleAdmin);
            assertEquals(manager.findById(roleAdmin.id), manager.findByName("Administrator"));
        }

        @Test
        void remove_removesFromBothMaps() {
            manager.add(roleAdmin);
            manager.remove(roleAdmin);
            assertEquals(Optional.empty(), manager.findById(roleAdmin.id));
            assertEquals(Optional.empty(), manager.findByName("Administrator"));
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        void add_throwsWhenRoleNull() {
            assertThrows(IllegalArgumentException.class, () -> manager.add(null));
        }

        @Test
        void add_throwsWhenNameAlreadyExists() {
            manager.add(roleAdmin);
            Set<Permission> perms = new HashSet<>();
            Role other = new Role("Administrator", "Other", perms);
            assertThrows(IllegalArgumentException.class, () -> manager.add(other));
        }

        @Test
        void remove_throwsWhenRoleAssigned() {
            manager.add(roleAdmin);
            manager.setRoleAssignedChecker(role -> role.id.equals(roleAdmin.id));
            assertThrows(IllegalStateException.class, () -> manager.remove(roleAdmin));
        }

        @Test
        void addPermissionToRole_throwsWhenRoleNotFound() {
            assertThrows(IllegalArgumentException.class, () -> manager.addPermissionToRole("NoRole", permRead));
        }

        @Test
        void addPermissionToRole_throwsWhenPermissionNull() {
            manager.add(roleAdmin);
            assertThrows(IllegalArgumentException.class, () -> manager.addPermissionToRole("Administrator", null));
        }

        @Test
        void removePermissionFromRole_throwsWhenRoleNotFound() {
            assertThrows(IllegalArgumentException.class, () -> manager.removePermissionFromRole("NoRole", permRead));
        }
    }
}
