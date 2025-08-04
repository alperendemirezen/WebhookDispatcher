package hook;


import java.util.Iterator;

import log.Log4jUtility;

public class MergeWorker implements Runnable {


    @Override
    public void run() {
        Log4jUtility.getLogger().debug(" Merge Worker triggered at hour start");
        PauseController.pause();

        while (!ThreadStatusManager.allPaused()) {
            ThreadStatusManager.logThreadStatus();
            Log4jUtility.getLogger().debug("Waiting for all threads to pause... Active: " + ThreadStatusManager.getRunningThreadCount());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Log4jUtility.getLogger().debug("All threads paused. Performing merge action...");
        mergePrivatesAndMain();


        PauseController.resume();
        Log4jUtility.getLogger().debug("Threads resumed.");

    }

    private void mergePrivatesAndMain() {
        for(MainWorker mainWorker :KafkaServlet.threadManagerTask.getMainWorkers()){
            RetryWorker retryWorker = KafkaServlet.threadManagerTask.getRetryWorkerByTopic(mainWorker.getTopic());

            Iterator<PrivateWorker> iterator = retryWorker.getPrivateWorkers().iterator();

            while(iterator.hasNext()) {
                PrivateWorker privateWorker = iterator.next();

                if (privateWorker.getSubscriber().getOffset()>= mainWorker.getLastOffset()) {
                    Log4jUtility.getLogger().debug("Merging subscriber: " + privateWorker.getSubscriber().getUrl() +
                            " (private offset=" + privateWorker.getSubscriber().getOffset() +
                            ", main offset=" + mainWorker.getLastOffset() + ")");
                    ManagerDB.insertToSubscribers(privateWorker.getSubscriber());
                    ManagerDB.deleteFromPrivate(privateWorker.getSubscriber());
                    privateWorker.stop();

                }
            }
        }
    }
}
