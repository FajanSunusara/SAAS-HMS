package application.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.embed.swing.SwingFXUtils;

public class BookingFileManager {
    private static final String BASE_DOCUMENTS_PATH = "documents/bookings/";
    
    /**
     * Creates a dedicated folder for a booking
     */
    public static String createBookingFolder(String bookingId) {
        try {
            String folderPath = BASE_DOCUMENTS_PATH + bookingId + "/";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (created) {
                    System.out.println("Created booking folder: " + folder.getAbsolutePath());
                } else {
                    System.err.println("Failed to create booking folder: " + folder.getAbsolutePath());
                    return null;
                }
            }
            return folder.getAbsolutePath() + File.separator;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Saves a document to the booking folder with organized naming
     */
    public static String saveBookingDocument(File sourceFile, String bookingId, 
                                           String documentType, String personName) {
        try {
            String folderPath = createBookingFolder(bookingId);
            if (folderPath == null) {
                throw new IOException("Failed to create booking folder");
            }
            
            String extension = getFileExtension(sourceFile.getName());
            String fileName = generateDocumentFileName(bookingId, documentType, personName, extension);
            String destinationPath = folderPath + fileName;
            
            Files.copy(sourceFile.toPath(), new File(destinationPath).toPath(), 
                      StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Document saved: " + destinationPath);
            return destinationPath;
            
        } catch (IOException e) {
            System.err.println("Error saving document: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Saves an image to the booking folder
     */
    public static String saveBookingImage(javafx.scene.image.Image image, String bookingId, 
                                        String documentType, String personName) {
        try {
            String folderPath = createBookingFolder(bookingId);
            if (folderPath == null) {
                throw new IOException("Failed to create booking folder");
            }
            
            String fileName = generateDocumentFileName(bookingId, documentType, personName, ".png");
            String destinationPath = folderPath + fileName;
            
            // Convert JavaFX Image to BufferedImage and save
            java.awt.image.BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            File outputFile = new File(destinationPath);
            boolean success = javax.imageio.ImageIO.write(bImage, "png", outputFile);
            
            if (success) {
                System.out.println("Image saved: " + destinationPath);
                return destinationPath;
            } else {
                throw new IOException("Failed to write image file");
            }
            
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generates organized file names for documents
     */
    private static String generateDocumentFileName(String bookingId, String docType, 
                                                 String personName, String extension) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String safePersonName = personName.replaceAll("[^a-zA-Z0-9\\-]", "_");
        String safeDocType = docType.toUpperCase().replaceAll("[^a-zA-Z0-9]", "_");
        
        return String.format("%s-%s-%s-%s%s", 
                           bookingId, safeDocType, safePersonName, timestamp, extension);
    }
    
    /**
     * Gets file extension from file name
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".dat";
    }
    
    /**
     * Gets all documents for a booking
     */
    public static File[] getBookingDocuments(String bookingId) {
        String folderPath = BASE_DOCUMENTS_PATH + bookingId + "/";
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            return folder.listFiles();
        }
        return new File[0];
    }
    
    /**
     * Deletes a specific document
     */
    public static boolean deleteDocument(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting document: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cleans up booking folder (use with caution)
     */
    public static boolean cleanupBookingFolder(String bookingId) {
        try {
            String folderPath = BASE_DOCUMENTS_PATH + bookingId + "/";
            File folder = new File(folderPath);
            if (folder.exists() && folder.isDirectory()) {
                return deleteFolder(folder);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error cleaning up booking folder: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Recursively deletes a folder
     */
    private static boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        return folder.delete();
    }
    
    /**
     * Gets the documents folder size for a booking
     */
    public static long getBookingFolderSize(String bookingId) {
        String folderPath = BASE_DOCUMENTS_PATH + bookingId + "/";
        File folder = new File(folderPath);
        return getFolderSize(folder);
    }
    
    /**
     * Calculates folder size recursively
     */
    private static long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += getFolderSize(file);
                }
            }
        }
        return length;
    }
    
    /**
     * Checks if booking folder exists
     */
    public static boolean bookingFolderExists(String bookingId) {
        String folderPath = BASE_DOCUMENTS_PATH + bookingId + "/";
        File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory();
    }
    
    /**
     * Gets the base documents path
     */
    public static String getBaseDocumentsPath() {
        return BASE_DOCUMENTS_PATH;
    }
}