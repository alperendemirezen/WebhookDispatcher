package hook;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HourlyCoordinatorScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void startScheduler() {
        long initialDelay = 5000; //getDelayUntilNextHour();
        long period =30000; // TimeUnit.HOURS.toMillis(1);

        scheduler.scheduleAtFixedRate(new MergeWorker(), initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private static long getDelayUntilNextHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }
}
