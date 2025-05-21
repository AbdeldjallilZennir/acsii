package com.mycreche.controllers;

import com.mycreche.models.User;
import com.mycreche.utils.Database;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        // Disable login button if fields are empty
        loginButton.setDisable(true);

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateInput());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateInput());

        // Allow pressing Enter to trigger login
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

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        errorLabel.setText("");

        try (Connection conn = Database.getConnection()) {
            String query = "SELECT id, username, password, role FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                System.out.println(rs.getString("password"));
                System.out.println(rs.getString("username"));

                if (BCrypt.checkpw(password, storedHash)) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        "", // Password not needed after auth
                        rs.getString("role")
                    );
                    showDashboard(user.getRole());
                } else {
                    errorLabel.setText("Invalid username or password");
                    passwordField.clear();
                }
            } else {
                errorLabel.setText("Invalid username or password");
            }

        } catch (Exception e) {
            errorLabel.setText("Login failed. Please try again.");
            e.printStackTrace();
        }
    }

    private void showDashboard(String role) {
        errorLabel.setText("Access granted for role: " + role);
        System.out.println("✅ Access granted for role: " + role);
        // TODO: Load dashboard based on role
    }
}