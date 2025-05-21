package com.mycreche.controllers;

import com.mycreche.models.Child;
import com.mycreche.utils.Database;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class ChildListController {
    @FXML private TableView<Child> childTable;
    @FXML private TableColumn<Child, String> nameCol;
    @FXML private TableColumn<Child, LocalDate> birthCol;
    @FXML private TableColumn<Child, String> genderCol;
    @FXML private TableColumn<Child, String> groupCol;
    @FXML private TableColumn<Child, String> contactCol;
    @FXML private TableColumn<Child, String> phoneCol;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;

    private ObservableList<Child> allChildren = FXCollections.observableArrayList();
    private FilteredList<Child> filteredChildren;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        birthCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getBirthDate()));
        genderCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGender()));
        groupCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        contactCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrimaryContactName()));
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhoneNumber()));

        loadChildrenFromDB();
        setupSearch();
        updateTotal();
    }

    private void setupSearch() {
        filteredChildren = new FilteredList<>(allChildren, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredChildren.setPredicate(child -> {
                if (newVal == null || newVal.isBlank()) return true;
                return child.getFullName().toLowerCase().contains(newVal.toLowerCase());
            });
            childTable.setItems(filteredChildren);
            updateTotal();
        });
        childTable.setItems(filteredChildren);
    }

    private void updateTotal() {
        totalLabel.setText("Total: " + filteredChildren.size() + " children");
    }

    private void loadChildrenFromDB() {
        String sql = "SELECT * FROM children";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
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

                allChildren.add(child);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleViewDetails() {
        Child selected = childTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child_form.fxml"));
            Scene scene = new Scene(loader.load());

            ChildController controller = loader.getController();
            controller.setChild(selected);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Child Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleEditChild() {
        // Can implement later (editable form view)
    }


    @FXML
public void handleAddChild() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child_form.fxml"));
        Scene scene = new Scene(loader.load());

        ChildController controller = loader.getController();
        controller.prepareForNewAddChild(true);  // This method should make the form fully editable and hide modify/save buttons

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Add New Child");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        // Optional: Refresh the list after adding
        allChildren.clear();
        loadChildrenFromDB();
        updateTotal();

    } catch (Exception e) {
        e.printStackTrace();
    }
    }




    @FXML
    public void handleDeleteChild() {
        Child selected = childTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getFullName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM children WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, selected.getId());
                    stmt.executeUpdate();

                    allChildren.remove(selected);
                    updateTotal();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
@FXML
private void handlePendingRequests() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pending_requests.fxml"));
        Parent root = loader.load();

        PendingRequestsController requests = loader.getController();
        

        Stage stage = new Stage();
        stage.setTitle("Pending Requests");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


}