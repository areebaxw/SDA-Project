package controllers;

import aws.ALBMonitoringService;
import aws.AWSClientFactory;
import dao.ALBDAO;
import dao.AWSCredentialDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import utils.SceneNavigator;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.ALBResource;
import models.AWSCredential;
import models.User;

import java.util.List;

/**
 * ALBController - Controller for ALB monitoring panel
 */
public class ALBController {
    @FXML private TableView<ALBResource> albTable;
    @FXML private TableColumn<ALBResource, String> nameColumn;
    @FXML private TableColumn<ALBResource, String> dnsColumn;
    @FXML private TableColumn<ALBResource, String> stateColumn;
    @FXML private TableColumn<ALBResource, String> schemeColumn;
    @FXML private TableColumn<ALBResource, Long> requestCountColumn;
    @FXML private TableColumn<ALBResource, Boolean> idleColumn;

    @FXML private Button backButton;

    private User currentUser;
    private final ALBDAO albDAO = new ALBDAO();
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();
    private ALBMonitoringService albMonitoringService;
    private final ObservableList<ALBResource> albData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTableColumns();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeAwsClientFromSavedCredentials();
        loadALBs();
    }

    private void initializeAwsClientFromSavedCredentials() {
        try {
            AWSCredential cred = awsCredentialDAO.getActiveCredentials(currentUser.getUserId());
            if (cred != null) {
                AWSClientFactory.getInstance().initializeCredentials(
                        cred.getAccessKey(),
                        cred.getSecretKey(),
                        cred.getRegion()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize AWS client from saved credentials: " + e.getMessage());
        }
    }

    private ALBMonitoringService requireService() {
        if (!AWSClientFactory.getInstance().isInitialized()) {
            showError("AWS credentials not configured. Please configure credentials first.");
            return null;
        }

        if (albMonitoringService == null) {
            try {
                albMonitoringService = new ALBMonitoringService();
            } catch (Exception e) {
                showError("Failed to initialize ALB service: " + e.getMessage());
                return null;
            }
        }
        return albMonitoringService;
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("loadBalancerName"));
        dnsColumn.setCellValueFactory(new PropertyValueFactory<>("dnsName"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        schemeColumn.setCellValueFactory(new PropertyValueFactory<>("scheme"));
        requestCountColumn.setCellValueFactory(new PropertyValueFactory<>("requestCount"));
        idleColumn.setCellValueFactory(new PropertyValueFactory<>("idle"));
        albTable.setItems(albData);
    }

    @FXML
    private void handleRefresh() {
        loadALBs();
    }

    @FXML
    private void handleSyncFromAWS() {
        if (currentUser == null) {
            showError("User not set");
            return;
        }

        ALBMonitoringService service = requireService();
        if (service == null) return;

        int synced = service.syncFromAWS(currentUser.getUserId());
        loadALBs();
        showInfo("Synced " + synced + " ALBs from AWS.");
    }

    @FXML
    private void handleDeleteALB() {
        ALBResource selected = albTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a load balancer.");
            return;
        }

        ALBMonitoringService service = requireService();
        if (service == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete ALB");
        confirmation.setContentText("Delete ALB '" + selected.getLoadBalancerName() + "'?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok = service.deleteALB(selected.getLoadBalancerArn());
            if (ok) {
                albDAO.deleteByArn(selected.getLoadBalancerArn());
                loadALBs();
                showInfo("ALB delete requested.");
            } else {
                showError("Failed to delete ALB.");
            }
        }
    }

    private void loadALBs() {
        List<ALBResource> albs = albDAO.getAll();
        albData.clear();
        albData.addAll(albs);
    }

    @FXML
    private void handleBackToDashboard() {
        SceneNavigator.navigateTo("/views/dashboard.fxml",
                "AWS Governance Dashboard - " + currentUser.getUsername(),
                (DashboardController ctrl) -> ctrl.setCurrentUser(currentUser));
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
