package com.nmichail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConsoleUtils")
class ConsoleUtilsTest {

    @Nested
    @DisplayName("promptString")
    class PromptString {
        @Test
        void promptString_whitespaceInput_EXPECT_trimmed() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("  hello  \n".getBytes()));
            String result = ConsoleUtils.promptString(scanner, "msg", true);
            assertEquals("hello", result);
        }

        @Test
        void promptString_requiredAndEmptyThenValue_EXPECT_value() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("\n\nvalue\n".getBytes()));
            String result = ConsoleUtils.promptString(scanner, "msg", true);
            assertEquals("value", result);
        }

        @Test
        void promptString_notRequiredAndEmpty_EXPECT_empty() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("\n".getBytes()));
            String result = ConsoleUtils.promptString(scanner, "msg", false);
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("promptInt")
    class PromptInt {
        @Test
        void promptInt_validInRange_EXPECT_number() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("3\n".getBytes()));
            int result = ConsoleUtils.promptInt(scanner, "num", 1, 5);
            assertEquals(3, result);
        }

        @Test
        void promptInt_outOfRangeThenValid_EXPECT_secondValue() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("99\n2\n".getBytes()));
            int result = ConsoleUtils.promptInt(scanner, "num", 1, 10);
            assertEquals(2, result);
        }

        @Test
        void promptInt_invalidThenValid_EXPECT_secondValue() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("abc\n5\n".getBytes()));
            int result = ConsoleUtils.promptInt(scanner, "num", 1, 10);
            assertEquals(5, result);
        }

        @Test
        void promptInt_minGreaterThanMax_EXPECT_throws() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
            assertThrows(IllegalArgumentException.class,
                    () -> ConsoleUtils.promptInt(scanner, "x", 10, 1));
        }
    }

    @Nested
    @DisplayName("promptYesNo")
    class PromptYesNo {
        @Test
        void promptYesNo_yesInput_EXPECT_true() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("y\n".getBytes()));
            assertTrue(ConsoleUtils.promptYesNo(scanner, "?"));
        }

        @Test
        void promptYesNo_noInput_EXPECT_false() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("n\n".getBytes()));
            assertFalse(ConsoleUtils.promptYesNo(scanner, "?"));
        }

        @Test
        void promptYesNo_invalidThenYes_EXPECT_true() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("x\nyes\n".getBytes()));
            assertTrue(ConsoleUtils.promptYesNo(scanner, "?"));
        }
    }

    @Nested
    @DisplayName("promptChoice")
    class PromptChoice {
        @Test
        void promptChoice_validIndex_EXPECT_selectedOption() {
            List<String> options = List.of("A", "B", "C");
            Scanner scanner = new Scanner(new ByteArrayInputStream("2\n".getBytes()));
            String result = ConsoleUtils.promptChoice(scanner, "Choose", options);
            assertEquals("B", result);
        }

        @Test
        void promptChoice_nullOrEmptyOptions_EXPECT_throws() {
            Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
            assertThrows(IllegalArgumentException.class,
                    () -> ConsoleUtils.promptChoice(scanner, "x", null));
            assertThrows(IllegalArgumentException.class,
                    () -> ConsoleUtils.promptChoice(scanner, "x", List.of()));
        }
    }
}