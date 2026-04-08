package com.nmichail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AuditLog {

    private final List<AuditEntry> entries = new CopyOnWriteArrayList<>();
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread worker;

    public AuditLog() {
        this.worker = new Thread(this::runWorker, "audit-log-worker");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = Instant.now().toString();
        AuditEntry e = new AuditEntry(timestamp, action, performer, target, details != null ? details : "");
        if (!running.get()) {
            entries.add(e);
            return;
        }
        queue.offer(e);
    }

    public List<AuditEntry> getAll() {
        flush(500);
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<AuditEntry> getByPerformer(String performer) {
        flush(500);
        if (performer == null || performer.isBlank()) {
            return List.of();
        }
        return entries.stream()
                .filter(e -> performer.equals(e.performer()))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        flush(500);
        if (action == null || action.isBlank()) {
            return List.of();
        }
        return entries.stream()
                .filter(e -> action.equals(e.action()))
                .collect(Collectors.toList());
    }

    public void printLog() {
        flush(1500);
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
        flush(2000);
        Path path = Path.of(filename);
        List<String> lines = entries.stream()
                .map(e -> e.timestamp() + "|" + e.action() + "|" + e.performer() + "|" + e.target() + "|" + e.details())
                .toList();
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public void flush(long timeoutMs) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Math.max(0, timeoutMs));
        while (!queue.isEmpty() && System.nanoTime() < deadline) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        worker.interrupt();
        AuditEntry e;
        while ((e = queue.poll()) != null) {
            entries.add(e);
        }
    }

    private void runWorker() {
        try {
            while (running.get() || !queue.isEmpty()) {
                AuditEntry e = queue.poll(250, TimeUnit.MILLISECONDS);
                if (e != null) {
                    entries.add(e);
                }
            }
        } catch (InterruptedException ignored) {
        } finally {
            AuditEntry e;
            while ((e = queue.poll()) != null) {
                entries.add(e);
            }
        }
    }
}