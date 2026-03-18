package services;

import models.Alert;

/**
 * AlertObserver - Observer Pattern Interface
 * Observers are notified when alerts are created
 */
public interface AlertObserver {
    /**
     * Called when an alert is created
     * @param alert The alert that was created
     */
    void onAlertCreated(Alert alert);
    
    /**
     * Called when an alert is resolved
     * @param alert The alert that was resolved
     */
    void onAlertResolved(Alert alert);
}
