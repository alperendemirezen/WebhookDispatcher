package hook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import log.Log4jUtility;

public class AppConfig {

    private static volatile Properties config;
    private static final String CONFIG_DIR = System.getProperty("user.dir") +
            File.separator + "KafkaServlet" + File.separator + "Configuration";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.ini";

    public static void readConfig() throws Exception {
        if (config != null) {
            return;
        }

        createConfigDirectoryIfNotExists();
        copyConfigFileIfNotExists();
        loadConfiguration();
    }

    private static void createConfigDirectoryIfNotExists() {
        File confDirectory = new File(CONFIG_DIR);
        if (!confDirectory.exists()) {
            confDirectory.mkdirs();
        }
    }

    private static void copyConfigFileIfNotExists() throws IOException {
        File confFile = new File(CONFIG_FILE);
        if (confFile.exists()) {
            return;
        }

        try (InputStream input = KafkaServlet.class.getClassLoader()
                .getResourceAsStream("config.ini")) {

            if (input == null) {
                throw new IOException("config.ini not found in classpath");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(new FileOutputStream(confFile), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    private static void loadConfiguration() throws Exception {
        config = ConfigLoader.load(CONFIG_FILE);
        if (config == null) {
            throw new Exception("Cannot read config.ini");
        }
    }

    public static int getRetryCount() {
        try {
            String value = config.getProperty("retry.count");
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 3;
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    public static int getTimeoutMs() {
        try {
            String value = config.getProperty("timeoutMS");
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 100;
        } catch (NumberFormatException e) {
            Log4jUtility.getLogger().error("Invalid timeoutMS value in config. Using default 100 ms.");
            return 100;
        }
    }

    public static int getRetryPeriodMs() {
        try {
            String value = config.getProperty("retry.period.min");
            if (value != null && !value.isEmpty()) {
                double minutes = Double.parseDouble(value);
                return (int) (minutes * 60 * 1000);
            } else {
                return 60000;
            }
        } catch (NumberFormatException e) {
            Log4jUtility.getLogger().error("Invalid retry.period.min value in config. Using default 60000 ms.");
            return 60000;
        }
    }

    public static String getRetryMode() {
        String mode = config.getProperty("retry.mode");
        if (mode != null) {
            mode = mode.trim().toLowerCase();
            if (mode.equals("limited") || mode.equals("unlimited")) {
                return mode;
            }
        }
        return "unlimited";
    }

    public static int getCheckTopicsIntervalMinutes() {
        String intervalStr = config.getProperty("check.topics.interval.minutes");
        if (intervalStr != null) {
            try {
                int interval = Integer.parseInt(intervalStr.trim());
                if (interval > 0) {
                    return interval;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 60;
    }

    public static Properties getConfig() {
        return config;
    }

    public static long getMergeThreadIntervalMs() {
        String intervalStr = config.getProperty("merge.thread.interval.minutes");
        if (intervalStr != null) {
            try {
                double minutes = Double.parseDouble(intervalStr.trim());
                if (minutes > 0) {
                    return (long) (minutes * 60 * 1000);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 60 * 60 * 1000;
    }

}