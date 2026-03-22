package com.nmichail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

public final class MultiThreadProgressApp {

    private static final int THREAD_COUNT = 4;
    private static final int CALCULATION_STEPS = 40;
    private static final int MS_BETWEEN_STEPS = 90;
    private static final int BAR_WIDTH = 36;
    private static final int DISPLAY_REFRESH_MS = 80;

    private static final String ESC = "\u001B";

    public static void main(String[] args) {
        int threads = THREAD_COUNT;
        int steps = CALCULATION_STEPS;
        int msPerStep = MS_BETWEEN_STEPS;

        if (args.length >= 1) {
            threads = Math.max(1, Integer.parseInt(args[0]));
        }
        if (args.length >= 2) {
            steps = Math.max(1, Integer.parseInt(args[1]));
        }
        if (args.length >= 3) {
            msPerStep = Math.max(1, Integer.parseInt(args[2]));
        }

        run(threads, steps, msPerStep);
    }

    private static long currentThreadIdCompat() {
        return Thread.currentThread().getId();
    }

    private static void run(int threadCount, int calculationSteps, int msBetweenSteps) {
        final Object lock = new Object();
        final int[] numThread = new int[threadCount];
        final long[] threadId = new long[threadCount];
        final String[] threadNames = new String[threadCount];
        final AtomicIntegerArray progress = new AtomicIntegerArray(threadCount);
        final long[] timeThread = new long[threadCount];
        final boolean[] isEnd = new boolean[threadCount];

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch allDone = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                synchronized (lock) {
                    numThread[idx] = idx + 1;
                    threadId[idx] = currentThreadIdCompat();
                    threadNames[idx] = Thread.currentThread().getName();
                }
                long t0 = System.nanoTime();
                for (int s = 0; s < calculationSteps; s++) {
                    progress.set(idx, s + 1);
                    try {
                        Thread.sleep(msBetweenSteps);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
                synchronized (lock) {
                    timeThread[idx] = elapsed;
                    isEnd[idx] = true;
                }
                allDone.countDown();
            });
        }

        Thread display = new Thread(() -> {
            try {
                startLatch.countDown();
                while (allDone.getCount() > 0) {
                    printFrame(lock, threadCount, calculationSteps, numThread, threadId, threadNames,
                            progress, timeThread, isEnd);
                    Thread.sleep(DISPLAY_REFRESH_MS);
                }
                printFrame(lock, threadCount, calculationSteps, numThread, threadId, threadNames,
                        progress, timeThread, isEnd);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "display");
        display.setDaemon(true);
        display.start();

        try {
            Thread.sleep(50);
            allDone.await();
            display.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }

        System.out.println();
        System.out.println("end");
    }

    private static void printFrame(
            Object stateLock,
            int threadCount,
            int totalSteps,
            int[] orderNumber,
            long[] threadIds,
            String[] threadNames,
            AtomicIntegerArray progress,
            long[] elapsedMs,
            boolean[] done) {

        StringBuilder sb = new StringBuilder(512);
        sb.append(ESC).append("[2J").append(ESC).append("[H");

        synchronized (stateLock) {
            for (int i = 0; i < threadCount; i++) {
                int ord = orderNumber[i] > 0 ? orderNumber[i] : i + 1;
                String idStr = threadIds[i] > 0 ? String.valueOf(threadIds[i]) : "  —  ";
                String name = threadNames[i] != null ? threadNames[i] : "(start…)";

                int p = progress.get(i);
                double ratio = totalSteps > 0 ? Math.min(1.0, (double) p / totalSteps) : 0;
                int filled = (int) Math.round(ratio * BAR_WIDTH);
                String bar = buildBar(filled, BAR_WIDTH);
                int pct = (int) Math.round(ratio * 100);

                sb.append(String.format("№%-2d | id=%-5s | %-12s | [%s] %3d%%",
                        ord, idStr, truncate(name, 12), bar, pct));

                if (done[i]) {
                    sb.append(String.format(" | time of thread: %d ms", elapsedMs[i]));
                }
                sb.append(System.lineSeparator());
            }
        }

        System.out.print(sb);
        System.out.flush();
    }

    private static String buildBar(int filled, int width) {
        StringBuilder b = new StringBuilder(width);
        for (int k = 0; k < width; k++) {
            b.append(k < filled ? '█' : '░');
        }
        return b.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
