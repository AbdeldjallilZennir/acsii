package com.mycreche.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.Date;

import com.mycreche.models.Child;
import com.mycreche.models.Group;
import com.mycreche.utils.Database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ChildController {
    @FXML private TextField fullNameField, dietField, primaryContactNameField, phoneField, emailField, emergencyContactField;
    @FXML private DatePicker birthDatePicker, enrollmentDatePicker;
    @FXML private TextArea allergiesArea, medicalNotesArea, addressArea, pickupArea, instructionsArea;
    @FXML private RadioButton maleRadio, femaleRadio;
    @FXML private ToggleGroup genderGroup;
    @FXML private ComboBox<Group> groupComboBox;
    @FXML private ComboBox<String> relationshipComboBox,emailComboBox;
    @FXML private CheckBox photoPermissionCheck;
    @FXML private Label statusLabel;
    @FXML private Button modifyButton;
    @FXML private Button saveButton;

    private Child currentChild;
    private int childId;


    public void setUserEmail(String email) {
        emailField.setText(email);
        emailField.setEditable(false); // Optional: lock the field
    }

    public void setChild(Child child) {
        childId = child.getId();
        this.currentChild = child;
        fullNameField.setText(child.getFullName());
        birthDatePicker.setValue(child.getBirthDate());
        enrollmentDatePicker.setValue(child.getEnrollmentDate());
        // Select correct group in the ComboBox by matching name
        for (Group group : groupComboBox.getItems()) {
            if (group.getName().equals(child.getGroupName())) {
                groupComboBox.setValue(group);
                break;
            }
        }
        
        allergiesArea.setText(child.getAllergies());
        medicalNotesArea.setText(child.getMedicalNotes());
        dietField.setText(child.getDietRestrictions());
        primaryContactNameField.setText(child.getPrimaryContactName());
        relationshipComboBox.setValue(child.getRelationship());
        phoneField.setText(child.getPhoneNumber());
        emailField.setText(child.getEmail());
        addressArea.setText(child.getAddress());
        emergencyContactField.setText(child.getEmergencyContact());
        pickupArea.setText(child.getAuthorizedPickup());
        instructionsArea.setText(child.getSpecialInstructions());
        photoPermissionCheck.setSelected(child.isPhotoPermission());
        
        switch (child.getGender()) {
            case "Male" -> genderGroup.selectToggle(maleRadio);
            case "Female" -> genderGroup.selectToggle(femaleRadio);
        }
    }

    @FXML
    public void initialize() {
        setReadOnly(true);
        relationshipComboBox.getItems().addAll("Mother", "Father", "Guardian", "Other");
        loadGroupsFromDatabase();
    }

    @FXML
    private void handleModify() {
        setReadOnly(false);
        modifyButton.setDisable(true);
        saveButton.setVisible(true);
        saveButton.setManaged(true);
    }

    @FXML
    private void handleSave() {
        System.out.println("Saving changes...");
        String sql = """
            UPDATE children SET
                full_name = ?, birth_date = ?, gender = ?, enrollment_date = ?, group_name = ?, group_id = ?,
                allergies = ?, medical_notes = ?, diet_restrictions = ?, primary_contact_name = ?,
                relationship = ?, phone_number = ?, email = ?, address = ?, emergency_contact = ?,
                authorized_pickup = ?, special_instructions = ?, photo_permission = ?
            WHERE id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Group selectedGroup = groupComboBox.getSelectionModel().getSelectedItem();

            stmt.setString(1, fullNameField.getText());
            stmt.setDate(2, Date.valueOf(birthDatePicker.getValue()));
            stmt.setString(3, getSelectedGender());

            stmt.setDate(4, Date.valueOf(enrollmentDatePicker.getValue()));
            stmt.setString(5, selectedGroup.getName());
            stmt.setInt(6, selectedGroup.getId());
            stmt.setString(7, allergiesArea.getText());
            stmt.setString(8, medicalNotesArea.getText());
            stmt.setString(9, dietField.getText());
            System.out.println("1");
            stmt.setString(10, primaryContactNameField.getText());
            stmt.setString(11, relationshipComboBox.getValue());
            System.out.println("3");
            stmt.setString(12, phoneField.getText());
            stmt.setString(13, emailField.getText());
            System.out.println("2");
            stmt.setString(14, addressArea.getText());
            stmt.setString(15, emergencyContactField.getText());
            stmt.setString(16, pickupArea.getText());

            stmt.setString(17, instructionsArea.getText());
            stmt.setBoolean(18, photoPermissionCheck.isSelected());

            stmt.setInt(19, childId);
            
            int updated = stmt.executeUpdate();
            statusLabel.setText(updated > 0 ? "Changes saved successfully." : "No changes were made.");
            System.out.println("3");
            setFormEditable(false);
            saveButton.setDisable(true);
            modifyButton.setDisable(false);

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error saving child data.");
        }

        setReadOnly(true);
        modifyButton.setDisable(false);
        saveButton.setVisible(false);
        saveButton.setManaged(false);
    }

    private void setReadOnly(boolean readOnly) {
        fullNameField.setEditable(!readOnly);
        dietField.setEditable(!readOnly);
        phoneField.setEditable(!readOnly);
        emailField.setEditable(!readOnly);
        primaryContactNameField.setEditable(!readOnly);
        emergencyContactField.setEditable(!readOnly);
        allergiesArea.setEditable(!readOnly);
        medicalNotesArea.setEditable(!readOnly);
        addressArea.setEditable(!readOnly);
        pickupArea.setEditable(!readOnly);
        instructionsArea.setEditable(!readOnly);
        birthDatePicker.setDisable(readOnly);
        enrollmentDatePicker.setDisable(readOnly);
        groupComboBox.setDisable(readOnly);
        relationshipComboBox.setDisable(readOnly);
        maleRadio.setDisable(readOnly);
        femaleRadio.setDisable(readOnly);
        photoPermissionCheck.setDisable(readOnly);
    }

    private void setFormEditable(boolean editable) {
        setReadOnly(!editable);
    }

    private String getSelectedGender() {
        if (maleRadio.isSelected()) return "Male";
        if (femaleRadio.isSelected()) return "Female";
        return null;
    }

    public void addChild(boolean isAdmin) {
        String sql = """
            INSERT INTO children (
                full_name, birth_date, gender, enrollment_date, group_name, group_id,
                allergies, medical_notes, diet_restrictions, primary_contact_name,
                relationship, phone_number, email, address, emergency_contact,
                authorized_pickup, special_instructions, photo_permission, status , parentid
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Group selectedGroup = groupComboBox.getSelectionModel().getSelectedItem();

            stmt.setString(1, fullNameField.getText());
            stmt.setDate(2, Date.valueOf(birthDatePicker.getValue()));
            stmt.setString(3, getSelectedGender());
            stmt.setDate(4, Date.valueOf(enrollmentDatePicker.getValue()));
            stmt.setString(5, selectedGroup.getName());
            stmt.setInt(6, selectedGroup.getId());
            stmt.setString(7, allergiesArea.getText());
            stmt.setString(8, medicalNotesArea.getText());
            stmt.setString(9, dietField.getText());
            stmt.setString(10, primaryContactNameField.getText());
            stmt.setString(11, relationshipComboBox.getValue());
            stmt.setString(12, phoneField.getText());
            stmt.setString(13, emailField.getText());
            stmt.setString(14, addressArea.getText());
            stmt.setString(15, emergencyContactField.getText());
            stmt.setString(16, pickupArea.getText());
            stmt.setString(17, instructionsArea.getText());
            stmt.setBoolean(18, photoPermissionCheck.isSelected());
            stmt.setString(19, isAdmin ? "accepted" : "pending");
            stmt.setInt(20, getParentIdByEmail(emailField.getText()));

            stmt.executeUpdate();
            statusLabel.setText(isAdmin ? "Child added successfully." : "Child request submitted. Awaiting approval.");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error adding child.");
        }
    }

    public void prepareForNewAddChild(Boolean isAdmin) {
        setFormEditable(true);
        modifyButton.setVisible(false);
        modifyButton.setManaged(false);
        saveButton.setVisible(false);
        saveButton.setManaged(false);

        Button addButton = new Button("Add Child");
        addButton.setText("Add");
        addButton.setPrefWidth(150);
        addButton.setPrefHeight(40);
        addButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");

        addButton.setOnAction(e -> addChild(isAdmin));

        fullNameField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                VBox bottom = (VBox) newScene.lookup("#bottom");
                if (bottom != null) {
                    bottom.getChildren().add(addButton);
                } else {
                    System.err.println("Element with fx:id=\"bottom\" not found in scene.");
                }
            }
        });
    }

    public void setAvailableGroups(List<Group> groups) {
        ObservableList<Group> groupList = FXCollections.observableArrayList(groups);
        groupComboBox.setItems(groupList);
    }

    private int getParentIdByEmail(String email) {
    String query = "SELECT parent_id FROM parents WHERE email = ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, email);
        var rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("parent_id");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1; // not found or error
}

private void loadGroupsFromDatabase() {
    ObservableList<Group> groupList = FXCollections.observableArrayList();
    String sql = "SELECT id, name,teacher_id FROM `group`";
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int teacher_id = rs.getInt("teacher_id");
            groupList.add(new Group(id, name,teacher_id));
        }

        groupComboBox.setItems(groupList);

    } catch (SQLException e) {
        e.printStackTrace();
        statusLabel.setText("Error loading groups.");
    }
}


} 