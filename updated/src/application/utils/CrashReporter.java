package application.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashReporter {

    private static final String LOG_PATH = System.getProperty("log.path", "logs/app.log");

    // Call this method to log exceptions
    public static void logException(Throwable e, String source) {
        try {
            File logFile = new File(LOG_PATH);
            logFile.getParentFile().mkdirs();

            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write("=== CRASH REPORT ===\n");
                fw.write("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                fw.write("Source: " + source + "\n");
                fw.write("Message: " + e.getMessage() + "\n");

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                fw.write("Stack Trace:\n" + sw.toString() + "\n");
                fw.write("=====================\n\n");
            }

            // Show alert dialog (JavaFX Thread safe)
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Unexpected Error");
                alert.setHeaderText("⚠️ Something went wrong!");
                alert.setContentText(
                        "An unexpected error occurred in the system.\n\n" +
                        "Details have been saved in 'logs/app.log'.\n" +
                        "Please send that file to support for troubleshooting."
                );
                alert.showAndWait();
            });

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
