package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.User;
import dao.UserDAO;

import java.util.List;

public class SuperAdminController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> credentialsConfiguredColumn;
    @FXML private Button deleteUserButton;
    @FXML private Button logoutButton;
    @FXML private Label totalUsersValueLabel;
    @FXML private Label adminsValueLabel;
    @FXML private Label configuredValueLabel;

    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTable();
        loadUsers();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        credentialsConfiguredColumn.setCellValueFactory(new PropertyValueFactory<>("hasCredentials"));
        // Custom cell factory for nicer display
        credentialsConfiguredColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Configured" : "Not Configured");
                }
            }
        });

        // Listen for selection changes to enable/disable delete button
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            deleteUserButton.setDisable(newSel == null);
        });
    }

    private void loadUsers() {
        List<User> allUsers = userDAO.getAllUsersWithCredentialStatus();
        userList.setAll(allUsers);
        usersTable.setItems(userList);
        refreshStats();
        deleteUserButton.setDisable(true);
    }

    private void refreshStats() {
        int totalUsers = userList.size();
        long admins = userList.stream()
                .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("admin"))
                .count();
        long configured = userList.stream()
                .filter(User::isHasCredentials)
                .count();

        totalUsersValueLabel.setText(String.valueOf(totalUsers));
        adminsValueLabel.setText(String.valueOf(admins));
        configuredValueLabel.setText(String.valueOf(configured));
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete user: " + selectedUser.getUsername() + "?", 
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Delete User");
        
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                boolean success = userDAO.deleteUser(selectedUser.getUserId());
                if (success) {
                    userList.remove(selectedUser);
                    refreshStats();
                    Alert info = new Alert(Alert.AlertType.INFORMATION, "User successfully deleted.");
                    info.show();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete user.");
                    err.show();
                }
            }
        });
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 620);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("AWS Governance Tool - Login");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
