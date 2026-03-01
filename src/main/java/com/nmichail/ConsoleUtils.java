package com.nmichail;

import java.util.List;
import java.util.Scanner;

public final class ConsoleUtils {

    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";
    public static final String CYAN = "\033[36m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String RED = "\033[31m";

    private ConsoleUtils() {
    }

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            printPrompt(message);
            String line = scanner.nextLine();
            if (line == null) {
                line = "";
            }
            String trimmed = line.trim();
            if (required && trimmed.isEmpty()) {
                println(DIM + "  (input value)" + RESET);
                continue;
            }
            return trimmed;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        while (true) {
            printPrompt(message + " [" + min + ".." + max + "]");
            String line = scanner.nextLine();
            if (line == null) {
                line = "";
            }
            line = line.trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    println(RED + "  Expected number from " + min + " to " + max + RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                println(RED + "  Input number" + RESET);
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            printPrompt(message + " (y/n)");
            String line = scanner.nextLine();
            if (line == null) {
                line = "";
            }
            String lower = line.trim().toLowerCase();
            if (lower.equals("y") || lower.equals("yes") || lower.equals("д") || lower.equals("да")) {
                return true;
            }
            if (lower.equals("n") || lower.equals("no") || lower.equals("н") || lower.equals("нет")) {
                return false;
            }
            println(YELLOW + "  Enter y (yes) or n (no)" + RESET);
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("options cannot be null or empty");
        }
        println("");
        println(CYAN + "  " + message + RESET);
        for (int i = 0; i < options.size(); i++) {
            println("    " + (i + 1) + ". " + options.get(i));
        }
        int index = promptInt(scanner, "Choose number", 1, options.size());
        return options.get(index - 1);
    }

    public static void printPrompt(String message) {
        System.out.print("  " + CYAN + message + RESET + " > ");
    }

    private static void println(String s) {
        System.out.println(s);
    }

    public static void printBox(String title, String content) {
        int width = 72;
        String border = "╔" + "═".repeat(width - 2) + "╗";
        System.out.println(border);
        if (title != null && !title.isEmpty()) {
            String t = title.length() > width - 4 ? title.substring(0, width - 5) + "…" : title;
            System.out.println("║  " + BOLD + t + RESET + " ".repeat(Math.max(0, width - 4 - t.length())) + "  ║");
            System.out.println("╠" + "═".repeat(width - 2) + "╣");
        }
        if (content != null && !content.isEmpty()) {
            for (String line : content.split("\n")) {
                String pad = line.length() < width - 4 ? " ".repeat(width - 4 - line.length()) : "";
                System.out.println("║  " + line + pad + "  ║");
            }
        }
        System.out.println("╚" + "═".repeat(width - 2) + "╝");
        System.out.flush();
    }

    public static void printSection(String title) {
        System.out.println("");
        System.out.println("  " + BOLD + "── " + title + " ──" + RESET);
        System.out.println("");
    }
}