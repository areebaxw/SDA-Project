package services;

import models.Alert;
import dao.AlertDAO;
import java.util.ArrayList;
import java.util.List;

/**
 * AlertService - Observer Pattern implementation for alert management
 * Singleton service that manages alerts and notifies observers
 */
public class AlertService {
    private static AlertService instance;
    private final AlertDAO alertDAO;
    private final List<AlertObserver> observers;
    
    /**
     * Private constructor for Singleton pattern
     */
    private AlertService() {
        this.alertDAO = new AlertDAO();
        this.observers = new ArrayList<>();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized AlertService getInstance() {
        if (instance == null) {
            instance = new AlertService();
        }
        return instance;
    }
    
    /**
     * Register an observer (Observer Pattern)
     */
    public void registerObserver(AlertObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer registered: " + observer.getClass().getSimpleName());
        }
    }
    
    /**
     * Unregister an observer
     */
    public void unregisterObserver(AlertObserver observer) {
        observers.remove(observer);
        System.out.println("Observer unregistered: " + observer.getClass().getSimpleName());
    }
    
    /**
     * Create an alert and notify observers
     */
    public boolean createAlert(Alert alert) {
        boolean success = alertDAO.createAlert(alert);
        
        if (success) {
            System.out.println("Alert created: " + alert.getMessage());
            notifyObserversOfCreation(alert);
        }
        
        return success;
    }
    
    /**
     * Resolve an alert and notify observers
     */
    public boolean resolveAlert(int alertId) {
        boolean success = alertDAO.resolveAlert(alertId);
        
        if (success) {
            System.out.println("Alert resolved: " + alertId);
            // Get the alert to pass to observers
            List<Alert> alerts = alertDAO.getAllAlerts();
            for (Alert alert : alerts) {
                if (alert.getAlertId() == alertId) {
                    notifyObserversOfResolution(alert);
                    break;
                }
            }
        }
        
        return success;
    }
    
    /**
     * Get all unresolved alerts
     */
    public List<Alert> getUnresolvedAlerts() {
        return alertDAO.getUnresolvedAlerts();
    }
    
    /**
     * Get all alerts
     */
    public List<Alert> getAllAlerts() {
        return alertDAO.getAllAlerts();
    }
    
    /**
     * Get alerts by resource type
     */
    public List<Alert> getAlertsByResourceType(String resourceType) {
        return alertDAO.getAlertsByResourceType(resourceType);
    }
    
    /**
     * Get total alert count
     */
    public int getTotalAlertCount(boolean unresolvedOnly) {
        return alertDAO.getTotalAlertCount(unresolvedOnly);
    }
    
    /**
     * Delete alert
     */
    public boolean deleteAlert(int alertId) {
        return alertDAO.deleteAlert(alertId);
    }
    
    /**
     * Notify all observers of alert creation
     */
    private void notifyObserversOfCreation(Alert alert) {
        for (AlertObserver observer : observers) {
            try {
                observer.onAlertCreated(alert);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify all observers of alert resolution
     */
    private void notifyObserversOfResolution(Alert alert) {
        for (AlertObserver observer : observers) {
            try {
                observer.onAlertResolved(alert);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get alert statistics by severity
     */
    public int getAlertCountBySeverity(String severity, boolean unresolvedOnly) {
        return alertDAO.getAlertCountBySeverity(severity, unresolvedOnly);
    }
}
