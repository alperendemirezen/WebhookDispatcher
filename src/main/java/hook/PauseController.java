package hook;

public class PauseController {
    private static final Object lock = new Object();
    private static volatile boolean paused = false;

    public static void pause() {
        paused = true;
    }

    public static void resume() {
        synchronized (lock) {
            paused = false;
            lock.notifyAll();
        }
    }

    public static void waitIfPaused() {
        synchronized (lock) {
            while (paused) {
                ThreadStatusManager.markPaused();
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    ThreadStatusManager.markResumed();
                }
            }
        }
    }


    public static boolean isPaused() {
        return paused;
    }
}
