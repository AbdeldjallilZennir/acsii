package com.mycreche.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import com.mycreche.models.Child;
import com.mycreche.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PendingRequestsController {

    @FXML
    private ListView<HBox> pendingListView;

    @FXML
    public void initialize() {
        loadPendingRequests();
    }


    
    private void loadPendingRequests() {
        pendingListView.getItems().clear();

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM waiting_list";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String fullName = rs.getString("full_name");
                String birthDate = rs.getString("birth_date");

                Label nameLabel = new Label(fullName + " (" + birthDate + ")");
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                nameLabel.setOnMouseClicked((MouseEvent e) -> openChildForm(id));

                Button acceptBtn = new Button("Accept");
                acceptBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                acceptBtn.setOnAction(e -> acceptChild(id));

                Button rejectBtn = new Button("Reject");
                rejectBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
                rejectBtn.setOnAction(e -> rejectChild(id));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox itemBox = new HBox(10, nameLabel, spacer, acceptBtn, rejectBtn);
                itemBox.setPadding(new Insets(10));
                itemBox.setStyle("-fx-background-color: white; -fx-border-color: #E4C590; -fx-border-radius: 10; -fx-background-radius: 10;");
                itemBox.setPrefHeight(50);

                pendingListView.getItems().add(itemBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openChildForm(int childId) {
        showChildDetails(childId);
        System.out.println("Open child form for ID: " + childId);
    }

    private void acceptChild(int childId) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE children SET status = ? WHERE id = ?");
            stmt.setString(1, "accepted");
            stmt.setInt(2, childId);
            stmt.executeUpdate();
            loadPendingRequests(); // Refresh list
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rejectChild(int childId) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM children WHERE id = ?");
            stmt.setInt(1, childId);
            stmt.executeUpdate();
            loadPendingRequests(); // Refresh list
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showChildDetails(int childId) {
    try (Connection conn = Database.getConnection()) {
        String query = "SELECT * FROM waiting_list WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, childId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Child child = new Child();
            child.setId(rs.getInt("id"));
            child.setFullName(rs.getString("full_name"));
            child.setBirthDate(rs.getDate("birth_date").toLocalDate());
            child.setGender(rs.getString("gender"));
            child.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());
            child.setGroupName(rs.getString("group_name"));
            child.setAllergies(rs.getString("allergies"));
            child.setMedicalNotes(rs.getString("medical_notes"));
            child.setDietRestrictions(rs.getString("diet_restrictions"));
            child.setPrimaryContactName(rs.getString("primary_contact_name"));
            child.setRelationship(rs.getString("relationship"));
            child.setPhoneNumber(rs.getString("phone_number"));
            child.setEmail(rs.getString("email"));
            child.setAddress(rs.getString("address"));
            child.setEmergencyContact(rs.getString("emergency_contact"));
            child.setAuthorizedPickup(rs.getString("authorized_pickup"));
            child.setSpecialInstructions(rs.getString("special_instructions"));
            child.setPhotoPermission(rs.getBoolean("photo_permission"));

            // Now open the form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child_form.fxml"));
            Scene scene = new Scene(loader.load());

            ChildController controller = loader.getController();
            controller.setChild(child);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Child Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
