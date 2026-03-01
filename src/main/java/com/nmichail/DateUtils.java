package com.nmichail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtils() {
    }

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    public static boolean isBefore(String date1, String date2) {
        String d1 = toDatePart(date1);
        String d2 = toDatePart(date2);
        if (d1 == null || d2 == null) return false;
        return d1.compareTo(d2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        String d1 = toDatePart(date1);
        String d2 = toDatePart(date2);
        if (d1 == null || d2 == null) return false;
        return d1.compareTo(d2) > 0;
    }

    public static String addDays(String date, int days) {
        if (date == null || date.isBlank()) return date;
        String d = toDatePart(date);
        if (d == null) return date;
        try {
            LocalDate ld = LocalDate.parse(d, DATE_FORMAT);
            return ld.plusDays(days).format(DATE_FORMAT);
        } catch (Exception e) {
            return date;
        }
    }

    public static String formatRelativeTime(String date) {
        if (date == null || date.isBlank()) return "?";
        String d = toDatePart(date);
        if (d == null) return date;
        try {
            LocalDate target = LocalDate.parse(d, DATE_FORMAT);
            long diff = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), target);
            if (diff == 0) return "today";
            if (diff > 0) return "in " + diff + " day" + (diff == 1 ? "" : "s");
            return Math.abs(diff) + " day" + (diff == -1 ? "" : "s") + " ago";
        } catch (Exception e) {
            return date;
        }
    }

    private static String toDatePart(String date) {
        if (date == null) return null;
        String s = date.trim();
        if (s.length() >= 10) return s.substring(0, 10);
        return s.isEmpty() ? null : s;
    }
}