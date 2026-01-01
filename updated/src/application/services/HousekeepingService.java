package application.services;

import application.models.*;
import application.models.Room;
import application.services.dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.util.stream.Collectors;

public class HousekeepingService {

    private final HousekeepingDAO housekeepingDAO;
    private final StaffDAO staffDAO;
    private final RoomDAO roomDAO;

    // Cache for frequently accessed data - FIXED with proper generics
    private Map<Long, Staff> staffCache;
    private Map<String, Room> roomCache;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 300000; // 5 minutes

    public HousekeepingService() {
        this.housekeepingDAO = new HousekeepingDAO();
        this.staffDAO = new StaffDAO();
        this.roomDAO = new RoomDAO();
        this.staffCache = new HashMap<>();
        this.roomCache = new HashMap<>();
    }

    // ==================== TASK MANAGEMENT ====================

    /**
     * Get all housekeeping tasks from database
     */
    public ObservableList<HousekeepingTask> getAllTasks() {
        try {
            refreshCacheIfNeeded();
            List<HousekeepingTask> tasks = housekeepingDAO.getAllHousekeepingTasks();
            
            // Populate staff names from cache
            for (HousekeepingTask task : tasks) {
                if (task.getStaffId() != null) {
                    Staff staff = staffCache.get(task.getStaffId());
                    if (staff != null) {
                        task.setAssignedStaff(staff.getName());
                    }
                }
            }
            
            return FXCollections.observableArrayList(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Get tasks assigned to specific staff member
     */
    public ObservableList<HousekeepingTask> getTasksForStaff(String staffName) {
        try {
            refreshCacheIfNeeded();
            
            // Find staff ID by name
            Long staffId = null;
            for (Staff staff : staffCache.values()) {
                if (staff.getName().equals(staffName)) {
                    staffId = staff.getId();
                    break;
                }
            }
            
            if (staffId == null) {
                return FXCollections.observableArrayList();
            }
            
            List<HousekeepingTask> tasks = housekeepingDAO.getTasksByStaffId(staffId);
            
            // Populate staff names
            for (HousekeepingTask task : tasks) {
                task.setAssignedStaff(staffName);
            }
            
            return FXCollections.observableArrayList(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Save or update a housekeeping task
     */
    public boolean saveTask(HousekeepingTask task) {
        try {
            refreshCacheIfNeeded();
            
            // Convert staff name to ID if needed
            if (task.getStaffId() == null && task.getAssignedStaff() != null) {
                for (Staff staff : staffCache.values()) {
                    if (staff.getName().equals(task.getAssignedStaff())) {
                        task.setStaffId(staff.getId());
                        break;
                    }
                }
            }
            
            if (task.getId() == null) {
                housekeepingDAO.addHousekeepingTask(task);
            } else {
                housekeepingDAO.updateHousekeepingTask(task);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a housekeeping task
     */
    public boolean deleteTask(Long taskId) {
        try {
            if (taskId == null) return false;
            housekeepingDAO.deleteHousekeepingTask(taskId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== DROPDOWN DATA ====================

    /**
     * Get all room numbers for dropdown
     */
    public ObservableList<String> getAllRoomNumbers() {
        try {
            refreshCacheIfNeeded();
            List<String> roomNumbers = roomCache.values().stream()
                .map(Room::getRoomNo)
                .sorted()
                .collect(Collectors.toList());
            return FXCollections.observableArrayList(roomNumbers);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Get all housekeeping staff names
     */
    public ObservableList<String> getAllStaffNames() {
        try {
            refreshCacheIfNeeded();
            List<String> staffNames = staffCache.values().stream()
                .filter(staff -> "Housekeeping".equals(staff.getDepartment()) && staff.isActive())
                .map(Staff::getName)
                .sorted()
                .collect(Collectors.toList());
            return FXCollections.observableArrayList(staffNames);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Get task types
     */
    public ObservableList<String> getTaskTypes() {
        return FXCollections.observableArrayList(
            "🧹 Cleaning", "🛏️ Change Bedsheet", "🧼 Sanitize", "🪣 Mopping",
            "🔧 Maintenance", "🏠 Turn-down", "🔍 Inspection", "🧽 Deep Clean"
        );
    }

    /**
     * Get task statuses
     */
    public ObservableList<String> getStatuses() {
        return FXCollections.observableArrayList(
            "Pending", "In Progress", "Completed", "Issue", "On Hold"
        );
    }

    /**
     * Get task priorities
     */
    public ObservableList<String> getPriorities() {
        return FXCollections.observableArrayList("Low", "Medium", "High", "Urgent");
    }

    // ==================== CACHE METHODS - FIXED ====================

    /**
     * Refresh cache if needed
     */
    private void refreshCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_DURATION) {
            refreshCache();
        }
    }

    /**
     * Refresh staff and room cache - FIXED with proper generics
     */
    private void refreshCache() {
        try {
            // Refresh staff cache
            List<Staff> allStaff = staffDAO.getAllStaff();
            staffCache.clear();
            for (Staff staff : allStaff) {
                staffCache.put(staff.getId(), staff);
            }

            // Refresh room cache - FIXED
            List<Room> allRooms = roomDAO.getAllRooms();
            roomCache.clear();
            for (Room room : allRooms) {
                roomCache.put(room.getRoomNo(), room);
            }

            lastCacheUpdate = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Force cache refresh
     */
    public void forceRefreshCache() {
        refreshCache();
    }

    /**
     * Check if staff exists
     */
    public boolean staffExists(String staffName) {
        refreshCacheIfNeeded();
        return staffCache.values().stream()
            .anyMatch(staff -> staff.getName().equals(staffName));
    }

    /**
     * Check if room exists
     */
    public boolean roomExists(String roomNo) {
        refreshCacheIfNeeded();
        return roomCache.containsKey(roomNo);
    }
}
