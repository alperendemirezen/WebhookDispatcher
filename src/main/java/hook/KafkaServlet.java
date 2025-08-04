package hook;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import log.Log4jUtility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class KafkaServlet extends HttpServlet {

    private static final long serialVersionUID = 3549137072967571143L;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static ThreadManager threadManagerTask = new ThreadManager();


    @Override
    public void init() throws ServletException {
        super.init();
        try {
            OffsetConfig.initializeOffsetConfig();
            AppConfig.readConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long period = AppConfig.getCheckTopicsIntervalMinutes();
        long periodMillis = TimeUnit.MINUTES.toMillis(period);
        scheduler.scheduleAtFixedRate(threadManagerTask, 1000, periodMillis, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Log4jUtility.getLogger().debug("[ShutdownHook] JVM shutdown detected. Cleaning up...");
            threadManagerTask.getStopEverything();
            Log4jUtility.getLogger().debug("[ShutdownHook] Cleanup complete.");
        }));

    }

    @Override
    public void destroy() {
        Log4jUtility.getLogger().debug("[KafkaServlet] destroy() called. Cleaning up...");
        threadManagerTask.getStopEverything();
        Log4jUtility.getLogger().debug("[KafkaServlet] Cleanup from destroy() complete.");
        super.destroy();
    }




}
