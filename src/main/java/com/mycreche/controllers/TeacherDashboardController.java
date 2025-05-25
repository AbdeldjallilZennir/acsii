package com.mycreche.controllers;

import com.mycreche.models.*;
import com.mycreche.utils.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.LocalTimeStringConverter;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import java.io.IOException;

public class TeacherDashboardController implements Initializable,DashboardReceiver {

    @FXML private Label welcomeLabel;
    @FXML private ComboBox<Group> groupSelector;
    @FXML private DatePicker attendanceDatePicker;
    @FXML private TableView<ChildAttendanceRow> childTable;
    @FXML private TableColumn<ChildAttendanceRow, String> nameCol;
    @FXML private TableColumn<ChildAttendanceRow, String> ageCol;
    @FXML private TableColumn<ChildAttendanceRow, String> parentCol;
    @FXML private TableColumn<ChildAttendanceRow, LocalTime> arrivalCol;
    @FXML private TableColumn<ChildAttendanceRow, LocalTime> departureCol;
    @FXML private Label statusLabel;

    private User loggedInUser;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private int currentTeacherId;

    private String email;

    @Override
    public void setUserData(int id, String email) {
        this.currentTeacherId = id;
        this.email = email;

        initializeColumns();
        loadGroups();
        setupContextMenu(); // Setup context menu after initialization
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the date picker with today's date
        attendanceDatePicker.setValue(LocalDate.now());
        
        // Set up date picker listener to reload data when date changes
        attendanceDatePicker.setOnAction(e -> {
            if (groupSelector.getSelectionModel().getSelectedItem() != null) {
                handleGroupSelection();
            }
            updateStatusLabel();
        });
        
        // Initial status update
        updateStatusLabel();
    }


