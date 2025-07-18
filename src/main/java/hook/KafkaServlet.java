package hook;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;


public class KafkaServlet extends HttpServlet {

    private Thread mainThread;
    private Thread retryThread;


    @Override
    public void init() throws ServletException {
        super.init();
        try {

            AppConfig.readConfig();

            MainWorker task = new MainWorker();
            mainThread = new Thread(task);
            mainThread.start();

            Runnable retryTask = new RetryWorker();
            retryThread = new Thread(retryTask);
            retryThread.start();

            if (AppConfig.getRetryMode().equals("unlimited")) {
                HourlyScheduler.startScheduler();

                ArrayList<Subscriber> privateSubscribers = ManagerDB.getPrivateWorkersFromDB();

                for (Subscriber subscriber : privateSubscribers) {
                    PrivateWorker pw = new PrivateWorker(subscriber);
                    Thread thread = new Thread(pw);
                    RetryWorker.privateWorkersThreads.add(thread);
                    thread.start();
                }
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[ShutdownHook] JVM shutdown detected. Cleaning up...");
                stopEverything();
                System.out.println("[ShutdownHook] Cleanup complete.");
            }));

        } catch (Exception e) {
            throw new ServletException("Failed to start Kafka Consumer", e);
        }
    }

    @Override
    public void destroy() {
        System.out.println("[KafkaServlet] destroy() called. Cleaning up...");
        stopEverything();
        System.out.println("[KafkaServlet] Cleanup from destroy() complete.");
        super.destroy();
    }

    private void stopEverything() {
        try {
            mainThread.interrupt();
            retryThread.interrupt();

            for (Thread t : RetryWorker.privateWorkersThreads) {
                t.interrupt();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
