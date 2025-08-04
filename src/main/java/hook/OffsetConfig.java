package hook;

import java.io.*;
import java.util.Properties;

public class OffsetConfig {

    private static volatile Properties offsetConfig;
    private static final String CONFIG_DIR = System.getProperty("user.dir") +
            File.separator + "KafkaServlet" + File.separator + "Configuration";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "offsetConfig.ini";

    public static void initializeOffsetConfig() throws Exception {
        File confDirectory = new File(CONFIG_DIR);
        if (!confDirectory.exists()) {
            confDirectory.mkdirs();
        }

        File confFile = new File(CONFIG_FILE);
        if (!confFile.exists()) {

            offsetConfig = new Properties();
            saveOffsetConfig();
        } else {

            offsetConfig = new Properties();
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                offsetConfig.load(in);
            }
        }
    }

    public static synchronized void addTopic(String topicName) {

        String offsetKey = topicName + ".main.last.offset";
        if (!offsetConfig.containsKey(offsetKey)) {
            offsetConfig.setProperty(offsetKey, "0");
            saveOffsetConfig();
        }
    }

    public static synchronized void updateMainOffset(String topicName, long offset) {

        String offsetKey = topicName + ".main.last.offset";
        offsetConfig.setProperty(offsetKey, String.valueOf(offset));
        saveOffsetConfig();
    }

    public static synchronized void saveOffsetConfig() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            offsetConfig.store(out, "Updated by OffsetConfig");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getMainOffset(String topicName) {
        long offset;
        try {
            String offsetKey = topicName + ".main.last.offset";
            String strOffset = offsetConfig.getProperty(offsetKey);
            offset = (strOffset != null && !strOffset.isEmpty()) ? Long.parseLong(strOffset) : -1;
        } catch (NumberFormatException e) {
            offset = -1;
        }
        return offset;
    }

}