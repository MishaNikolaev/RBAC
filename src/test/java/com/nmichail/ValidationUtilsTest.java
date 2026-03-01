package com.nmichail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils")
class ValidationUtilsTest {

    @Nested
    @DisplayName("isValidUsername")
    class IsValidUsername {
        @Test
        void isValidUsername_validInput_EXPECT_true() {
            assertTrue(ValidationUtils.isValidUsername("abc"));
            assertTrue(ValidationUtils.isValidUsername("user_123"));
            assertTrue(ValidationUtils.isValidUsername("Admin"));
            assertTrue(ValidationUtils.isValidUsername("a1b2c3"));
            assertTrue(ValidationUtils.isValidUsername("user_name_99"));
        }

        @Test
        void isValidUsername_tooShort_EXPECT_false() {
            assertFalse(ValidationUtils.isValidUsername("ab"));
            assertFalse(ValidationUtils.isValidUsername("a"));
        }

        @Test
        void isValidUsername_tooLong_EXPECT_false() {
            assertFalse(ValidationUtils.isValidUsername("abcdefghijklmnopqrstu")); // 21 chars
        }

        @Test
        void isValidUsername_invalidChars_EXPECT_false() {
            assertFalse(ValidationUtils.isValidUsername("user-name"));
            assertFalse(ValidationUtils.isValidUsername("user name"));
            assertFalse(ValidationUtils.isValidUsername("user@mail"));
        }

        @Test
        void isValidUsername_nullOrBlank_EXPECT_false() {
            assertFalse(ValidationUtils.isValidUsername(null));
            assertFalse(ValidationUtils.isValidUsername(""));
            assertFalse(ValidationUtils.isValidUsername("   "));
        }
    }

    @Nested
    @DisplayName("isValidEmail")
    class IsValidEmail {
        @Test
        void isValidEmail_validInput_EXPECT_true() {
            assertTrue(ValidationUtils.isValidEmail("a@b.co"));
            assertTrue(ValidationUtils.isValidEmail("user@company.com"));
            assertTrue(ValidationUtils.isValidEmail("test+tag@domain.org"));
        }

        @Test
        void isValidEmail_noAt_EXPECT_false() {
            assertFalse(ValidationUtils.isValidEmail("userexample.com"));
        }

        @Test
        void isValidEmail_noDotAfterAt_EXPECT_false() {
            assertFalse(ValidationUtils.isValidEmail("user@domain"));
        }

        @Test
        void isValidEmail_nullOrBlank_EXPECT_false() {
            assertFalse(ValidationUtils.isValidEmail(null));
            assertFalse(ValidationUtils.isValidEmail(""));
        }
    }

    @Nested
    @DisplayName("isValidDate")
    class IsValidDate {
        @Test
        void isValidDate_isoFormat_EXPECT_true() {
            assertTrue(ValidationUtils.isValidDate("2026-02-12"));
            assertTrue(ValidationUtils.isValidDate("2025-01-01"));
        }

        @Test
        void isValidDate_invalidFormat_EXPECT_false() {
            assertFalse(ValidationUtils.isValidDate("12-02-2026"));
            assertFalse(ValidationUtils.isValidDate("2026/02/12"));
            assertFalse(ValidationUtils.isValidDate("not-a-date"));
        }

        @Test
        void isValidDate_nullOrBlank_EXPECT_false() {
            assertFalse(ValidationUtils.isValidDate(null));
            assertFalse(ValidationUtils.isValidDate(""));
        }
    }

    @Nested
    @DisplayName("normalizeString")
    class NormalizeString {
        @Test
        void normalizeString_nullInput_EXPECT_null() {
            assertNull(ValidationUtils.normalizeString(null));
        }

        @Test
        void normalizeString_whitespaceInput_EXPECT_trimmedAndCollapsed() {
            assertEquals("a b c", ValidationUtils.normalizeString("  a   b   c  "));
        }
    }

    @Nested
    @DisplayName("requireNonEmpty")
    class RequireNonEmpty {
        @Test
        void requireNonEmpty_nonEmpty_EXPECT_noException() {
            assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("ok", "field"));
        }

        @Test
        void requireNonEmpty_null_EXPECT_throwsIllegalArgument() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.requireNonEmpty(null, "field"));
            assertTrue(e.getMessage().contains("field"));
        }

        @Test
        void requireNonEmpty_blank_EXPECT_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.requireNonEmpty("", "value"));
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.requireNonEmpty("   ", "value"));
        }
    }
}