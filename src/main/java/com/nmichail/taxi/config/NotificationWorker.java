package com.nmichail.taxi.config;

import com.nmichail.taxi.model.NotificationTask;
import com.nmichail.taxi.service.NotificationService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "taxi.notification.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationWorker {

    private final NotificationService notificationService;
    private final NotificationWorkNotifier workNotifier;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile ExecutorService pool;

    @Value("${taxi.notification.workers:4}")
    private int workers;

    @Value("${taxi.notification.sleep-ms:50}")
    private long sleepMs;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        int n = Math.max(1, workers);
        pool = Executors.newFixedThreadPool(n);
        for (int i = 0; i < n; i++) {
            pool.submit(this::runLoop);
        }
    }

    private void runLoop() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            notificationService.tryLockNextPendingTask()
                    .ifPresentOrElse(this::processOne, this::sleepQuietly);
        }
    }

    private void processOne(NotificationTask task) {
        try {
            notificationService.markSent(task.id);
        } catch (Exception e) {
            notificationService.markFailed(task.id, e.getClass().getSimpleName());
        }
    }

    private void sleepQuietly() {
        try {
            workNotifier.awaitPollInterval(Math.max(50, sleepMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        ExecutorService p = pool;
        if (p == null) {
            return;
        }
        p.shutdown();
        try {
            if (!p.awaitTermination(Duration.ofSeconds(3).toMillis(), TimeUnit.MILLISECONDS)) {
                p.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            p.shutdownNow();
        }
    }
}