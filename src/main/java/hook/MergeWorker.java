package hook;

public class MergeWorker implements Runnable {

    @Override
    public void run() {
        System.out.println("🕐 Coordinator triggered at hour start");

        PauseController.pause();

        while (!ThreadStatusManager.allPaused()) {
            ThreadStatusManager.logThreadStatus();
            System.out.println("Waiting for all threads to pause... Active: " + ThreadStatusManager.getRunningThreadCount());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


        System.out.println("All threads paused. Performing merge action...");
        performCriticalAction();


        PauseController.resume();
        System.out.println("Threads resumed.");
    }

    private void performCriticalAction() {
        System.out.println("MERGE ACTION IS DONE");
        System.out.println("MERGE ACTION IS DONE");
        System.out.println("MERGE ACTION IS DONE");
        System.out.println("MERGE ACTION IS DONE");
        System.out.println("MERGE ACTION IS DONE");
        System.out.println("MERGE ACTION IS DONE");
        System.out.println("MERGE ACTION IS DONE");


    }
}
