package application.controllers;

import application.models.PhotoIdVerification;
import application.models.VerifiedPerson;
import application.services.dao.PhotoIdVerificationDAO;
import application.utils.SimpleCameraCapture;
import application.utils.WebcamCapture;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PhotoIdVerificationController implements Initializable {

    @FXML private ComboBox<Integer> totalPersonsComboBox;
    @FXML private Button captureMainGuestBtn;
    @FXML private ImageView mainGuestImageView;
    @FXML private VBox personsContainer;
    @FXML private TextArea notesTextArea;
    @FXML private Label statusLabel;
    @FXML private Button resetAllButton;

    private Long bookingId;
    private String mainGuestPhotoPath;
    private List<PersonForm> personForms = new ArrayList<>();
    private PhotoIdVerificationDAO verificationDAO = new PhotoIdVerificationDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PhotoIdVerificationController initialized");
        setupTotalPersonsComboBox();
        setupEventHandlers();
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
        System.out.println("Booking ID set: " + bookingId);
        loadExistingVerification();
    }

    private void setupTotalPersonsComboBox() {
        for (int i = 1; i <= 10; i++) {
            totalPersonsComboBox.getItems().add(i);
        }
        totalPersonsComboBox.setValue(1);
        
        totalPersonsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePersonForms();
            }
        });
        
        // Initialize with one person form
        updatePersonForms();
    }

    private void setupEventHandlers() {
        if (resetAllButton != null) {
            resetAllButton.setOnAction(e -> handleResetAll());
        }
    }

    private void updatePersonForms() {
        personsContainer.getChildren().clear();
        personForms.clear();
        
        Integer totalPersons = totalPersonsComboBox.getValue();
        if (totalPersons == null) {
            totalPersons = 1;
        }
        
        for (int i = 0; i < totalPersons; i++) {
            PersonForm personForm = new PersonForm(i + 1);
            personsContainer.getChildren().add(personForm);
            personForms.add(personForm);
        }
    }

    @FXML
    private void handleCaptureMainGuestPhoto() {
        try {
            Stage stage = (Stage) captureMainGuestBtn.getScene().getWindow();
            Image capturedImage = SimpleCameraCapture.captureFromCamera(stage);
            
            if (capturedImage != null) {
                mainGuestImageView.setImage(capturedImage);
                
                // Save to temporary file
                File tempFile = File.createTempFile("guest_photo_", ".png");
                if (WebcamCapture.saveImage(capturedImage, tempFile)) {
                    mainGuestPhotoPath = tempFile.getAbsolutePath();
                    updateStatus("Main guest photo captured and saved");
                    System.out.println("Main guest photo saved to: " + mainGuestPhotoPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Capture Error", "Failed to capture main guest photo: " + e.getMessage());
        }
    }

    @FXML
    private void handleCompleteVerification() {
        if (!validateForm()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                "Please complete all required fields:\n\n" +
                "• Capture main guest photo\n" +
                "• Fill all person details\n" +
                "• Capture photos for all persons");
            return;
        }

        try {
            // Save verification to database
            PhotoIdVerification verification = new PhotoIdVerification(bookingId, totalPersonsComboBox.getValue());
            verification.setMainGuestPhotoPath(mainGuestPhotoPath);
            verification.setVerifiedBy("Admin");
            verification.setStatus("Completed");
            verification.setNotes(notesTextArea.getText());

            Long verificationId = verificationDAO.saveVerification(verification);
            
            if (verificationId != null) {
                // Save person details
                for (PersonForm personForm : personForms) {
                    VerifiedPerson person = personForm.toVerifiedPerson(verificationId);
                    verificationDAO.saveVerifiedPerson(person);
                }

                updateStatus("Verification completed successfully!");
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Photo ID verification completed successfully!\n\n" +
                    "Total persons verified: " + totalPersonsComboBox.getValue() + "\n" +
                    "Verification ID: " + verificationId);
                
                // Close dialog
                Stage stage = (Stage) captureMainGuestBtn.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save verification to database.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save verification: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetAll() {
        // Reset main guest photo
        mainGuestImageView.setImage(null);
        mainGuestPhotoPath = null;
        
        // Reset notes
        if (notesTextArea != null) {
            notesTextArea.clear();
        }
        
        // Restart with default person count
        totalPersonsComboBox.setValue(1);
        
        updateStatus("All fields reset");
        showAlert(Alert.AlertType.INFORMATION, "Reset Complete", "All verification fields have been reset.");
    }

    private boolean validateForm() {
        // Validate main guest photo
        if (mainGuestPhotoPath == null) {
            return false;
        }

        // Validate all person forms
        for (PersonForm personForm : personForms) {
            if (!personForm.isValid()) {
                return false;
            }
        }

        return true;
    }

    private void loadExistingVerification() {
        try {
            if (bookingId != null) {
                var existingVerification = verificationDAO.findByBookingId(bookingId);
                if (existingVerification.isPresent()) {
                    populateForm(existingVerification.get());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading existing verification: " + e.getMessage());
        }
    }

    private void populateForm(PhotoIdVerification verification) {
        if (verification == null) return;
        
        if (totalPersonsComboBox != null) {
            totalPersonsComboBox.setValue(verification.getTotalPersons());
        }
        if (notesTextArea != null) {
            notesTextArea.setText(verification.getNotes());
        }
        if (statusLabel != null) {
            statusLabel.setText("Status: " + verification.getStatus());
        }

        if (verification.getMainGuestPhotoPath() != null) {
            mainGuestPhotoPath = verification.getMainGuestPhotoPath();
            try {
                File file = new File(mainGuestPhotoPath);
                if (file.exists()) {
                    mainGuestImageView.setImage(WebcamCapture.loadImage(file));
                }
            } catch (Exception e) {
                System.err.println("Error loading main guest photo: " + e.getMessage());
            }
        }

        // Populate person forms
        if (verification.getPersons() != null) {
            updatePersonForms();
            for (int i = 0; i < verification.getPersons().size() && i < personForms.size(); i++) {
                personForms.get(i).populate(verification.getPersons().get(i));
            }
        }
    }

    private void updateStatus(String message) {
        System.out.println("Status: " + message);
        if (statusLabel != null) {
            statusLabel.setText("Status: " + message);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for person form
    private class PersonForm extends VBox {
        private int personIndex;
        private TextField nameField;
        private ComboBox<String> idTypeComboBox;
        private TextField idNumberField;
        private TextField comingFromField;
        private TextField proceedingToField;
        private TextField contactField;
        private TextField relationshipField;
        private Button capturePhotoBtn;
        private ImageView photoImageView;
        private String photoPath;

        public PersonForm(int index) {
            this.personIndex = index;
            createForm();
        }

        private void createForm() {
            setSpacing(15);
            setStyle("-fx-padding: 20; -fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-color: #fafafa;");

            // Header with person number and type
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            
            Label headerLabel = new Label("Person " + personIndex + (personIndex == 1 ? " (MAIN GUEST)" : " (ADDITIONAL)"));
            headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label requiredLabel = new Label("All fields required");
            requiredLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            headerBox.getChildren().addAll(headerLabel, spacer, requiredLabel);
            getChildren().add(headerBox);

            // Main content grid
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(12);

            // Column constraints for better layout
            ColumnConstraints labelCol = new ColumnConstraints();
            labelCol.setPrefWidth(120);
            ColumnConstraints fieldCol = new ColumnConstraints();
            fieldCol.setPrefWidth(200);
            ColumnConstraints photoCol = new ColumnConstraints();
            photoCol.setPrefWidth(150);
            grid.getColumnConstraints().addAll(labelCol, fieldCol, photoCol);

            // Row 1: Name and Photo
            Label nameLabel = new Label("Full Name:");
            nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            nameField = new TextField();
            nameField.setPromptText("Enter full name");
            nameField.setStyle("-fx-pref-width: 200; -fx-padding: 8;");
            
            // Photo section
            VBox photoSection = new VBox(8);
            photoSection.setAlignment(Pos.TOP_CENTER);
            
            capturePhotoBtn = new Button("📸 CAPTURE ID");
            capturePhotoBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand;");
            capturePhotoBtn.setOnAction(e -> handleCapturePersonPhoto());
            
            photoImageView = new ImageView();
            photoImageView.setFitWidth(100);
            photoImageView.setFitHeight(75);
            photoImageView.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 2; -fx-border-radius: 8;");
            
            photoSection.getChildren().addAll(capturePhotoBtn, photoImageView);
            
            grid.add(nameLabel, 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(photoSection, 2, 0, 1, 4); // Span 4 rows

            // Row 2: ID Type and Number
            Label idTypeLabel = new Label("ID Type:");
            idTypeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            idTypeComboBox = new ComboBox<>();
            idTypeComboBox.getItems().addAll("Aadhaar Card", "Passport", "Driver License", "Voter ID", "PAN Card", "Other Govt ID");
            idTypeComboBox.setPromptText("Select ID type");
            idTypeComboBox.setStyle("-fx-pref-width: 200;");

            Label idNumberLabel = new Label("ID Number:");
            idNumberLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            idNumberField = new TextField();
            idNumberField.setPromptText("Enter ID number");
            idNumberField.setStyle("-fx-pref-width: 200; -fx-padding: 8;");

            grid.add(idTypeLabel, 0, 1);
            grid.add(idTypeComboBox, 1, 1);
            grid.add(idNumberLabel, 0, 2);
            grid.add(idNumberField, 1, 2);

            // Row 3: Travel Information
            Label comingFromLabel = new Label("Coming From:");
            comingFromLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            comingFromField = new TextField();
            comingFromField.setPromptText("City, State");
            comingFromField.setStyle("-fx-pref-width: 200; -fx-padding: 8;");

            Label proceedingToLabel = new Label("Proceeding To:");
            proceedingToLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            proceedingToField = new TextField();
            proceedingToField.setPromptText("City, State");
            proceedingToField.setStyle("-fx-pref-width: 200; -fx-padding: 8;");

            grid.add(comingFromLabel, 0, 3);
            grid.add(comingFromField, 1, 3);
            grid.add(proceedingToLabel, 0, 4);
            grid.add(proceedingToField, 1, 4);

            // Row 4: Contact and Relationship
            Label contactLabel = new Label("Contact Number:");
            contactLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            contactField = new TextField();
            contactField.setPromptText("Phone number");
            contactField.setStyle("-fx-pref-width: 200; -fx-padding: 8;");

            grid.add(contactLabel, 0, 5);
            grid.add(contactField, 1, 5);

            // Relationship (for additional persons)
            if (personIndex > 1) {
                Label relationshipLabel = new Label("Relationship:");
                relationshipLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
                relationshipField = new TextField();
                relationshipField.setPromptText("e.g., Spouse, Child, Friend");
                relationshipField.setStyle("-fx-pref-width: 200; -fx-padding: 8;");

                grid.add(relationshipLabel, 0, 6);
                grid.add(relationshipField, 1, 6);
            }

            getChildren().add(grid);
        }

        private void handleCapturePersonPhoto() {
            try {
                Stage stage = (Stage) capturePhotoBtn.getScene().getWindow();
                Image capturedImage = SimpleCameraCapture.captureFromCamera(stage);
                
                if (capturedImage != null) {
                    photoImageView.setImage(capturedImage);
                    
                    // Save to temporary file
                    File tempFile = File.createTempFile("person_photo_" + personIndex + "_", ".png");
                    if (WebcamCapture.saveImage(capturedImage, tempFile)) {
                        photoPath = tempFile.getAbsolutePath();
                        System.out.println("Person " + personIndex + " photo saved to: " + photoPath);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Capture Error", "Failed to capture photo: " + e.getMessage());
            }
        }

        public boolean isValid() {
            boolean nameValid = nameField.getText() != null && !nameField.getText().trim().isEmpty();
            boolean idTypeValid = idTypeComboBox.getValue() != null;
            boolean idNumberValid = idNumberField.getText() != null && !idNumberField.getText().trim().isEmpty();
            boolean photoValid = photoPath != null;
            
            return nameValid && idTypeValid && idNumberValid && photoValid;
        }

        public VerifiedPerson toVerifiedPerson(Long verificationId) {
            VerifiedPerson person = new VerifiedPerson(verificationId, 
                nameField.getText() != null ? nameField.getText().trim() : "", 
                personIndex);
            person.setIdType(idTypeComboBox.getValue());
            person.setIdNumber(idNumberField.getText() != null ? idNumberField.getText().trim() : "");
            person.setIdPhotoPath(photoPath);
            person.setComingFrom(comingFromField.getText());
            person.setProceedingTo(proceedingToField.getText());
            person.setContactNumber(contactField.getText());
            if (personIndex > 1 && relationshipField != null) {
                person.setRelationshipWithGuest(relationshipField.getText());
            }
            return person;
        }

        public void populate(VerifiedPerson person) {
            if (person == null) return;
            
            if (nameField != null) {
                nameField.setText(person.getPersonName());
            }
            if (idTypeComboBox != null) {
                idTypeComboBox.setValue(person.getIdType());
            }
            if (idNumberField != null) {
                idNumberField.setText(person.getIdNumber());
            }
            if (comingFromField != null) {
                comingFromField.setText(person.getComingFrom());
            }
            if (proceedingToField != null) {
                proceedingToField.setText(person.getProceedingTo());
            }
            if (contactField != null) {
                contactField.setText(person.getContactNumber());
            }
            if (personIndex > 1 && relationshipField != null) {
                relationshipField.setText(person.getRelationshipWithGuest());
            }
            if (person.getIdPhotoPath() != null) {
                photoPath = person.getIdPhotoPath();
                try {
                    File file = new File(photoPath);
                    if (file.exists() && photoImageView != null) {
                        Image image = WebcamCapture.loadImage(file);
                        photoImageView.setImage(image);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading person photo: " + e.getMessage());
                }
            }
        }

        // Helper method to show alerts
        private void showAlert(Alert.AlertType type, String title, String message) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}