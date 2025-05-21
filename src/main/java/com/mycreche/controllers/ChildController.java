package com.mycreche.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;

import com.mycreche.models.Child;
import com.mycreche.utils.Database;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ChildController {
    // Same FXML fields as in the form
    @FXML private TextField fullNameField, dietField, primaryContactNameField, phoneField, emailField, emergencyContactField;
    @FXML private DatePicker birthDatePicker, enrollmentDatePicker;
    @FXML private TextArea allergiesArea, medicalNotesArea, addressArea, pickupArea, instructionsArea;
    @FXML private RadioButton maleRadio, femaleRadio;
    @FXML private ToggleGroup genderGroup;
    @FXML private ComboBox<String> groupComboBox, relationshipComboBox;
    @FXML private CheckBox photoPermissionCheck;
    @FXML private Label statusLabel;
    @FXML private Button modifyButton;
    @FXML private Button saveButton;

    private Child currentChild;
    private int childId; 



    public void setChild(Child child) {
        childId = child.getId();
        this.currentChild = child;
        fullNameField.setText(child.getFullName());
        birthDatePicker.setValue(child.getBirthDate());
        enrollmentDatePicker.setValue(child.getEnrollmentDate());
        groupComboBox.setValue(child.getGroupName());
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
    setReadOnly(true); // Disable inputs by default
    relationshipComboBox.getItems().addAll("Mother", "Father", "Guardian", "Other");
}

@FXML
private void handleModify() {
    setReadOnly(false); // Enable all fields for editing
    modifyButton.setDisable(true); // Disable modify button to prevent re-click
    saveButton.setVisible(true);
    saveButton.setManaged(true); // Make it appear and take layout space
}
@FXML
private void handleSave() {
    // Add your validation and saving logic here.
    System.out.println("Saving changes...");
    String sql = """
        UPDATE children SET
            full_name = ?, birth_date = ?, gender = ?, enrollment_date = ?, group_name = ?,
            allergies = ?, medical_notes = ?, diet_restrictions = ?, primary_contact_name = ?,
            relationship = ?, phone_number = ?, email = ?, address = ?, emergency_contact = ?,
            authorized_pickup = ?, special_instructions = ?, photo_permission = ?
        WHERE id = ?
    """;

    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, fullNameField.getText());
        stmt.setDate(2, Date.valueOf(birthDatePicker.getValue()));
        stmt.setString(3, getSelectedGender());
        stmt.setDate(4, Date.valueOf(enrollmentDatePicker.getValue()));
        stmt.setString(5, groupComboBox.getValue());
        stmt.setString(6, allergiesArea.getText());
        stmt.setString(7, medicalNotesArea.getText());
        stmt.setString(8, dietField.getText());
        stmt.setString(9, primaryContactNameField.getText());
        stmt.setString(10, relationshipComboBox.getValue());
        stmt.setString(11, phoneField.getText());
        stmt.setString(12, emailField.getText());
        stmt.setString(13, addressArea.getText());
        stmt.setString(14, emergencyContactField.getText());
        stmt.setString(15, pickupArea.getText());
        stmt.setString(16, instructionsArea.getText());
        stmt.setBoolean(17, photoPermissionCheck.isSelected());
        stmt.setInt(18, childId);

        int updated = stmt.executeUpdate();

        if (updated > 0) {
            statusLabel.setText("Changes saved successfully.");
        } else {
            statusLabel.setText("No changes were made.");
        }

        setFormEditable(false);
        saveButton.setDisable(true);
        modifyButton.setDisable(false);

    } catch (SQLException e) {
        e.printStackTrace();
        statusLabel.setText("Error saving child data.");
    }



    // After saving, disable inputs again
    setReadOnly(true);
    modifyButton.setDisable(false); // Allow re-modification later
    saveButton.setVisible(false);
    saveButton.setManaged(false); // Hide the save button again
}


