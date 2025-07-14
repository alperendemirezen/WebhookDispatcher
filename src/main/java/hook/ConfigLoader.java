package hook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public static Properties load(String filePath) throws Exception {
        Properties props = new Properties();
        try (InputStream input =  new FileInputStream(filePath)) {
            if (input == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }
            props.load(input);
        }
        return props;
    }
}
