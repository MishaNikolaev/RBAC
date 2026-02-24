package com.nmichail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserManager")
class UserManagerTest {

    private UserManager manager;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
        user1 = User.validate("alice_01", "Alice Smith", "alice@example.com");
        user2 = User.validate("bob_user", "Bob Jones", "bob@test.org");
    }

    @Nested
    @DisplayName("Repository methods")
    class RepositoryMethods {

        @Test
        void add_addsUser() {
            manager.add(user1);
            assertEquals(1, manager.count());
            assertTrue(manager.exists("alice_01"));
        }

        @Test
        void remove_removesUser() {
            manager.add(user1);
            assertTrue(manager.remove(user1));
            assertEquals(0, manager.count());
            assertFalse(manager.exists("alice_01"));
        }

        @Test
        void remove_returnsFalseWhenUserNotPresent() {
            manager.add(user1);
            assertFalse(manager.remove(user2));
            assertEquals(1, manager.count());
        }

        @Test
        void findById_returnsUserByUsername() {
            manager.add(user1);
            assertEquals(Optional.of(user1), manager.findById("alice_01"));
            assertEquals(Optional.empty(), manager.findById("unknown"));
        }

        @Test
        void findAll_returnsAllUsers() {
            manager.add(user1);
            manager.add(user2);
            List<User> all = manager.findAll();
            assertEquals(2, all.size());
            assertTrue(all.contains(user1));
            assertTrue(all.contains(user2));
        }

        @Test
        void count_returnsSize() {
            assertEquals(0, manager.count());
            manager.add(user1);
            assertEquals(1, manager.count());
            manager.add(user2);
            assertEquals(2, manager.count());
        }

        @Test
        void clear_removesAll() {
            manager.add(user1);
            manager.add(user2);
            manager.clear();
            assertEquals(0, manager.count());
            assertFalse(manager.exists("alice_01"));
            assertFalse(manager.exists("bob_user"));
        }
    }

    @Nested
    @DisplayName("Additional methods")
    class AdditionalMethods {

        @Test
        void findByUsername_returnsOptional() {
            manager.add(user1);
            assertEquals(Optional.of(user1), manager.findByUsername("alice_01"));
            assertEquals(Optional.empty(), manager.findByUsername("nobody"));
        }

        @Test
        void findByEmail_returnsFirstMatchIgnoreCase() {
            manager.add(user1);
            assertEquals(Optional.of(user1), manager.findByEmail("alice@example.com"));
            assertEquals(Optional.of(user1), manager.findByEmail("ALICE@EXAMPLE.COM"));
            assertEquals(Optional.empty(), manager.findByEmail("other@example.com"));
        }

        @Test
        void findByFilter_filtersByPredicate() {
            manager.add(user1);
            manager.add(user2);
            List<User> result = manager.findByFilter(UserFilters.byUsername("alice_01"));
            assertEquals(1, result.size());
            assertEquals(user1, result.get(0));
        }

        @Test
        void findAll_withFilterAndSorter() {
            manager.add(user1);
            manager.add(user2);
            List<User> result = manager.findAll(UserFilters.byEmailDomain("example.com"), Comparator.comparing(User::username));
            assertEquals(1, result.size());
            assertEquals(user1, result.get(0));
        }

        @Test
        void exists_returnsTrueWhenUserPresent() {
            assertFalse(manager.exists("alice_01"));
            manager.add(user1);
            assertTrue(manager.exists("alice_01"));
        }

        @Test
        void update_changesFullNameAndEmail() {
            manager.add(user1);
            manager.update("alice_01", "Alice Brown", "alice.brown@example.com");
            Optional<User> updated = manager.findByUsername("alice_01");
            assertTrue(updated.isPresent());
            assertEquals("Alice Brown", updated.get().fullName());
            assertEquals("alice.brown@example.com", updated.get().email());
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        void add_throwsWhenUserNull() {
            assertThrows(IllegalArgumentException.class, () -> manager.add(null));
        }

        @Test
        void add_throwsWhenUserAlreadyExists() {
            manager.add(user1);
            assertThrows(IllegalArgumentException.class, () -> manager.add(user1));
            assertThrows(IllegalArgumentException.class, () -> manager.add(User.validate("alice_01", "Other", "other@x.com")));
        }

        @Test
        void add_throwsWhenValidationFails() {
            assertThrows(IllegalArgumentException.class, () -> manager.add(User.validate("ab", "Name", "a@b.co")));
            assertThrows(IllegalArgumentException.class, () -> manager.add(User.validate("user", "Name", "invalid-email")));
        }

        @Test
        void update_throwsWhenUserDoesNotExist() {
            assertThrows(IllegalArgumentException.class, () -> manager.update("nobody", "Name", "a@b.co"));
        }

        @Test
        void update_throwsWhenNewDataInvalid() {
            manager.add(user1);
            assertThrows(IllegalArgumentException.class, () -> manager.update("alice_01", "", "a@b.co"));
            assertThrows(IllegalArgumentException.class, () -> manager.update("alice_01", "Alice", "bad-email"));
        }
    }
}
