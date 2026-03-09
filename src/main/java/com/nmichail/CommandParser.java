package com.nmichail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, String> commandDescriptions = new LinkedHashMap<>();

    public void registerCommand(String name, String description, Command command) {
        ValidationUtils.requireNonEmpty(name, "name");
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }
        String key = name.trim().toLowerCase();
        commands.put(key, command);
        String desc;
        if (description == null) {
            desc = "";
        } else {
            desc = description.trim();
        }
        commandDescriptions.put(key, desc);    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        if (commandName == null || commandName.isBlank()) {
            System.out.println("No command specified. Type 'help' to see available commands.");
            return;
        }
        String key = commandName.trim().toLowerCase();
        Command command = commands.get(key);
        if (command == null) {
            System.out.println("Unknown command: " + commandName);
            System.out.println("Type 'help' to see available commands.");
            return;
        }
        command.execute(scanner, system);
    }

    public void printHelp() {
        if (commands.isEmpty()) {
            System.out.println("No commands registered.");
            return;
        }
        String[] headers = {"Command", "Description"};
        List<String[]> rows = commandDescriptions.entrySet().stream()
                .map(e -> new String[]{e.getKey(), e.getValue()})
                .toList();
        String table = FormatUtils.formatTable(headers, rows);
        System.out.println(FormatUtils.formatHeader("Available commands"));
        System.out.println();
        System.out.println(table);
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null) {
            System.out.println("Empty input.");
            return;
        }
        if (input.trim().isEmpty()) {
            System.out.println("Empty input.");
            return;
        }
        String[] parts = input.trim().split("\\s+");
        String name = parts[0];
        executeCommand(name, scanner, system);
    }
}