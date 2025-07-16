package hook;

import java.util.List;

public class RetryWorker implements Runnable {

    @Override
    public void run() {

        System.out.println("[RetryWorker] Started on thread: " + Thread.currentThread().getName());
        ThreadStatusManager.registerThread();
        try {
            int retryLimit = AppConfig.getRetryCount();
            String mode = AppConfig.getRetryMode();


            while (true) {
                try {

                    System.out.println("[RetryWorker] Checking failed messages...");
                    List<FailedMessage> failedList = ManagerDB.getAllFailedMessages();

                    for (FailedMessage msg : failedList) {

                        int statusCode = WebhookSender.send(msg.getUrl(), msg.getMessage());

                        if (statusCode == 200) {
                            if (mode.equals("unlimited")) {

                                Subscriber subscriber = new Subscriber(msg.getUrl(), msg.getOffset());
                                ManagerDB.insertPrivateSubscriber(subscriber);

                                Thread thread = new Thread(new PrivateWorker(subscriber));
                                thread.start();

                            }

                            ManagerDB.deleteFromFailedMessages(msg.getId(), msg.getUrl(), mode);
                            System.out.println("Retried and deleted successful: offset=" + msg.getOffset() + " url=" + msg.getUrl());

                        } else {
                            ManagerDB.incrementRetryCount(msg.getId());
                            System.out.println("Retry failed, retry count increased: " + msg.getUrl());


                            if (mode.equals("limited") && msg.getRetryCount() >= retryLimit) {
                                ManagerDB.deleteFromFailedMessages(msg.getId(), msg.getUrl(), mode);
                            }

                        }

                        PauseController.waitIfPaused();
                    }


                    //Thread.sleep(AppConfig.getRetryPeriodMs());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ThreadStatusManager.unregisterThread();
        }
    }
}


