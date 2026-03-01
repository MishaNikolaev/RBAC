package com.nmichail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FormatUtils {

    private static final int BOX_PADDING = 2;
    private static final int MAX_CELL_WIDTH = 80;

    private FormatUtils() {
    }

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }
        int cols = headers.length;
        int[] widths = new int[cols];
        for (int c = 0; c < cols; c++) {
            int w = headers[c] != null ? headers[c].length() : 0;
            for (String[] row : rows != null ? rows : Collections.<String[]>emptyList()) {
                if (row != null && c < row.length && row[c] != null) {
                    w = Math.max(w, Math.min(MAX_CELL_WIDTH, row[c].length()));
                }
            }
            widths[c] = Math.max(1, Math.min(MAX_CELL_WIDTH, w));
        }

        StringBuilder sb = new StringBuilder();
        String sep = makeSeparator(widths, "+", "-");
        sb.append(sep).append("\n");
        sb.append(makeRow(widths, headers)).append("\n");
        sb.append(sep).append("\n");
        if (rows != null) {
            for (String[] row : rows) {
                String[] cells = new String[cols];
                for (int c = 0; c < cols; c++) {
                    cells[c] = (row != null && c < row.length && row[c] != null) ? row[c] : "";
                }
                sb.append(makeRow(widths, cells)).append("\n");
            }
        }
        sb.append(sep);
        return sb.toString();
    }

    private static String makeSeparator(int[] widths, String corner, String fill) {
        StringBuilder sb = new StringBuilder();
        sb.append(corner);
        for (int w : widths) {
            sb.append(fill.repeat(w + 2)).append(corner);
        }
        return sb.toString();
    }

    private static String makeRow(int[] widths, String[] cells) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int c = 0; c < widths.length; c++) {
            String cell = c < cells.length && cells[c] != null ? cells[c] : "";
            sb.append(" ").append(padRight(truncate(cell, widths[c]), widths[c])).append(" |");
        }
        return sb.toString();
    }

    public static String formatBox(String text) {
        if (text == null) {
            text = "";
        }
        String[] lines = text.split("\n", -1);
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        width = Math.max(width, 1);
        String border = "+" + "-".repeat(width + BOX_PADDING * 2) + "+";
        StringBuilder sb = new StringBuilder();
        sb.append(border).append("\n");
        for (String line : lines) {
            sb.append("|").append(" ".repeat(BOX_PADDING))
                    .append(padRight(line, width))
                    .append(" ".repeat(BOX_PADDING)).append("|\n");
        }
        sb.append(border);
        return sb.toString();
    }

    public static String formatHeader(String text) {
        if (text == null) {
            text = "";
        }
        String dash = "─".repeat(2);
        return String.format("%s %s %s", dash, text, dash);
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (maxLength <= 0) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return text.substring(0, maxLength);
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (length <= 0) {
            return text;
        }
        String s = truncate(text, length);
        return String.format("%-" + length + "s", s);
    }

    public static String padLeft(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (length <= 0) {
            return text;
        }
        String s = truncate(text, length);
        return String.format("%" + length + "s", s);
    }
}