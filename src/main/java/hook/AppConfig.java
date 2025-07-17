package hook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppConfig {
    private static Properties config;
    private static String path = System.getProperty("user.dir") + File.separator + "KafkaServlet" + File.separator + "Configuration";
    private static String confPath = path + File.separator + "env.properties";

    public static void  readConfig() throws Exception {

        File confDirectory = new File(path);
        if (!confDirectory.exists()) {
            confDirectory.mkdirs();
        }

        File confFile = new File(confPath);
        if (!confFile.exists()) {
            try (InputStream input = KafkaServlet.class.getClassLoader().getResourceAsStream("env.properties")) {
                if (input != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(confFile), StandardCharsets.UTF_8))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }

                        writer.flush();
                    }
                } else {
                    System.err.println("File does not found!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AppConfig.setConfig(ConfigLoader.load(confPath));

        if (AppConfig.getConfig() == null) {
            throw new Exception("Cannot read env.properties");
        }

    }


    public static void saveConfig() {
        try (FileOutputStream out = new FileOutputStream(confPath)) {
            config.store(out, "Updated by AppConfig");
        } catch (Exception e) {
            System.err.println("env.properties could not be saved: " + e.getMessage());
        }
    }

    public static long getConfigStartOffset(){
        long offset;

        try{
            String strOffset = AppConfig.config.getProperty("start.offset");
            offset = (strOffset!=null && !strOffset.isEmpty()) ? Long.parseLong(strOffset) : -1;
        }catch (NumberFormatException e){
            offset = -1;
        }
        return  offset;

    }
    public static long getMainLastOffset() {
        try {
            String strOffset = config.getProperty("main.last.offset");
            return (strOffset != null && !strOffset.isEmpty()) ? Long.parseLong(strOffset) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    public static void setMainLastOffset(long offset){
        config.setProperty("main.last.offset",String.valueOf(offset));
    }
    public static void setStartOffset(long offset){
        config.setProperty("main.last.offset",String.valueOf(offset));
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
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 1000;
        } catch (NumberFormatException e) {
            System.err.println("Invalid timeoutMS value in config. Using default 1000 ms.");
            return 1000;
        }
    }

    public static int getRetryPeriodMs() {
        try {
            String value = config.getProperty("retry.period.min");
            int minutes = (value != null && !value.isEmpty()) ? Integer.parseInt(value) : 3;
            return Math.max(minutes, 1) * 60 * 1000;
        } catch (NumberFormatException e) {
            System.err.println("Invalid retry.period.min in config. Using default 3 minutes.");
            return 3 * 60 * 1000;
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
        return "limited";
    }

    public static Properties getConfig() {
        return config;
    }

    public static void setConfig(Properties config) {
        AppConfig.config = config;
    }
}