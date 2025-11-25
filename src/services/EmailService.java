package services;

import models.Alert;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * EmailService - Sends email notifications for alerts
 */
public class EmailService {
    private static EmailService instance;
    private final String fromEmail = "mahadmalik1090@gmail.com";
    private final String password = "mzuwobmonfpkvmdw"; // Gmail App Password (spaces removed)
    
    private EmailService() {}
    
    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }
    
    /**
     * Send alert notification email
     */
    public boolean sendAlertNotification(String toEmail, Alert alert) {
        try {
            // Set up mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });
            
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            
            // Set subject based on severity
            String subject = String.format("[%s] AWS Alert: %s - %s", 
                alert.getSeverity().toUpperCase(), 
                alert.getResourceType(), 
                alert.getAlertType());
            message.setSubject(subject);
            
            // Create email body
            String emailBody = buildEmailBody(alert);
            message.setContent(emailBody, "text/html; charset=utf-8");
            
            // Send email
            Transport.send(message);
            
            System.out.println("Alert email sent successfully to: " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("Failed to send alert email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Build HTML email body
     */
    private String buildEmailBody(Alert alert) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #ED7D27; color: white; padding: 20px; text-align: center; }");
        html.append(".content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }");
        html.append(".alert-info { margin: 10px 0; padding: 10px; background-color: white; border-left: 4px solid ");
        
        // Color based on severity
        switch (alert.getSeverity().toLowerCase()) {
            case "critical":
            case "high":
                html.append("#f44336");
                break;
            case "medium":
                html.append("#FF9800");
                break;
            default:
                html.append("#4CAF50");
        }
        
        html.append("; }");
        html.append(".label { font-weight: bold; color: #ED7D27; }");
        html.append(".footer { text-align: center; color: #777; font-size: 12px; margin-top: 20px; }");
        html.append("</style></head><body>");
        
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h2>🚨 AWS Governance Alert</h2>");
        html.append("</div>");
        
        html.append("<div class='content'>");
        html.append("<div class='alert-info'>");
        html.append("<p><span class='label'>Alert ID:</span> ").append(alert.getAlertId()).append("</p>");
        html.append("<p><span class='label'>Resource ID:</span> ").append(alert.getResourceId()).append("</p>");
        html.append("<p><span class='label'>Resource Type:</span> ").append(alert.getResourceType()).append("</p>");
        html.append("<p><span class='label'>Alert Type:</span> ").append(alert.getAlertType()).append("</p>");
        html.append("<p><span class='label'>Severity:</span> <span style='color: ");
        
        switch (alert.getSeverity().toLowerCase()) {
            case "critical":
            case "high":
                html.append("#f44336; font-weight: bold;'>").append(alert.getSeverity().toUpperCase());
                break;
            case "medium":
                html.append("#FF9800; font-weight: bold;'>").append(alert.getSeverity().toUpperCase());
                break;
            default:
                html.append("#4CAF50; font-weight: bold;'>").append(alert.getSeverity().toUpperCase());
        }
        
        html.append("</span></p>");
        html.append("<p><span class='label'>Message:</span><br>").append(alert.getMessage()).append("</p>");
        html.append("<p><span class='label'>Created At:</span> ").append(alert.getCreatedAt()).append("</p>");
        html.append("</div>");
        
        html.append("<p style='margin-top: 20px;'>Please log in to the AWS Governance Tool to review and resolve this alert.</p>");
        html.append("</div>");
        
        html.append("<div class='footer'>");
        html.append("<p>This is an automated message from AWS Cloud Governance & Resource Monitoring Tool</p>");
        html.append("<p>Do not reply to this email</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Configure email settings (can be called from UI)
     */
    public void configureEmail(String fromEmail, String password) {
        // This would update email configuration
        // In production, store these securely in database or config file
        System.out.println("Email configuration updated");
    }
}
