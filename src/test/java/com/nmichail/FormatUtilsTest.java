package com.nmichail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FormatUtils")
class FormatUtilsTest {

    @Nested
    @DisplayName("formatTable")
    class FormatTable {
        @Test
        void formatTable_emptyHeaders_EXPECT_emptyString() {
            assertEquals("", FormatUtils.formatTable(new String[]{}, List.of()));
        }

        @Test
        void formatTable_nullHeaders_EXPECT_emptyString() {
            assertEquals("", FormatUtils.formatTable(null, List.of()));
        }

        @Test
        void formatTable_headersAndRows_EXPECT_asciiTableRendered() {
            String[] headers = {"A", "B"};
            List<String[]> rows = List.of(
                    new String[]{"1", "2"},
                    new String[]{"x", "y"}
            );
            String out = FormatUtils.formatTable(headers, rows);
            assertTrue(out.contains("| A "));
            assertTrue(out.contains("| B "));
            assertTrue(out.contains("| 1 "));
            assertTrue(out.contains("| 2 "));
            assertTrue(out.contains("+"));
            assertTrue(out.contains("-"));
        }
    }

    @Nested
    @DisplayName("formatBox")
    class FormatBox {
        @Test
        void formatBox_givenText_EXPECT_wrappedInBorder() {
            String out = FormatUtils.formatBox("hello");
            assertTrue(out.startsWith("+"));
            assertTrue(out.contains("hello"));
            assertTrue(out.contains("|"));
        }

        @Test
        void formatBox_null_EXPECT_emptyBox() {
            String out = FormatUtils.formatBox(null);
            assertTrue(out.contains("+"));
        }

        @Test
        void formatBox_multiline_EXPECT_allLinesInBox() {
            String out = FormatUtils.formatBox("a\nb");
            assertTrue(out.contains("a"));
            assertTrue(out.contains("b"));
        }
    }

    @Nested
    @DisplayName("formatHeader")
    class FormatHeader {
        @Test
        void formatHeader_givenText_EXPECT_dashesAndText() {
            String out = FormatUtils.formatHeader("Title");
            assertTrue(out.contains("Title"));
            assertTrue(out.contains("─"));
        }

        @Test
        void formatHeader_null_EXPECT_nonNullString() {
            assertNotNull(FormatUtils.formatHeader(null));
        }
    }

    @Nested
    @DisplayName("truncate")
    class Truncate {
        @Test
        void truncate_shortString_EXPECT_unchanged() {
            assertEquals("ab", FormatUtils.truncate("ab", 10));
        }

        @Test
        void truncate_longString_EXPECT_truncatedWithEllipsis() {
            assertEquals("abc...", FormatUtils.truncate("abcdefgh", 6));
        }

        @Test
        void truncate_null_EXPECT_emptyString() {
            assertEquals("", FormatUtils.truncate(null, 5));
        }

        @Test
        void truncate_zeroMaxLength_EXPECT_emptyString() {
            assertEquals("", FormatUtils.truncate("hello", 0));
        }
    }

    @Nested
    @DisplayName("padRight")
    class PadRight {
        @Test
        void padRight_shortString_EXPECT_paddedToLength() {
            assertEquals("ab   ", FormatUtils.padRight("ab", 5));
        }

        @Test
        void padRight_longString_EXPECT_truncatedToLength() {
            String s = FormatUtils.padRight("abcdef", 5);
            assertEquals(5, s.length());
        }
    }

    @Nested
    @DisplayName("padLeft")
    class PadLeft {
        @Test
        void padLeft_shortString_EXPECT_paddedLeft() {
            assertEquals("  12", FormatUtils.padLeft("12", 4));
        }
    }
}