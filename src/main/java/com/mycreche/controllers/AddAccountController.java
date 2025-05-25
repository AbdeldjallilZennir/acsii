package com.mycreche.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import com.mycreche.utils.Database;
import org.mindrot.jbcrypt.BCrypt;

public class AddAccountController {

    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField firstNameField, lastNameField, emailField, phoneField, addressField, emergencyNameField, emergencyPhoneField;
    @FXML private PasswordField passwordField;
    @FXML private VBox commonFields, parentFields;
    @FXML private Button submitButton;
    @FXML private Label messageLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;


    private Connection conn;

    public void setConnection(Connection connection) {
        this.conn = connection;
    }


    public void initialize() {
        roleComboBox.getItems().addAll("educator", "parent", "cuisinier");
        roleComboBox.setOnAction(e -> updateFieldVisibility());

        // Phone auto-formatting
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> phoneField.setText(formatPhone(newVal)));
        emergencyPhoneField.textProperty().addListener((obs, oldVal, newVal) -> emergencyPhoneField.setText(formatPhone(newVal)));
    }

    private void updateFieldVisibility() {
        String selectedRole = roleComboBox.getValue();
        boolean isParent = "parent".equals(selectedRole);
        parentFields.setVisible(isParent);
        parentFields.setManaged(isParent);
    }

    @FXML
    private void handleCreateAccount() {
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
        String role = roleComboBox.getValue();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();

        if (role == null || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all required fields.");
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            messageLabel.setText("Email must end with @gmail.com");
            return;
        }

        if (!isValidPhone(phone)) {
            messageLabel.setText("Phone must be in the format xx-xx-xx-xx-xx");
            return;
        }
        // Email validation
    if (!email.endsWith("@gmail.com") || email.length() <= "@gmail.com".length()) {
        emailErrorLabel.setText("Email must end with @gmail.com and contain characters before it");
        emailErrorLabel.setVisible(true);
        emailErrorLabel.setManaged(true);
        return;
    }

    // Password validation: must contain at least one digit and one letter
    if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
        passwordErrorLabel.setVisible(true);
        passwordErrorLabel.setManaged(true);
        return;
    }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try {
            if (emailExistsInRoles(email)) {
                messageLabel.setText("Email already exists in roles.");
                return;
            }

            switch (role) {
                case "educator":
                    insertTeacher(firstName, lastName, email, phone, hashedPassword);
                    break;
                case "parent":
                    String address = addressField.getText().trim();
                    String eName = emergencyNameField.getText().trim();
                    String ePhone = emergencyPhoneField.getText().trim();

                    if (address.isEmpty() || eName.isEmpty() || ePhone.isEmpty()) {
                        messageLabel.setText("Please fill in all parent-specific fields.");
                        return;
                    }

                    if (!isValidPhone(ePhone)) {
                        messageLabel.setText("Emergency phone must be in format xx-xx-xx-xx-xx");
                        return;
                    }

                    insertParent(firstName, lastName, hashedPassword, email, phone, address, eName, ePhone);
                    break;
                case "cuisinier":
                    insertCuisinier(email, firstName, lastName, hashedPassword, phone);
                    break;
                default:
                    messageLabel.setText("Unknown role.");
                    return;
            }

            insertIntoRolesTable(email, role);
            messageLabel.setText("Account created successfully!");
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
        }
    }

    private boolean emailExistsInRoles(String email) throws SQLException {
        String query = "SELECT 1 FROM roles WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private void insertTeacher(String firstName, String lastName, String email, String phone, String password) throws SQLException {
        String sql = "INSERT INTO teacher (first_name, last_name, email, phone_number, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, password);
            stmt.executeUpdate();
        }
    }

    private void insertParent(String firstName, String lastName, String password, String email, String phone, String address, String eName, String ePhone) throws SQLException {
        String sql = "INSERT INTO parents (first_name, last_name, password, email, phone, address, emergency_contact_name, emergency_contact_phone, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, password);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setString(6, address);
            stmt.setString(7, eName);
            stmt.setString(8, ePhone);
            stmt.setString(9, now);
            stmt.setString(10, now);
            stmt.executeUpdate();
        }
    }

    private void insertCuisinier(String email, String firstName, String lastName, String password, String phone) throws SQLException {
        String sql = "INSERT INTO cuisinier (email, first_name, last_name, password, phone, creation_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, password);
            stmt.setString(5, phone);
            stmt.setString(6, LocalDateTime.now().toString());
            stmt.executeUpdate();
        }
    }

    private void insertIntoRolesTable(String email, String role) throws SQLException {
        String sql = "INSERT INTO roles (email, role) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, role);
            stmt.executeUpdate();
        }
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        addressField.clear();
        emergencyNameField.clear();
        emergencyPhoneField.clear();
    }

    private boolean isValidPhone(String phone) {
        return Pattern.matches("^\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}$", phone);
    }

    private String formatPhone(String raw) {
        String digits = raw.replaceAll("\\D", "");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < digits.length() && i < 10; i++) {
            if (i > 0 && i % 2 == 0) formatted.append("-");
            formatted.append(digits.charAt(i));
        }

        return formatted.toString();
    }
}