package services;

import models.Alert;

/**
 * ConsoleAlertObserver - Concrete Observer that prints alerts to console
 */
public class ConsoleAlertObserver implements AlertObserver {
    
    @Override
    public void onAlertCreated(Alert alert) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║               NEW ALERT NOTIFICATION                     ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║ Resource: " + alert.getResourceId());
        System.out.println("║ Type: " + alert.getResourceType());
        System.out.println("║ Alert Type: " + alert.getAlertType());
        System.out.println("║ Severity: " + alert.getSeverity().toUpperCase());
        System.out.println("║ Message: " + alert.getMessage());
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }
    
    @Override
    public void onAlertResolved(Alert alert) {
        System.out.println("✓ Alert RESOLVED: " + alert.getResourceId() + " - " + alert.getMessage());
    }
}
