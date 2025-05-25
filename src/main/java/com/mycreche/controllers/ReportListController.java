package com.mycreche.controllers;

import com.mycreche.models.Child;
import com.mycreche.models.DailyReport;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class ReportListController {

    @FXML private Label titleLabel;
    @FXML private TableView<DailyReport> reportTable;
    @FXML private TableColumn<DailyReport, String> colDate, colMood, colMeals, colSleep, colSeen;

    private Child currentChild;

    public void setChild(Child child) {
        this.currentChild = child;
        if (titleLabel != null && child != null) {
            titleLabel.setText("Daily Reports – " + child.getFullName());
        }
    }

    public void setReports(List<DailyReport> reports) {
        reportTable.setItems(FXCollections.observableArrayList(reports));
    }

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReportDate().toString()));
        colMood.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMood()));
        colMeals.setCellValueFactory(data -> new SimpleStringProperty(
            "B:" + data.getValue().getBreakfastStatus() + 
            ", L:" + data.getValue().getLunchStatus() + 
            ", S:" + data.getValue().getSnackStatus()));
        colSleep.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getSleepQuality() + " (" + data.getValue().getSleepDuration() + "h)"));
        colSeen.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isSeen() ? "Yes" : "No"));

        // Add row click listener for detailed view
        reportTable.setRowFactory(tv -> {
            TableRow<DailyReport> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    DailyReport report = row.getItem();
                    showReportDetails(report);
                }
            });
            return row;
        });
    }

    private void showReportDetails(DailyReport report) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Details");
        alert.setHeaderText("Details for " + report.getReportDate());

        StringBuilder content = new StringBuilder();
        content.append("Mood: ").append(report.getMood()).append("\n")
               .append("Meals:\n")
               .append("  • Breakfast: ").append(report.getBreakfastStatus()).append("\n")
               .append("  • Lunch: ").append(report.getLunchStatus()).append("\n")
               .append("  • Snack: ").append(report.getSnackStatus()).append("\n")
               .append("Sleep: ").append(report.getSleepQuality()).append(" (").append(report.getSleepDuration()).append("h)\n")
               .append("Diaper Changes: ").append(report.getDiaperChanges()).append("\n")
               .append("Used Potty: ").append(report.isPottyTrainingUsed() ? "Yes" : "No").append("\n\n")
               .append("Activities:\n")
               .append("  • Art: ").append(report.isActivitiesArt() ? "Yes" : "No").append("\n")
               .append("  • Music: ").append(report.isActivitiesMusic() ? "Yes" : "No").append("\n")
               .append("  • Reading: ").append(report.isActivitiesReading() ? "Yes" : "No").append("\n")
               .append("  • Outdoor: ").append(report.isActivitiesOutdoor() ? "Yes" : "No").append("\n")
               .append("  • Sensory: ").append(report.isActivitiesSensory() ? "Yes" : "No").append("\n")
               .append("  • Free Play: ").append(report.isActivitiesFreePlay() ? "Yes" : "No").append("\n")
               .append("  • Group Activity: ").append(report.isActivitiesGroup() ? "Yes" : "No").append("\n\n")
               .append("Learning Highlights: ").append(report.getLearningHighlights()).append("\n")
               .append("Notes: ").append(report.getGeneralNotes()).append("\n")
               .append("Reminders: ").append(report.getRemindersRequests()).append("\n\n")
               .append("Medication Given: ").append(report.isMedicationGiven() ? "Yes" : "No").append("\n")
               .append("Incident Occurred: ").append(report.isIncidentOccurred() ? "Yes" : "No").append("\n")
               .append("All Healthy: ").append(report.isNoHealthConcerns() ? "Yes" : "No").append("\n")
               .append("Health Notes: ").append(report.getHealthNotes());

        alert.setContentText(content.toString());
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
}