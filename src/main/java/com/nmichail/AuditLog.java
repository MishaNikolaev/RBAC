package com.nmichail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AuditLog {

    private final List<AuditEntry> entries = new CopyOnWriteArrayList<>();

    public void log(String action, String performer, String target, String details) {
        String timestamp = Instant.now().toString();
        entries.add(new AuditEntry(timestamp, action, performer, target, details != null ? details : ""));
    }

    public List<AuditEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<AuditEntry> getByPerformer(String performer) {
        if (performer == null || performer.isBlank()) {
            return List.of();
        }
        return entries.stream()
                .filter(e -> performer.equals(e.performer()))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        if (action == null || action.isBlank()) {
            return List.of();
        }
        return entries.stream()
                .filter(e -> action.equals(e.action()))
                .collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Audit log\n(nothing)\n");
            return;
        }
        System.out.println("Audit log");
        String[] headers = {"Timestamp", "Action", "Performer", "Target", "Details"};
        List<String[]> rows = new ArrayList<>();
        for (AuditEntry e : entries) {
            rows.add(new String[]{
                    FormatUtils.truncate(e.timestamp(), 24),
                    e.action(),
                    e.performer(),
                    e.target(),
                    FormatUtils.truncate(e.details(), 30)
            });
        }
        System.out.println(FormatUtils.formatTable(headers, rows));
    }

    public void saveToFile(String filename) throws IOException {
        Path path = Path.of(filename);
        List<String> lines = entries.stream()
                .map(e -> e.timestamp() + "|" + e.action() + "|" + e.performer() + "|" + e.target() + "|" + e.details())
                .toList();
        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}