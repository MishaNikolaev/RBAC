package com.nmichail.taxi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ConditionalOnProperty(value = "taxi.notification.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationWorkNotifier {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition work = lock.newCondition();

    public void notifyWorkers() {
        lock.lock();
        try {
            work.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void awaitPollInterval(long maxWaitMs) throws InterruptedException {
        lock.lock();
        try {
            work.await(Math.max(1, maxWaitMs), TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }
}