package com.mycreche.controllers;

import com.mycreche.models.Teacher;
import com.mycreche.utils.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupCreateController {

    @FXML private TextField groupNameField;
    @FXML private ComboBox<Teacher> teacherComboBox;

    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        loadTeachers();
    }

    private void loadTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, first_name,last_name FROM teacher")) {

            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setId(rs.getInt("id"));
                teacher.setFirstName(rs.getString("first_name"));
                teacher.setLastName(rs.getString("last_name"));
                teachers.add(teacher);
            }

            teacherComboBox.setItems(FXCollections.observableArrayList(teachers));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load teachers: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleSave() {
        String groupName = groupNameField.getText().trim();
        Teacher selectedTeacher = teacherComboBox.getSelectionModel().getSelectedItem();

        if (groupName.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Group name cannot be empty").show();
            return;
        }

        if (selectedTeacher == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a teacher").show();
            return;
        }

        try {
            String sql = "INSERT INTO `group` (name, teacher_id) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, selectedTeacher.getId());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                new Alert(Alert.AlertType.INFORMATION, "Group created successfully!").show();
                Stage stage = (Stage) groupNameField.getScene().getWindow();
                stage.close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to create group.").show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage()).show();
        }
    }
}