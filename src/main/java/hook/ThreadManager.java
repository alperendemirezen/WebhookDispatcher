package hook;

import log.Log4jUtility;

import java.util.ArrayList;
import java.util.List;

public class ThreadManager implements Runnable {

    public static ArrayList<String> topics = new ArrayList<>();
    private static boolean firstRunning = true;
    private List<Thread> mainThreads = new ArrayList<>();
    private List<MainWorker> mainWorkers = new ArrayList<>();
    private List<Thread> retryThreads = new ArrayList<>();
    private List<RetryWorker> retryWorkers = new ArrayList<>();


    @Override
    public void run() {

        if(firstRunning){
            Log4jUtility.getLogger().debug("ThreadManager started running");
            if (AppConfig.getRetryMode().equals("unlimited")) {
                HourlyScheduler.startScheduler();
            }
            firstRunning=false;
        }

        for(String topic : ManagerDB.getTopics()){
            if (!topics.contains(topic)) {
                Log4jUtility.getLogger().debug("New topic added: " + topic);
                newTopicAdded(topic);
                OffsetConfig.addTopic(topic);
                topics.add(topic);
            } else {
                Log4jUtility.getLogger().debug("Topic already exists: " + topic);
            }
        }
    }

    private void newTopicAdded(String topic){

        try {
            MainWorker task = new MainWorker(topic);
            mainWorkers.add(task);
            Thread mainThread = new Thread(task);
            mainThread.start();
            mainThreads.add(mainThread);


            RetryWorker retryTask = new RetryWorker(topic);
            retryWorkers.add(retryTask);
            Thread retryThread = new Thread(retryTask);
            retryThread.start();
            retryThreads.add(retryThread);

            if (AppConfig.getRetryMode().equals("unlimited")) {
                ArrayList<Subscriber> privateSubscribers = ManagerDB.getPrivateWorkersFromDB(topic);

                for (Subscriber subscriber : privateSubscribers) {
                    PrivateWorker pw = new PrivateWorker(subscriber);
                    retryTask.getPrivateWorkers().add(pw);
                    Thread thread = new Thread(pw);
                    retryTask.getPrivateWorkersThreads().add(thread);
                    thread.start();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void stopEverything() {
        for (Thread mainThread : mainThreads) {
            try {
                if (mainThread.isAlive()) {
                    Log4jUtility.getLogger().debug("Stopping main thread: " + mainThread.getName());
                    mainThread.interrupt();
                }
            } catch (Exception e) {
                Log4jUtility.getLogger().error("Error stopping main thread: " + mainThread.getName(), e);
            }

        }

        for (RetryWorker retryWorker : retryWorkers) {

            for (Thread t : retryWorker.getPrivateWorkersThreads()) {
                try {
                    Log4jUtility.getLogger().debug("Stopping private worker thread: " + t.getName());
                    t.interrupt();
                } catch (Exception e) {
                    Log4jUtility.getLogger().error("Error stopping private worker thread: " + t.getName(), e);
                }


            }
        }
        for (Thread retryThread : retryThreads) {
            try {
                if (retryThread.isAlive()) {
                    Log4jUtility.getLogger().debug("Stopping retry thread: " + retryThread.getName());
                    retryThread.interrupt();
                }
            } catch (Exception e) {
                Log4jUtility.getLogger().error("Error stopping retry thread: " + retryThread.getName(), e);
            }
        }


    }

    public RetryWorker getRetryWorkerByTopic(String topic) {
        for (RetryWorker retryWorker : retryWorkers) {
            if (retryWorker.getTopic().equals(topic)) {
                return retryWorker;
            }
        }
        return null;
    }

    public void getStopEverything() {
        stopEverything();
    }

    public List<MainWorker> getMainWorkers() {
        return mainWorkers;
    }


}





