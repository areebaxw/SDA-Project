package utils;

import java.util.regex.Pattern;

/**
 * Validator - Utility class for input validation
 * Implements GRASP Information Expert pattern
 */
public class Validator {
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // AWS Access Key pattern (starts with AKIA)
    private static final Pattern AWS_ACCESS_KEY_PATTERN = Pattern.compile(
        "^AKIA[0-9A-Z]{16}$"
    );
    
    // AWS Region pattern
    private static final Pattern AWS_REGION_PATTERN = Pattern.compile(
        "^[a-z]{2}-[a-z]+-[0-9]{1}$"
    );
    
    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate username
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.length() >= 3 && username.length() <= 50;
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 6; // Minimum 6 characters
    }
    
    /**
     * Validate AWS access key format
     */
    public static boolean isValidAWSAccessKey(String accessKey) {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            return false;
        }
        return AWS_ACCESS_KEY_PATTERN.matcher(accessKey.trim()).matches() || 
               accessKey.length() == 20; // Some keys may not start with AKIA
    }
    
    /**
     * Validate AWS secret key format
     */
    public static boolean isValidAWSSecretKey(String secretKey) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            return false;
        }
        return secretKey.length() == 40; // AWS secret keys are 40 characters
    }
    
    /**
     * Validate AWS region format
     */
    public static boolean isValidAWSRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            return false;
        }
        return AWS_REGION_PATTERN.matcher(region.trim()).matches();
    }
    
    /**
     * Validate string is not empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate numeric value is within range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate positive number
     */
    public static boolean isPositive(double value) {
        return value > 0;
    }
    
    /**
     * Validate rule name
     */
    public static boolean isValidRuleName(String ruleName) {
        if (ruleName == null || ruleName.trim().isEmpty()) {
            return false;
        }
        return ruleName.length() >= 3 && ruleName.length() <= 100;
    }
    
    /**
     * Get validation error message for password
     */
    public static String getPasswordValidationMessage() {
        return "Password must be at least 6 characters long";
    }
    
    /**
     * Get validation error message for email
     */
    public static String getEmailValidationMessage() {
        return "Please enter a valid email address";
    }
    
    /**
     * Get validation error message for username
     */
    public static String getUsernameValidationMessage() {
        return "Username must be between 3 and 50 characters";
    }
}
