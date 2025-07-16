package hook;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


public class KafkaServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        try {

            AppConfig.readConfig();

            MainWorker task = new MainWorker();
            Thread mainThread = new Thread(task);
            mainThread.setDaemon(true);
            mainThread.start();



            Runnable retryTask = new RetryWorker();

            Thread retryThread = new Thread(retryTask);
            retryThread.setDaemon(true);
            retryThread.start();

            if(AppConfig.getRetryMode().equals("unlimited")){
                HourlyCoordinatorScheduler.startScheduler();
            }


        } catch (Exception e) {
            throw new ServletException("Failed to start Kafka Consumer", e);
        }
    }
}