private void setReadOnly(boolean readOnly) {
        // TextFields
        fullNameField.setEditable(!readOnly);
        dietField.setEditable(!readOnly);
        phoneField.setEditable(!readOnly);
        emailField.setEditable(!readOnly);
        primaryContactNameField.setEditable(!readOnly);
        emergencyContactField.setEditable(!readOnly);

        // TextAreas
        allergiesArea.setEditable(!readOnly);
        medicalNotesArea.setEditable(!readOnly);
        addressArea.setEditable(!readOnly);
        pickupArea.setEditable(!readOnly);
        instructionsArea.setEditable(!readOnly);

        // DatePickers
        birthDatePicker.setDisable(readOnly);
        enrollmentDatePicker.setDisable(readOnly);

        // ComboBoxes
        groupComboBox.setDisable(readOnly);
        relationshipComboBox.setDisable(readOnly);

        // RadioButtons
        maleRadio.setDisable(readOnly);
        femaleRadio.setDisable(readOnly);

        // CheckBox
        photoPermissionCheck.setDisable(readOnly);
    }
    private void setFormEditable(boolean editable) {
    fullNameField.setEditable(editable);
    birthDatePicker.setDisable(!editable);
    enrollmentDatePicker.setDisable(!editable);
    groupComboBox.setDisable(!editable);
    allergiesArea.setEditable(editable);
    medicalNotesArea.setEditable(editable);
    dietField.setEditable(editable);
    primaryContactNameField.setEditable(editable);
    relationshipComboBox.setDisable(!editable);
    phoneField.setEditable(editable);
    emailField.setEditable(editable);
    addressArea.setEditable(editable);
    emergencyContactField.setEditable(editable);
    pickupArea.setEditable(editable);
    instructionsArea.setEditable(editable);
    photoPermissionCheck.setDisable(!editable);
    maleRadio.setDisable(!editable);
    femaleRadio.setDisable(!editable);
}

private String getSelectedGender() {
    if (maleRadio.isSelected()) return "Male";
    if (femaleRadio.isSelected()) return "Female";
    return null;
}

public void addChild(boolean isAdmin) {
    String sql = """
        INSERT INTO children (
            full_name, birth_date, gender, enrollment_date, group_name,
            allergies, medical_notes, diet_restrictions, primary_contact_name,
            relationship, phone_number, email, address, emergency_contact,
            authorized_pickup, special_instructions, photo_permission, status
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, fullNameField.getText());
        stmt.setDate(2, Date.valueOf(birthDatePicker.getValue()));
        stmt.setString(3, getSelectedGender());
        stmt.setDate(4, Date.valueOf(enrollmentDatePicker.getValue()));
        stmt.setString(5, groupComboBox.getValue());
        stmt.setString(6, allergiesArea.getText());
        stmt.setString(7, medicalNotesArea.getText());
        stmt.setString(8, dietField.getText());
        stmt.setString(9, primaryContactNameField.getText());
        stmt.setString(10, relationshipComboBox.getValue());
        stmt.setString(11, phoneField.getText());
        stmt.setString(12, emailField.getText());
        stmt.setString(13, addressArea.getText());
        stmt.setString(14, emergencyContactField.getText());
        stmt.setString(15, pickupArea.getText());
        stmt.setString(16, instructionsArea.getText());
        stmt.setBoolean(17, photoPermissionCheck.isSelected());
        stmt.setString(18, isAdmin ? "accepted" : "pending");

        stmt.executeUpdate();

        statusLabel.setText(isAdmin ? "Child added successfully." : "Child request submitted. Awaiting approval.");
    } catch (SQLException e) {
        e.printStackTrace();
        statusLabel.setText("Error adding child.");
    }
}

public void prepareForNewAddChild(Boolean isAdmin) {
    // Enable all form fields
    setFormEditable(true);

    // Hide modify/save buttons
    modifyButton.setVisible(false);
    modifyButton.setManaged(false);
    saveButton.setVisible(false);
    saveButton.setManaged(false);

    // Show an 'Add Child' button instead
    Button addButton = new Button("Add Child");
    addButton.setText("Add");
    addButton.setPrefWidth(150);
    addButton.setPrefHeight(40);
    addButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");

    addButton.setOnAction(e -> addChild(isAdmin));

    // Add button at bottom of form (e.g. using a VBox if your FXML supports it)
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


}
