
package hook;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadStatusManager {
    private static final AtomicInteger totalThreadCount = new AtomicInteger(0);
    private static final AtomicInteger pausedThreadCount = new AtomicInteger(0);

    public static void registerThread() {
        totalThreadCount.incrementAndGet();
    }

    public static void unregisterThread() {
        totalThreadCount.decrementAndGet();
    }

    public static void markPaused() {
        pausedThreadCount.incrementAndGet();
    }

    public static void markResumed() {
        pausedThreadCount.decrementAndGet();
    }

    public static boolean allPaused() {
        return pausedThreadCount.get() == totalThreadCount.get();
    }

    public static int getRunningThreadCount() {
        return totalThreadCount.get() - pausedThreadCount.get();
    }

    public static void logThreadStatus() {
        System.out.println("[ThreadStatus] Total: " + totalThreadCount.get() + ", Paused: " + pausedThreadCount.get());
    }
}
