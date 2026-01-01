package application.utils;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerUtil {
    private static final String LOG_PATH = System.getProperty("log.path", "logs/app.log");

    public static void log(String message) {
        try {
            File logFile = new File(LOG_PATH);
            logFile.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                        + " - " + message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
