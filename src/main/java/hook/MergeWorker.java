package hook;


import java.util.Iterator;

public class MergeWorker implements Runnable {


    @Override
    public void run() {
        System.out.println(" Merge Worker triggered at hour start");


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
        mergePrivatesAndMain();


        PauseController.resume();
        System.out.println("Threads resumed.");
    }

    private void mergePrivatesAndMain() {
        Iterator<PrivateWorker> iterator = RetryWorker.privateWorkers.iterator();

        while (iterator.hasNext()) {
            PrivateWorker worker = iterator.next();

            if (AppConfig.getMainLastOffset() <= worker.getSubscriber().getOffset()) {
                System.out.println("[MergeWorker] Merging subscriber: " + worker.getSubscriber().getUrl() +
                        " (private offset=" + worker.getSubscriber().getOffset() +
                        ", main offset=" + AppConfig.getMainLastOffset() + ")");

                ManagerDB.insertToSubscribers(worker.getSubscriber());
                ManagerDB.deleteFromPrivate(worker.getSubscriber());

                worker.stop();

                iterator.remove();
            } else {
                System.out.println("[MergeWorker] Skipped subscriber: " + worker.getSubscriber().getUrl() +
                        " (private offset=" + worker.getSubscriber().getOffset() +
                        ", main offset=" + AppConfig.getMainLastOffset() + ")");
            }
        }
    }
}
