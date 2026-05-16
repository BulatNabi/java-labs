package univapp;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Defaults {
    private static final String FILE = "defaults.properties";
    private static Properties props;

    public static Properties load() {
        if (props == null) {
            props = new Properties();
            Path p = Path.of(FILE);
            if (Files.exists(p)) {
                try (FileInputStream fis = new FileInputStream(FILE)) {
                    props.load(fis);
                } catch (IOException e) {
                    System.err.println("Не удалось прочитать defaults.properties: " + e.getMessage());
                }
            } else {
                System.err.println("Файл " + FILE + " не найден, используются жёсткие значения");
            }
        }
        return props;
    }

    public static String get(String key, String fallback) {
        return load().getProperty(key, fallback);
    }
}
