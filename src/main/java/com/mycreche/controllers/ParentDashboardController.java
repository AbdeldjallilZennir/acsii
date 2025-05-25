package com.mycreche.controllers;

import com.mycreche.models.*;
import com.mycreche.utils.Database;

import javafx.scene.Parent; // Add this line
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.fxml.Initializable;
import java.net.URL;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

public class ParentDashboardController implements Initializable , DashboardReceiver {
    private List<Child> children = new ArrayList<>();
    private List<DailyReport> allReports = new ArrayList<>();

    @FXML private ComboBox<Child> childComboBox;
    @FXML private TextArea childInfoArea;
    @FXML private TableView<DailyReport> reportTable;
    @FXML private TableColumn<DailyReport, String> colDate, colMood, colMeals, colSleep, colActivities, colSeen;


    private int currentParentId;

    private String email;

    @Override
    public void setUserData(int id, String email) {
        this.currentParentId = id;
        this.email = email;

        loadChildrenFromDatabase();
    }



    @FXML
public void handleAddChild() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child_form.fxml"));
        Scene scene = new Scene(loader.load());

        // Load controller
        ChildController controller = loader.getController();
        controller.setUserEmail(email);
        controller.prepareForNewAddChild(false);

        // Load all groups from DB
        List<Group> allGroups = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `group`")) {

            while (rs.next()) {
                Group group = new Group(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("teacher_id")
                );
                allGroups.add(group);
            }
        }

        // Pass groups to controller
        controller.setAvailableGroups(allGroups);

        // Show form
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Add New Child");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

@FXML
private void onChildSelected() {
    Child selectedChild = childComboBox.getSelectionModel().getSelectedItem();
    if (selectedChild == null) return;

    // Show child info
    StringBuilder info = new StringBuilder();
    info.append("Name: ").append(selectedChild.getFullName()).append("\n");
    info.append("Birth Date: ").append(selectedChild.getBirthDate()).append("\n");
    info.append("Group: ").append(selectedChild.getGroupName()).append("\n");
    info.append("Allergies: ").append(selectedChild.getAllergies()).append("\n");

    childInfoArea.setText(info.toString());

    loadReports(selectedChild.getId());

    // Filter and display reports for the selected child
    List<DailyReport> childReports = allReports.stream()
        .filter(r -> r.getChildId() == selectedChild.getId())
        .toList();

    reportTable.setItems(FXCollections.observableArrayList(childReports));
}

@Override
public void initialize(URL url, ResourceBundle rb) {
    colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReportDate().toString()));
    colMood.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMood()));
    colMeals.setCellValueFactory(data -> new SimpleStringProperty(
        "B:" + data.getValue().getBreakfastStatus() + 
        ", L:" + data.getValue().getLunchStatus() + 
        ", S:" + data.getValue().getSnackStatus()));
    colSleep.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getSleepQuality() + " (" + data.getValue().getSleepDuration() + "h)"));
    colActivities.setCellValueFactory(data -> new SimpleStringProperty(
        (data.getValue().isActivitiesOutdoor() ? "Outdoor " : "") +
        (data.getValue().isActivitiesReading() ? "Reading " : "") +
        (data.getValue().isActivitiesMusic() ? "Music" : "")));
    colSeen.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isSeen() ? "Yes" : "No"));


    // Optional: Set listener for ComboBox
    childComboBox.setOnAction(e -> onChildSelected());
}


@FXML
private void onViewReport() {
    Child selectedChild = childComboBox.getSelectionModel().getSelectedItem();
    if (selectedChild == null) {
        new Alert(Alert.AlertType.WARNING, "Please select a child first.").show();
        return;
    }

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/report_list.fxml"));
        Parent root = loader.load();

        ReportListController controller = loader.getController();
        controller.setReports(allReports.stream()
            .filter(r -> r.getChildId() == selectedChild.getId())
            .toList());

        Stage stage = new Stage();
        stage.setTitle("Daily Reports for " + selectedChild.getFullName());
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void loadChildrenFromDatabase() {
    children.clear();

    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM children WHERE parentid = ?")) {

        stmt.setInt(1, currentParentId);  // Use the class-level variable set via setParentId()

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Child child = new Child();
                child.setId(rs.getInt("id"));
                child.setFullName(rs.getString("full_name"));
                child.setBirthDate(rs.getDate("birth_date").toLocalDate());
                child.setAllergies(rs.getString("allergies"));
                child.setGroupId(rs.getInt("group_id"));

                children.add(child);
            }
        }

        childComboBox.setItems(FXCollections.observableArrayList(children));
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


private void loadReports(int child_id) {
    allReports.clear();

    String sql = "SELECT * " +
                 "FROM daily_reports dr " +
                 "JOIN attendance a ON dr.child_id = a.child_id AND dr.report_date = a.date " +
                 "WHERE dr.child_id = ?";

    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, child_id);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            DailyReport report = new DailyReport(
                rs.getInt("child_id"),
                rs.getDate("report_date").toLocalDate(),
                rs.getBoolean("seen")
            );

            report.setId(rs.getInt("id"));
            report.setArrivalTime(rs.getTime("arrival_time") != null ? rs.getTime("arrival_time").toLocalTime() : null);
            report.setDepartureTime(rs.getTime("departure_time") != null ? rs.getTime("departure_time").toLocalTime() : null);

            report.setBreakfastStatus(rs.getString("breakfast_status"));
            report.setLunchStatus(rs.getString("lunch_status"));
            report.setSnackStatus(rs.getString("snack_status"));

            report.setSleepQuality(rs.getString("sleep_quality"));
            report.setSleepDuration(rs.getString("sleep_duration"));

            report.setDiaperChanges(rs.getInt("diaper_changes"));
            report.setPottyTrainingUsed(rs.getBoolean("used_potty"));

            report.setMood(rs.getString("mood"));

            report.setActivitiesArt(rs.getBoolean("activity_art"));
            report.setActivitiesMusic(rs.getBoolean("activity_music"));
            report.setActivitiesReading(rs.getBoolean("activity_reading"));
            report.setActivitiesOutdoor(rs.getBoolean("activity_outdoor"));
            report.setActivitiesSensory(rs.getBoolean("activity_sensory"));
            report.setActivitiesFreePlay(rs.getBoolean("activity_free_play"));
            report.setActivitiesGroup(rs.getBoolean("activity_group"));

            report.setLearningHighlights(rs.getString("learning_highlights"));
            report.setGeneralNotes(rs.getString("general_notes"));
            report.setRemindersRequests(rs.getString("reminders")); // ✅ changed from reminders_requests to reminders

            report.setMedicationGiven(rs.getBoolean("medication_given"));
            report.setIncidentOccurred(rs.getBoolean("incident_occurred"));
            report.setNoHealthConcerns(rs.getBoolean("all_healthy")); // ✅ changed from no_health_concerns to all_healthy
            report.setHealthNotes(rs.getString("health_notes"));

            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());


            allReports.add(report);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}



    
}