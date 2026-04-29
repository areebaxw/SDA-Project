package controllers;

import aws.AWSClientFactory;
import aws.S3MonitoringService;
import dao.AWSCredentialDAO;
import dao.S3BucketDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import utils.SceneNavigator;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.AWSCredential;
import models.S3BucketResource;
import models.User;

import java.util.List;

/**
 * S3Controller - Controller for S3 bucket monitoring panel
 */
public class S3Controller {
    @FXML private TableView<S3BucketResource> s3Table;
    @FXML private TableColumn<S3BucketResource, String> bucketNameColumn;
    @FXML private TableColumn<S3BucketResource, String> regionColumn;
    @FXML private TableColumn<S3BucketResource, Long> objectCountColumn;
    @FXML private TableColumn<S3BucketResource, Double> sizeColumn;
    @FXML private TableColumn<S3BucketResource, Boolean> publicColumn;
    @FXML private TableColumn<S3BucketResource, Boolean> idleColumn;

    @FXML private Button backButton;

    private User currentUser;
    private final S3BucketDAO s3BucketDAO = new S3BucketDAO();
    private final AWSCredentialDAO awsCredentialDAO = new AWSCredentialDAO();
    private S3MonitoringService s3MonitoringService;
    private final ObservableList<S3BucketResource> s3Data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTableColumns();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeAwsClientFromSavedCredentials();
        loadS3Buckets();
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

    private S3MonitoringService requireService() {
        if (!AWSClientFactory.getInstance().isInitialized()) {
            showError("AWS credentials not configured. Please configure credentials first.");
            return null;
        }

        if (s3MonitoringService == null) {
            try {
                s3MonitoringService = new S3MonitoringService();
            } catch (Exception e) {
                showError("Failed to initialize S3 service: " + e.getMessage());
                return null;
            }
        }
        return s3MonitoringService;
    }

    private void setupTableColumns() {
        bucketNameColumn.setCellValueFactory(new PropertyValueFactory<>("bucketName"));
        regionColumn.setCellValueFactory(new PropertyValueFactory<>("region"));
        objectCountColumn.setCellValueFactory(new PropertyValueFactory<>("objectCount"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("totalSizeGb"));
        sizeColumn.setCellFactory(column -> new TableCell<S3BucketResource, Double>() {
            @Override
            protected void updateItem(Double sizeGb, boolean empty) {
                super.updateItem(sizeGb, empty);
                if (empty || sizeGb == null) {
                    setText(null);
                    return;
                }

                if (sizeGb < 1.0) {
                    double sizeMb = sizeGb * 1024.0;
                    setText(String.format("%.2f MB", sizeMb));
                } else {
                    setText(String.format("%.4f GB", sizeGb));
                }
            }
        });
        publicColumn.setCellValueFactory(new PropertyValueFactory<>("isPublic"));
        idleColumn.setCellValueFactory(new PropertyValueFactory<>("idle"));
        s3Table.setItems(s3Data);
    }

    @FXML
    private void handleRefresh() {
        loadS3Buckets();
    }

    @FXML
    private void handleSyncFromAWS() {
        if (currentUser == null) {
            showError("User not set");
            return;
        }

        S3MonitoringService service = requireService();
        if (service == null) return;

        int synced = service.syncFromAWS(currentUser.getUserId());
        loadS3Buckets();
        showInfo("Synced " + synced + " S3 buckets from AWS.");
    }

    @FXML
    private void handleDeleteBucket() {
        S3BucketResource selected = s3Table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a bucket.");
            return;
        }

        S3MonitoringService service = requireService();
        if (service == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete S3 Bucket");
        confirmation.setContentText("Delete bucket '" + selected.getBucketName() + "'? Bucket must be empty.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok = service.deleteBucket(selected.getBucketName());
            if (ok) {
                s3BucketDAO.deleteByBucketName(selected.getBucketName());
                loadS3Buckets();
                showInfo("Bucket deleted.");
            } else {
                showError("Failed to delete bucket. Ensure it is empty and you have permission.");
            }
        }
    }

    private void loadS3Buckets() {
        List<S3BucketResource> buckets = s3BucketDAO.getAll();
        s3Data.clear();
        s3Data.addAll(buckets);
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
