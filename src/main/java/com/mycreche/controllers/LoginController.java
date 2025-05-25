package com.mycreche.controllers;

import com.mycreche.utils.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        loginButton.setDisable(true);

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateInput());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateInput());

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin();
            }
        });
    }

    private void validateInput() {
        loginButton.setDisable(
            usernameField.getText().trim().isEmpty() || 
            passwordField.getText().trim().isEmpty()
        );
    }

    private ResultSet rs;

    @FXML
    public void handleLogin() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        errorLabel.setText("");

        try (Connection conn = Database.getConnection()) {

            // Step 1: Get role from roles table
            String roleQuery = "SELECT role FROM roles WHERE email = ?";
            PreparedStatement roleStmt = conn.prepareStatement(roleQuery);
            roleStmt.setString(1, email);
            ResultSet roleRs = roleStmt.executeQuery();

            if (!roleRs.next()) {
                errorLabel.setText("No account found for this email.");
                return;
            }

            String role = roleRs.getString("role");
            String userTable;

            switch (role.toLowerCase()) {
                case "cuisinier":
                    userTable = "cuisinier";
                    break;
                case "parent":
                    userTable = "parents";
                    break;
                case "educator":
                    userTable = "teacher";
                    break;
                default:
                    userTable = "users";
                    break;
            }

            // Step 2: Get user data from correct table
            String userQuery = "SELECT * FROM " + userTable + " WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, email);
            ResultSet userRs = userStmt.executeQuery();
            rs = userRs;

            if (!userRs.next()) {
                errorLabel.setText("Account not found in role-specific table.");
                return;
            }

            String hashedPassword = userRs.getString("password");

            if (!BCrypt.checkpw(password, hashedPassword)) {
                errorLabel.setText("Incorrect password.");
                passwordField.clear();
                return;
            }

            // Step 3: Success — redirect to role-specific dashboard

            showDashboard(role, email);

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("An error occurred. Please try again.");
        }
    }

    private int id;

    private void showDashboard(String role, String email) {
    try {
        FXMLLoader loader;
        switch (role) {
            case "parent":
                loader = new FXMLLoader(getClass().getResource("/views/parent_dashboard.fxml"));
                id = rs.getInt("parent_id");
                break;
            case "educator":
                loader = new FXMLLoader(getClass().getResource("/views/teacher_dashboard.fxml"));
                id = rs.getInt("id");
                break;
            case "cuisinier":
                loader = new FXMLLoader(getClass().getResource("/views/cuisinier_dashboard.fxml"));
                id = rs.getInt("id");
                
                break;
            default:
                loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
                break;
        }

        Scene scene = new Scene(loader.load());

        // Pass data to the controller
        Object controller = loader.getController();
        if (controller instanceof DashboardReceiver) {
            ((DashboardReceiver) controller).setUserData(id, email);
        }

        // Get current stage and set new scene
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(role.substring(0, 1).toUpperCase() + role.substring(1) + " Dashboard");
        stage.show();

    } catch (Exception e) {
        e.printStackTrace();
        errorLabel.setText("Failed to load dashboard.");
    }
}


}