    private void initializeColumns() {
        // Basic columns
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        ageCol.setCellValueFactory(data -> data.getValue().ageProperty());
        parentCol.setCellValueFactory(data -> data.getValue().parentProperty());
        
        // Arrival time column with HH:MM formatting and editing capability
        arrivalCol.setCellValueFactory(data -> data.getValue().arrivalTimeProperty());
        arrivalCol.setCellFactory(col -> {
            TextFieldTableCell<ChildAttendanceRow, LocalTime> cell = 
                new TextFieldTableCell<>(new SafeLocalTimeStringConverter());
            
            // Only allow editing for today's attendance
            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (cell.getTableRow() != null && cell.getTableRow().getItem() != null) {
                    ChildAttendanceRow row = (ChildAttendanceRow) cell.getTableRow().getItem();
                    cell.setEditable(row.isEditable() && isToday());
                }
            });
            
            return cell;
        });
        
        arrivalCol.setOnEditCommit(event -> {
            ChildAttendanceRow row = event.getRowValue();
            LocalTime newTime = event.getNewValue();
            
            // Validate the time
            if (isValidTime(newTime, row.getDepartureTime())) {
                row.arrivalTimeProperty().set(newTime);
                // Auto-save when time is changed
                saveIndividualAttendance(row);
            } else {
                showAlert("Invalid Time", "Arrival time must be before departure time.");
                childTable.refresh(); // Reset the display
            }
        });
        
        // Departure time column with HH:MM formatting
        departureCol.setCellValueFactory(data -> data.getValue().departureTimeProperty());
        departureCol.setCellFactory(col -> {
            TextFieldTableCell<ChildAttendanceRow, LocalTime> cell = 
                new TextFieldTableCell<>(new SafeLocalTimeStringConverter());
            
            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (cell.getTableRow() != null && cell.getTableRow().getItem() != null) {
                    ChildAttendanceRow row = (ChildAttendanceRow) cell.getTableRow().getItem();
                    cell.setEditable(row.isEditable() && isToday());
                }
            });
            
            return cell;
        });
        
        departureCol.setOnEditCommit(event -> {
            ChildAttendanceRow row = event.getRowValue();
            LocalTime newTime = event.getNewValue();
            
            // Validate the time
            if (isValidTime(row.getArrivalTime(), newTime)) {
                row.departureTimeProperty().set(newTime);
                // Auto-save when time is changed
                saveIndividualAttendance(row);
            } else {
                showAlert("Invalid Time", "Departure time must be after arrival time.");
                childTable.refresh(); // Reset the display
            }
        });
        
        childTable.setEditable(true);
        
        // Make rows clickable - this replaces the separate makeRowsClickable method
        makeRowsClickable();
    }

    private void makeRowsClickable() {
        childTable.setRowFactory(tv -> {
            TableRow<ChildAttendanceRow> row = new TableRow<>();
            
            // Double-click to open report
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openChildReport(row.getItem());
                }
            });
            
            // Add hover effect
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #e3f2fd; -fx-cursor: hand;");
                }
            });
            
            row.setOnMouseExited(event -> {
                row.setStyle("");
            });
            
            return row;
        });
    }

    private boolean isToday() {
        return attendanceDatePicker.getValue().equals(LocalDate.now());
    }

    private boolean isValidTime(LocalTime arrival, LocalTime departure) {
        if (arrival == null || departure == null) {
            return true; // Allow null values
        }
        return arrival.isBefore(departure);
    }

    private void loadGroups() {
        ObservableList<Group> groups = FXCollections.observableArrayList();
        String sql = "SELECT * FROM `group` WHERE teacher_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1,currentTeacherId );
            System.out.print("tttt:"+currentTeacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Group group = new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("teacher_id"));
                groups.add(group);
            }

            groupSelector.setItems(groups);
            
            // Set up the display converter for the ComboBox
            groupSelector.setConverter(new javafx.util.StringConverter<Group>() {
                @Override
                public String toString(Group group) {
                    return group != null ? group.getName() : "";
                }

                @Override
                public Group fromString(String string) {
                    return null;
                }
            });
            
            groupSelector.setOnAction(e -> handleGroupSelection());

            // Auto-select first group if available
            if (!groups.isEmpty()) {
                groupSelector.getSelectionModel().selectFirst();
                handleGroupSelection();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load groups: " + e.getMessage());
        }
    }

    @FXML
    private void handleGroupSelection() {
        Group selectedGroup = groupSelector.getSelectionModel().getSelectedItem();
        if (selectedGroup != null) {
            loadChildren(selectedGroup.getId());
        }
    }

    private void loadChildren(int groupId) {
        ObservableList<ChildAttendanceRow> children = FXCollections.observableArrayList();
        LocalDate selectedDate = attendanceDatePicker.getValue();
        boolean editable = selectedDate.equals(LocalDate.now());

        // Updated query to match new schema
        String sql = """
            SELECT c.id, c.full_name, c.birth_date, c.primary_contact_name, 
                   a.arrival_time, a.departure_time
            FROM children c
            LEFT JOIN attendance a ON c.id = a.child_id AND a.date = ?
            WHERE c.group_id = ?
            ORDER BY c.full_name
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(selectedDate));
            stmt.setInt(2, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalTime arrival = null;
                LocalTime departure = null;
                
                // Safely parse times from TIME columns
                Time arrivalTime = rs.getTime("arrival_time");
                Time departureTime = rs.getTime("departure_time");
                
                if (arrivalTime != null) {
                    arrival = arrivalTime.toLocalTime();
                }
                if (departureTime != null) {
                    departure = departureTime.toLocalTime();
                }
                
                ChildAttendanceRow row = new ChildAttendanceRow(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getDate("birth_date").toLocalDate(),
                    rs.getString("primary_contact_name"),
                    arrival,
                    departure
                );
                row.setEditable(editable);
                children.add(row);
            }

            childTable.setItems(children);
            updateStatusLabel();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load children: " + e.getMessage());
        }
    }

    // Save individual attendance record (called when cell is edited)
    private void saveIndividualAttendance(ChildAttendanceRow row) {
        LocalDate date = attendanceDatePicker.getValue();
        
        if (!date.equals(LocalDate.now())) {
            return; // Only save today's attendance
        }

        String sql = """
            INSERT INTO attendance (child_id, date, arrival_time, departure_time, present) 
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                arrival_time = VALUES(arrival_time),
                departure_time = VALUES(departure_time),
                present = VALUES(present),
                updated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, row.getChildId());
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, row.getArrivalTime() != null ? Time.valueOf(row.getArrivalTime()) : null);
            stmt.setTime(4, row.getDepartureTime() != null ? Time.valueOf(row.getDepartureTime()) : null);
            
            // Child is present if they have arrival time
            boolean present = row.getArrivalTime() != null;
            stmt.setBoolean(5, present);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save attendance: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveAttendance() {
        LocalDate date = attendanceDatePicker.getValue();
        if (!date.equals(LocalDate.now())) {
            showAlert("Warning", "You can only edit attendance for today.");
            return;
        }

        if (childTable.getItems().isEmpty()) {
            showAlert("Warning", "No children to save attendance for.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Use transaction
            
            String sql = """
                INSERT INTO attendance (child_id, date, arrival_time, departure_time, present) 
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE 
                    arrival_time = VALUES(arrival_time),
                    departure_time = VALUES(departure_time),
                    present = VALUES(present),
                    updated_at = CURRENT_TIMESTAMP
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (ChildAttendanceRow row : childTable.getItems()) {
                    stmt.setInt(1, row.getChildId());
                    stmt.setDate(2, Date.valueOf(date));
                    stmt.setTime(3, row.getArrivalTime() != null ? Time.valueOf(row.getArrivalTime()) : null);
                    stmt.setTime(4, row.getDepartureTime() != null ? Time.valueOf(row.getDepartureTime()) : null);
                    
                    // Child is present if they have arrival time
                    boolean present = row.getArrivalTime() != null;
                    stmt.setBoolean(5, present);
                    
                    stmt.executeUpdate();
                }
                
                conn.commit();
                showAlert("Success", "Attendance saved successfully.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save attendance: " + e.getMessage());
        }
    }

    private void updateStatusLabel() {
        LocalDate selectedDate = attendanceDatePicker.getValue();
        String dateText = selectedDate.equals(LocalDate.now()) ? "Today" : selectedDate.toString();
        if (statusLabel != null) {
            statusLabel.setText("Attendance for: " + dateText);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : 
                               title.equals("Warning") ? Alert.AlertType.WARNING : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to open the child report (consolidated from duplicates)
    private void openChildReport(ChildAttendanceRow childData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child_report.fxml"));
            Parent root = loader.load();
            
            // Get the controller and initialize with child data
            ChildReportController controller = loader.getController();
            controller.initializeWithChild(childData, attendanceDatePicker.getValue());
            
            // Create new stage for the report
            Stage reportStage = new Stage();
            reportStage.setTitle("Daily Report - " + childData.getName());
            reportStage.setScene(new Scene(root));
            reportStage.setMaximized(true); // Make it fullscreen for better UX
            
            // Show the report window
            reportStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open child report: " + e.getMessage());
        }
    }

    // Setup context menu for right-click options
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem openReportItem = new MenuItem("📝 Open Daily Report");
        openReportItem.setOnAction(e -> {
            ChildAttendanceRow selected = childTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openChildReport(selected);
            }
        });
        
        MenuItem markPresentItem = new MenuItem("✅ Mark Present Now");
        markPresentItem.setOnAction(e -> {
            ChildAttendanceRow selected = childTable.getSelectionModel().getSelectedItem();
            if (selected != null && isToday()) {
                selected.arrivalTimeProperty().set(LocalTime.now());
                saveIndividualAttendance(selected);
                showAlert("Success", selected.getName() + " marked as present.");
            }
        });
        
        MenuItem markDepartedItem = new MenuItem("🚪 Mark Departed Now");
        markDepartedItem.setOnAction(e -> {
            ChildAttendanceRow selected = childTable.getSelectionModel().getSelectedItem();
            if (selected != null && isToday() && selected.getArrivalTime() != null) {
                selected.departureTimeProperty().set(LocalTime.now());
                saveIndividualAttendance(selected);
                showAlert("Success", selected.getName() + " marked as departed.");
            }
        });
        
        contextMenu.getItems().addAll(openReportItem, markPresentItem, markDepartedItem);
        childTable.setContextMenu(contextMenu);
    }

    // Enhanced SafeLocalTimeStringConverter that ensures HH:MM format display and handles various input formats
    private class SafeLocalTimeStringConverter extends LocalTimeStringConverter {
        public SafeLocalTimeStringConverter() {
            super(timeFormatter, timeFormatter);
        }

        @Override
        public LocalTime fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            
            try {
                // Clean the input and try to parse
                String cleanValue = value.trim();
                
                // Handle various input formats
                if (cleanValue.matches("\\d{1,2}:\\d{2}")) {
                    return LocalTime.parse(cleanValue, timeFormatter);
                } else if (cleanValue.matches("\\d{1,2}\\.\\d{2}")) {
                    // Handle dot separator (8.30 -> 8:30)
                    cleanValue = cleanValue.replace(".", ":");
                    return LocalTime.parse(cleanValue, timeFormatter);
                } else if (cleanValue.matches("\\d{3,4}")) {
                    // Handle HHMM format (830 -> 8:30, 1430 -> 14:30)
                    if (cleanValue.length() == 3) {
                        cleanValue = "0" + cleanValue.charAt(0) + ":" + cleanValue.substring(1);
                    } else {
                        cleanValue = cleanValue.substring(0, 2) + ":" + cleanValue.substring(2);
                    }
                    return LocalTime.parse(cleanValue, timeFormatter);
                }
                
                return super.fromString(cleanValue);
            } catch (DateTimeParseException e) {
                showAlert("Invalid Time Format", "Please enter time in HH:MM format (e.g., 08:30)");
                return null;
            }
        }

        @Override
        public String toString(LocalTime value) {
            // Always display in HH:MM format, even for null values
            return value == null ? "" : value.format(timeFormatter);
        }
    }
}