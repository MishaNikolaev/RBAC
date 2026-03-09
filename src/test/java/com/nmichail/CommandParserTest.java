package com.nmichail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommandParser")
class CommandParserTest {

    private CommandParser parser;
    private RBACSystem system;
    private Scanner scanner;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        scanner = new Scanner(new ByteArrayInputStream(new byte[0]));
    }

    private static void runWithStdout(ByteArrayOutputStream out, Runnable action) {
        PrintStream prev = System.out;
        System.setOut(new PrintStream(out));
        try {
            action.run();
        } finally {
            System.setOut(prev);
        }
    }

    @Nested
    @DisplayName("registerCommand")
    class RegisterCommand {
        @Test
        void registerCommand_valid_EXPECT_registered() {
            boolean[] executed = {false};
            parser.registerCommand("test-cmd", "Test description", (s, sys) -> executed[0] = true);
            parser.executeCommand("test-cmd", scanner, system);
            assertTrue(executed[0]);
        }

        @Test
        void registerCommand_emptyName_EXPECT_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> parser.registerCommand("", "desc", (s, sys) -> {}));
            assertThrows(IllegalArgumentException.class,
                    () -> parser.registerCommand("  ", "desc", (s, sys) -> {}));
        }

        @Test
        void registerCommand_nullCommand_EXPECT_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> parser.registerCommand("x", "desc", null));
        }

        @Test
        void registerCommand_mixedCaseName_EXPECT_executesByLowerCaseKey() {
            boolean[] executed = {false};
            parser.registerCommand("MyCmd", "Desc", (s, sys) -> executed[0] = true);
            parser.executeCommand("mycmd", scanner, system);
            assertTrue(executed[0]);
        }
    }

    @Nested
    @DisplayName("executeCommand")
    class ExecuteCommand {
        @Test
        void executeCommand_unknownName_EXPECT_printsUnknownCommand() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            runWithStdout(out, () -> parser.executeCommand("nonexistent", scanner, system));
            assertTrue(out.toString().contains("Unknown command"));
            assertTrue(out.toString().contains("nonexistent"));
        }

        @Test
        void executeCommand_nullOrBlank_EXPECT_printsNoCommandSpecified() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            runWithStdout(out, () -> parser.executeCommand(null, scanner, system));
            assertTrue(out.toString().contains("No command specified"));
            out.reset();
            runWithStdout(out, () -> parser.executeCommand("   ", scanner, system));
            assertTrue(out.toString().contains("No command specified"));
        }
    }

    @Nested
    @DisplayName("parseAndExecute")
    class ParseAndExecute {
        @Test
        void parseAndExecute_emptyInput_EXPECT_printsEmptyInput() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            runWithStdout(out, () -> parser.parseAndExecute("", scanner, system));
            assertTrue(out.toString().contains("Empty input"));
            out.reset();
            runWithStdout(out, () -> parser.parseAndExecute("   ", scanner, system));
            assertTrue(out.toString().contains("Empty input"));
        }

        @Test
        void parseAndExecute_firstWordIsCommand_EXPECT_executesCommand() {
            boolean[] executed = {false};
            parser.registerCommand("run", "Run", (s, sys) -> executed[0] = true);
            parser.parseAndExecute("run extra args", scanner, system);
            assertTrue(executed[0]);
        }
    }

    @Nested
    @DisplayName("printHelp")
    class PrintHelp {
        @Test
        void printHelp_noCommands_EXPECT_noCommandsRegistered() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            runWithStdout(out, () -> parser.printHelp());
            assertTrue(out.toString().contains("No commands registered"));
        }

        @Test
        void printHelp_withCommands_EXPECT_tableWithCommandAndDescription() {
            parser.registerCommand("help", "Show help", (s, sys) -> {});
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            runWithStdout(out, () -> parser.printHelp());
            String s = out.toString();
            assertTrue(s.contains("Available commands"));
            assertTrue(s.contains("help"));
            assertTrue(s.contains("Show help"));
        }
    }
}
