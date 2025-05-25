package com.mycreche.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import javafx.scene.Parent;

import com.mycreche.utils.Database;

public class AdminController {

    @FXML
    private void handleCreateAccounts() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add_account.fxml"));
        Parent root = loader.load(); // ✅ Must be called before getController()

        AddAccountController controller = loader.getController();
        controller.setConnection(Database.getConnection());

        Stage stage = new Stage();
        stage.setTitle("Create New Account");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    } catch (IOException | SQLException e) {
        e.printStackTrace();
    }
}

    @FXML
    private void handleAddChild() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child_list.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Children List");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
private void handleCreateGroup() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/group_create.fxml"));
        Parent root = loader.load(); // Load before getting controller

        // Optional: if your GroupCreateController needs a DB connection or other setup:
        GroupCreateController controller = loader.getController();
        controller.setConnection(Database.getConnection());

        Stage stage = new Stage();
        stage.setTitle("Create New Group");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        // After closing, you may refresh data in the admin dashboard if needed
    } catch (IOException | SQLException e) {
        e.printStackTrace();
    }
}
}