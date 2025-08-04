package hook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import log.Log4jUtility;

public class RetryWorker implements Runnable {

    public List<PrivateWorker> privateWorkers = Collections.synchronizedList(new ArrayList<>());
    public List<Thread> privateWorkersThreads = Collections.synchronizedList(new ArrayList<>());
    private String topic;

    public String getTopic() {
        return topic;
    }
    public RetryWorker(String topic) {
      this.topic = topic;
    }

    @Override
    public void run() {

        Log4jUtility.getLogger().debug("Started on thread: " + Thread.currentThread().getName());
        ThreadStatusManager.registerThread();
        try {
            int retryLimit = AppConfig.getRetryCount();
            String mode = AppConfig.getRetryMode();


            while (true) {
                try {
                    List<FailedMessage> failedList = ManagerDB.getAllFailedMessages(topic);

                    for (FailedMessage msg : failedList) {
                        if (!Thread.currentThread().isInterrupted()) {

                            int statusCode = WebhookSender.send(msg.getUrl(), msg.getMessage(), msg.getOffset());

                            if (statusCode == 200) {

                                //Konum değiştirildi burada
                                ManagerDB.deleteFromFailedMessages(msg.getId(), msg.getUrl(), mode);
                                Log4jUtility.getLogger().debug("Retried and deleted successful: offset=" + msg.getOffset() + " url=" + msg.getUrl());

                                if (mode.equals("unlimited")) {

                                    Subscriber subscriber = new Subscriber(msg.getUrl(), msg.getOffset(), msg.getTopic());
                                    ManagerDB.insertPrivateSubscriber(subscriber);
                                    Log4jUtility.getLogger().debug("Inserted to private subscriber with url: " + subscriber.getUrl() + " and offset: " + subscriber.getOffset());

                                    PrivateWorker pw = new PrivateWorker(subscriber);
                                    privateWorkers.add(pw);
                                    Thread thread = new Thread(pw);
                                    privateWorkersThreads.add(thread);
                                    thread.start();

                                }

                            } else {
                                ManagerDB.incrementRetryCount(msg.getId());
                                Log4jUtility.getLogger().error("Retry failed, retry count increased: " + msg.getUrl());


                                if (mode.equals("limited") && msg.getRetryCount() >= retryLimit) {
                                    ManagerDB.deleteFromFailedMessages(msg.getId(), msg.getUrl(), mode);
                                }
                            }
                        }
                    }

                    PauseController.waitIfPaused();
                    safeSleepWithPause(AppConfig.getRetryPeriodMs());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ThreadStatusManager.unregisterThread();
        }
    }

    public static void safeSleepWithPause(long totalSleepMs) {
        long slept = 0;
        long chunk = 100;
        while (slept < totalSleepMs) {
            PauseController.waitIfPaused();
            long sleepTime = Math.min(chunk, totalSleepMs - slept);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            slept += sleepTime;
        }
    }


    public List<PrivateWorker> getPrivateWorkers() {
        return privateWorkers;
    }
    public List<Thread> getPrivateWorkersThreads() {
        return privateWorkersThreads;
    }


}


