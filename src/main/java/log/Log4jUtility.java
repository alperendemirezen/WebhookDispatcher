package log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.HashMap;
import org.apache.logging.log4j.ThreadContext;

public class Log4jUtility {

    private static HashMap<Class<?>, Object> loggerMap = new HashMap<>();

    public static Logger getLogger() {
        Class<?> clazz = Log4jUtility.class;
        try {
            StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
            clazz = Class.forName(ste.getClassName());
            ThreadContext.put("method", ste.getMethodName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger logger = (Logger) loggerMap.get(clazz);
        if (logger == null) {
            logger = LogManager.getLogger(clazz);
            loggerMap.put(clazz, logger);
        }
        return logger;
    }

}
