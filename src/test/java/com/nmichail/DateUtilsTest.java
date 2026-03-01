package com.nmichail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateUtils")
class DateUtilsTest {

    @Nested
    @DisplayName("getCurrentDate")
    class GetCurrentDate {
        @Test
        void getCurrentDate_call_EXPECT_isoFormat() {
            String d = DateUtils.getCurrentDate();
            assertTrue(d.matches("\\d{4}-\\d{2}-\\d{2}"));
        }
    }

    @Nested
    @DisplayName("getCurrentDateTime")
    class GetCurrentDateTime {
        @Test
        void getCurrentDateTime_call_EXPECT_isoLikeFormat() {
            String d = DateUtils.getCurrentDateTime();
            assertTrue(d.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        }
    }

    @Nested
    @DisplayName("isBefore / isAfter")
    class Compare {
        @Test
        void isBefore_date1BeforeDate2_EXPECT_true() {
            assertTrue(DateUtils.isBefore("2025-01-01", "2025-12-31"));
            assertFalse(DateUtils.isBefore("2025-12-31", "2025-01-01"));
            assertFalse(DateUtils.isBefore("2025-01-01", "2025-01-01"));
        }

        @Test
        void isAfter_date1AfterDate2_EXPECT_true() {
            assertTrue(DateUtils.isAfter("2025-12-31", "2025-01-01"));
            assertFalse(DateUtils.isAfter("2025-01-01", "2025-12-31"));
        }

        @Test
        void isBefore_isAfter_nullArgument_EXPECT_false() {
            assertFalse(DateUtils.isBefore(null, "2025-01-01"));
            assertFalse(DateUtils.isBefore("2025-01-01", null));
            assertFalse(DateUtils.isAfter(null, "2025-01-01"));
        }
    }

    @Nested
    @DisplayName("addDays")
    class AddDays {
        @Test
        void addDays_positiveDays_EXPECT_dateShifted() {
            assertEquals("2025-01-11", DateUtils.addDays("2025-01-01", 10));
        }

        @Test
        void addDays_negativeDays_EXPECT_dateShiftedBack() {
            assertEquals("2024-12-22", DateUtils.addDays("2025-01-01", -10));
        }

        @Test
        void addDays_zeroDays_EXPECT_sameDate() {
            assertEquals("2025-01-01", DateUtils.addDays("2025-01-01", 0));
        }

        @Test
        void addDays_nullDate_EXPECT_null() {
            assertNull(DateUtils.addDays(null, 5));
        }
    }

    @Nested
    @DisplayName("formatRelativeTime")
    class FormatRelativeTime {
        @Test
        void formatRelativeTime_today_EXPECT_today() {
            String today = DateUtils.getCurrentDate();
            assertEquals("today", DateUtils.formatRelativeTime(today));
        }

        @Test
        void formatRelativeTime_futureDate_EXPECT_inDays() {
            String future = DateUtils.addDays(DateUtils.getCurrentDate(), 5);
            assertTrue(DateUtils.formatRelativeTime(future).startsWith("in "));
            assertTrue(DateUtils.formatRelativeTime(future).contains("day"));
        }

        @Test
        void formatRelativeTime_pastDate_EXPECT_daysAgo() {
            String past = DateUtils.addDays(DateUtils.getCurrentDate(), -2);
            assertTrue(DateUtils.formatRelativeTime(past).endsWith("ago"));
        }

        @Test
        void formatRelativeTime_nullOrBlank_EXPECT_placeholder() {
            assertEquals("?", DateUtils.formatRelativeTime(null));
            assertEquals("?", DateUtils.formatRelativeTime(""));
        }
    }
}