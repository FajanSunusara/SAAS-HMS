package application.services;

import application.services.dao.DailyChargesDAO;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service to automatically update daily charges for all active bookings
 * Runs every morning at 6 AM
 */
public class DailyChargesService extends ScheduledService<Void> {
    
    private final DailyChargesDAO dailyChargesDAO;
    private static DailyChargesService instance;
    private ScheduledExecutorService scheduler;
    
    private DailyChargesService() {
        this.dailyChargesDAO = new DailyChargesDAO();
        setupScheduler();
    }
    
    public static synchronized DailyChargesService getInstance() {
        if (instance == null) {
            instance = new DailyChargesService();
        }
        return instance;
    }
    
    private void setupScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);
        
        // Calculate delay until next 6 AM
        LocalTime now = LocalTime.now();
        LocalTime targetTime = LocalTime.of(6, 0); // 6:00 AM
        
        long initialDelay;
        if (now.isBefore(targetTime)) {
            initialDelay = now.until(targetTime, java.time.temporal.ChronoUnit.SECONDS);
        } else {
            // Next day at 6 AM
            initialDelay = now.until(targetTime.plusHours(24), java.time.temporal.ChronoUnit.SECONDS);
        }
        
        // Schedule daily execution at 6 AM
        scheduler.scheduleAtFixedRate(
            this::runDailyUpdate,
            initialDelay,
            24 * 60 * 60, // 24 hours
            TimeUnit.SECONDS
        );
        
        System.out.println("Daily charges service scheduled to run at 6:00 AM daily");
    }
    
    private void runDailyUpdate() {
        try {
            System.out.println("Starting daily charges update at " + LocalTime.now());
            dailyChargesDAO.runDailyUpdate();
            System.out.println("Daily charges update completed successfully");
        } catch (Exception e) {
            System.err.println("Error during daily charges update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Manual trigger for daily update (for testing or immediate execution)
     */
    public void triggerManualUpdate() {
        try {
            System.out.println("Manual daily charges update triggered");
            dailyChargesDAO.runDailyUpdate();
            System.out.println("Manual daily charges update completed");
        } catch (Exception e) {
            System.err.println("Error during manual daily charges update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runDailyUpdate();
                return null;
            }
        };
    }
    
    public void startService() {
        if (!isRunning()) {
            start();
            System.out.println("Daily charges service started");
        }
    }
    
    public void stopService() {
        if (isRunning()) {
            cancel();
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        System.out.println("Daily charges service stopped");
    }
}
