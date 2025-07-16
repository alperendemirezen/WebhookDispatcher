package hook;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;


public class KafkaServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        try {

            AppConfig.readConfig();

            MainWorker task = new MainWorker();
            Thread mainThread = new Thread(task);
            mainThread.start();



            Runnable retryTask = new RetryWorker();

            Thread retryThread = new Thread(retryTask);
            retryThread.start();

            if(AppConfig.getRetryMode().equals("unlimited")){
                HourlyScheduler.startScheduler();

                ArrayList<Subscriber> privateSubscribers = ManagerDB.getPrivateWorkersFromDB();

                for(Subscriber subscriber : privateSubscribers){
                    PrivateWorker pw = new PrivateWorker(subscriber);
                    Thread thread = new Thread(new PrivateWorker(subscriber));
                    thread.start();
                }


            }


        } catch (Exception e) {
            throw new ServletException("Failed to start Kafka Consumer", e);
        }
    }
}
