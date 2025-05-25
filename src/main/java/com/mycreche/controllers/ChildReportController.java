package com.mycreche.controllers;

import com.mycreche.models.*;
import com.mycreche.utils.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ChildReportController implements Initializable, DashboardReceiver {

    // Header Labels
    @FXML private Label titleLabel;
    @FXML private Label childInfoLabel;
    @FXML private Label childNameLabel;
    @FXML private Label childAgeLabel;
    @FXML private Label guardianNameLabel;
    @FXML private Label arrivalTimeLabel;
    @FXML private Label departureTimeLabel;

    // Meals Section
    @FXML private ComboBox<String> breakfastCombo;
    @FXML private ComboBox<String> lunchCombo;
    @FXML private ComboBox<String> snackCombo;

    // Sleep Section
    @FXML private ComboBox<String> sleepCombo;
    @FXML private TextField sleepDurationField;

    // Diaper/Bathroom Section
    @FXML private Spinner<Integer> diaperSpinner;
    @FXML private CheckBox pottyTrainingCheck;

    // Mood Section
    @FXML private ToggleGroup moodGroup;
    @FXML private RadioButton happyRadio;
    @FXML private RadioButton contentRadio;
    @FXML private RadioButton fussyRadio;
    @FXML private RadioButton crankyRadio;
    @FXML private RadioButton tiredRadio;

    // Activities Section
    @FXML private CheckBox artCheck;
    @FXML private CheckBox musicCheck;
    @FXML private CheckBox readingCheck;
    @FXML private CheckBox outdoorCheck;
    @FXML private CheckBox sensoryCheck;
    @FXML private CheckBox freePlayCheck;
    @FXML private CheckBox groupCheck;
    @FXML private TextArea learningArea;

    // Notes Section
    @FXML private TextArea generalNotesArea;
    @FXML private TextArea remindersArea;

    // Health Section
    @FXML private CheckBox medicationCheck;
    @FXML private CheckBox incidentCheck;
    @FXML private CheckBox allHealthyCheck;
    @FXML private TextArea healthNotesArea;

    // Buttons
    @FXML private Button saveReportBtn;
    @FXML private Button sendToGuardianBtn;
    @FXML private Button backBtn;


    // Data
    private ChildAttendanceRow childData;
    private LocalDate reportDate;
    private int reportId = -1; // -1 means new report
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


    private int currentTeacherId;

    private String email;

    @Override
    public void setUserData(int id, String email) {
        this.currentTeacherId = id;
        this.email = email;
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize spinner factory
        diaperSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        
        // Set default selections
        breakfastCombo.getItems().addAll("Not Provided", "Ate Well", "Ate Some", "Didn't Eat");
        lunchCombo.getItems().addAll("Not Provided", "Ate Well", "Ate Some", "Didn't Eat"); 
        snackCombo.getItems().addAll("Not Provided", "Ate Well", "Ate Some", "Didn't Eat");
        sleepCombo.getItems().addAll("No Nap", "Slept Well", "Light Sleep", "Restless", "Refused to Sleep");
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
        diaperSpinner.setValueFactory(valueFactory);
        happyRadio.setSelected(true); // Default mood
        allHealthyCheck.setSelected(true); // Default health status
    }

    public void initializeWithChild(ChildAttendanceRow child, LocalDate date) {
        this.childData = child;
        this.reportDate = date;
        
        // Set child information
        childNameLabel.setText(child.getName());
        childAgeLabel.setText(child.getAge());
        guardianNameLabel.setText(child.getParent());
        
        // Format and display times
        String arrivalText = child.getArrivalTime() != null ? 
            child.getArrivalTime().format(timeFormatter) : "Not recorded";
        String departureText = child.getDepartureTime() != null ? 
            child.getDepartureTime().format(timeFormatter) : "Not recorded";
            
        arrivalTimeLabel.setText(arrivalText);
        departureTimeLabel.setText(departureText);
        
        // Update header info
        childInfoLabel.setText(String.format("Child: %s | Date: %s", 
            child.getName(), date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        
        // Load existing report if available
        loadExistingReport();
    }

    private void loadExistingReport() {
        String sql = """
            SELECT * FROM daily_reports 
            WHERE child_id = ? AND report_date = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, childData.getChildId());
            stmt.setDate(2, Date.valueOf(reportDate));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                reportId = rs.getInt("id");
                
                // Load meal data
                breakfastCombo.getSelectionModel().select(rs.getString("breakfast_status"));
                lunchCombo.getSelectionModel().select(rs.getString("lunch_status"));
                snackCombo.getSelectionModel().select(rs.getString("snack_status"));
                
                // Load sleep data
                sleepCombo.getSelectionModel().select(rs.getString("sleep_quality"));
                sleepDurationField.setText(rs.getString("sleep_duration"));
                
                // Load diaper/bathroom data
                diaperSpinner.getValueFactory().setValue(rs.getInt("diaper_changes"));
                pottyTrainingCheck.setSelected(rs.getBoolean("used_potty"));
                
                // Load mood data
                String mood = rs.getString("mood");
                switch (mood) {
                    case "Happy" -> happyRadio.setSelected(true);
                    case "Content" -> contentRadio.setSelected(true);
                    case "Fussy" -> fussyRadio.setSelected(true);
                    case "Cranky" -> crankyRadio.setSelected(true);
                    case "Tired" -> tiredRadio.setSelected(true);
                }
                
                // Load activities
                artCheck.setSelected(rs.getBoolean("activity_art"));
                musicCheck.setSelected(rs.getBoolean("activity_music"));
                readingCheck.setSelected(rs.getBoolean("activity_reading"));
                outdoorCheck.setSelected(rs.getBoolean("activity_outdoor"));
                sensoryCheck.setSelected(rs.getBoolean("activity_sensory"));
                freePlayCheck.setSelected(rs.getBoolean("activity_free_play"));
                groupCheck.setSelected(rs.getBoolean("activity_group"));
                
                // Load text areas
                learningArea.setText(rs.getString("learning_highlights"));
                generalNotesArea.setText(rs.getString("general_notes"));
                remindersArea.setText(rs.getString("reminders"));
                
                // Load health data
                medicationCheck.setSelected(rs.getBoolean("medication_given"));
                incidentCheck.setSelected(rs.getBoolean("incident_occurred"));
                allHealthyCheck.setSelected(rs.getBoolean("all_healthy"));
                healthNotesArea.setText(rs.getString("health_notes"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load existing report: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveReport() {
        if (reportId == -1) {
            insertNewReport();
        } else {
            updateExistingReport();
        }
    }

    private void insertNewReport() {
        String sql = """
            INSERT INTO daily_reports (
                child_id, report_date, breakfast_status, lunch_status, snack_status,
                sleep_quality, sleep_duration, diaper_changes, used_potty, mood,
                activity_art, activity_music, activity_reading, activity_outdoor,
                activity_sensory, activity_free_play, activity_group,
                learning_highlights, general_notes, reminders,
                medication_given, incident_occurred, all_healthy, health_notes,
                created_by, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setReportParameters(stmt);
            stmt.setInt(25, getCurrentUserId()); // Assuming you have a method to get current user
            
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reportId = generatedKeys.getInt(1);
                }
                showAlert("Success", "Report saved successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save report: " + e.getMessage());
        }
    }

    private void updateExistingReport() {
        String sql = """
            UPDATE daily_reports SET
                breakfast_status = ?, lunch_status = ?, snack_status = ?,
                sleep_quality = ?, sleep_duration = ?, diaper_changes = ?, used_potty = ?, mood = ?,
                activity_art = ?, activity_music = ?, activity_reading = ?, activity_outdoor = ?,
                activity_sensory = ?, activity_free_play = ?, activity_group = ?,
                learning_highlights = ?, general_notes = ?, reminders = ?,
                medication_given = ?, incident_occurred = ?, all_healthy = ?, health_notes = ?,
                updated_at = NOW()
            WHERE id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setReportParameters(stmt);
            stmt.setInt(23, reportId);
            
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                showAlert("Success", "Report updated successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update report: " + e.getMessage());
        }
    }

    private void setReportParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, childData.getChildId());
        stmt.setDate(2, Date.valueOf(reportDate));
        stmt.setString(3, breakfastCombo.getSelectionModel().getSelectedItem());
        stmt.setString(4, lunchCombo.getSelectionModel().getSelectedItem());
        stmt.setString(5, snackCombo.getSelectionModel().getSelectedItem());
        stmt.setString(6, sleepCombo.getSelectionModel().getSelectedItem());
        stmt.setString(7, sleepDurationField.getText());
        stmt.setInt(8, diaperSpinner.getValue());
        stmt.setBoolean(9, pottyTrainingCheck.isSelected());
        
        // Get selected mood
        String selectedMood = "Happy"; // Default
        if (contentRadio.isSelected()) selectedMood = "Content";
        else if (fussyRadio.isSelected()) selectedMood = "Fussy";
        else if (crankyRadio.isSelected()) selectedMood = "Cranky";
        else if (tiredRadio.isSelected()) selectedMood = "Tired";
        stmt.setString(10, selectedMood);
        
        // Activities
        stmt.setBoolean(11, artCheck.isSelected());
        stmt.setBoolean(12, musicCheck.isSelected());
        stmt.setBoolean(13, readingCheck.isSelected());
        stmt.setBoolean(14, outdoorCheck.isSelected());
        stmt.setBoolean(15, sensoryCheck.isSelected());
        stmt.setBoolean(16, freePlayCheck.isSelected());
        stmt.setBoolean(17, groupCheck.isSelected());
        
        // Text fields
        stmt.setString(18, learningArea.getText());
        stmt.setString(19, generalNotesArea.getText());
        stmt.setString(20, remindersArea.getText());
        
        // Health
        stmt.setBoolean(21, medicationCheck.isSelected());
        stmt.setBoolean(22, incidentCheck.isSelected());
        stmt.setBoolean(23, allHealthyCheck.isSelected());
        stmt.setString(24, healthNotesArea.getText());
    }

    @FXML
    private void handleSendToGuardian() {
        // First save the report
        handleSaveReport();
        
        if (reportId != -1) {
            // Mark report as sent and create notification for guardian
            markReportAsSent();
            showAlert("Success", "Report has been sent to the guardian!");
        }
    }

    private void markReportAsSent() {
        String sql = """
            UPDATE daily_reports SET sent_to_guardian = TRUE, sent_at = NOW() 
            WHERE id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reportId);
            stmt.executeUpdate();
            
            // You could also insert a notification record here for the guardian

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            // Load the teacher dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher_dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Teacher Dashboard");
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private int getCurrentUserId() {
        return currentTeacherId; 
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}