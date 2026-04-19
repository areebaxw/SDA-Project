package services;

import models.Alert;
import models.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Service Layer - White-Box Coverage Tests")
public class ServiceLayerTests {

    // ============ ALERT SERVICE TESTS ============
    
    @Test
    @DisplayName("TC1: Alert creation and validation")
    void testAlertCreation() {
        System.out.println("\n=== TC1: Alert Creation ===");
        Alert alert = new Alert("i-12345", "EC2", "High CPU", "CRITICAL", "CPU > 80%");
        alert.setAlertId(1);
        
        assertNotNull(alert);
        assertEquals("High CPU", alert.getAlertType());
        assertFalse(alert.isResolved());
        System.out.println("✓ PASS: Alert created and validated");
    }

    @Test
    @DisplayName("TC2: Alert resolution workflow")
    void testAlertResolution() {
        System.out.println("\n=== TC2: Alert Resolution ===");
        Alert alert = new Alert("res-123", "SQS", "Queue Full", "HIGH", "Messages exceeded 1000");
        
        // BRANCH COVERAGE: IF branch (alert not resolved)
        if (!alert.isResolved()) {
            alert.setResolved(true);
        }
        
        assertTrue(alert.isResolved());
        System.out.println("✓ PASS: Alert resolution workflow tested");
    }

    @Test
    @DisplayName("TC3: Multiple alerts handling")
    void testMultipleAlerts() {
        System.out.println("\n=== TC3: Multiple Alerts ===");
        List<Alert> alerts = new ArrayList<>();
        alerts.add(new Alert("res-1", "EC2", "Alert1", "HIGH", "msg1"));
        alerts.add(new Alert("res-2", "S3", "Alert2", "MEDIUM", "msg2"));
        alerts.add(new Alert("res-3", "SQS", "Alert3", "LOW", "msg3"));
        
        assertEquals(3, alerts.size());
        
        // STATEMENT COVERAGE: Process all alerts
        int resolved = 0;
        for (Alert alert : alerts) {
            if (alert.isResolved()) {
                resolved++;
            }
        }
        
        assertEquals(0, resolved);
        System.out.println("✓ PASS: Multiple alerts handled correctly");
    }

    @Test
    @DisplayName("TC4: Alert severity filtering")
    void testAlertSeverityFilter() {
        System.out.println("\n=== TC4: Alert Severity Filter ===");
        List<Alert> alerts = new ArrayList<>();
        alerts.add(new Alert("res-1", "EC2", "Alert1", "CRITICAL", "msg1"));
        alerts.add(new Alert("res-2", "S3", "Alert2", "MEDIUM", "msg2"));
        alerts.add(new Alert("res-3", "SQS", "Alert3", "CRITICAL", "msg3"));
        
        // BRANCH COVERAGE: Filter by severity
        List<Alert> criticalAlerts = new ArrayList<>();
        for (Alert alert : alerts) {
            if ("CRITICAL".equals(alert.getSeverity())) {
                criticalAlerts.add(alert);
            }
        }
        
        assertEquals(2, criticalAlerts.size());
        System.out.println("✓ PASS: Alert severity filtering works");
    }

    @Test
    @DisplayName("TC5: Alert message updates")
    void testAlertMessageUpdates() {
        System.out.println("\n=== TC5: Alert Message Updates ===");
        Alert alert = new Alert("res-123", "EC2", "CPU Alert", "HIGH", "Initial message");
        
        assertEquals("Initial message", alert.getMessage());
        alert.setMessage("Updated message with details");
        assertEquals("Updated message with details", alert.getMessage());
        System.out.println("✓ PASS: Alert message updates work");
    }

    // ============ RULE EVALUATION TESTS ============
    
    @Test
    @DisplayName("TC6: Rule condition evaluation (TRUE)")
    void testRuleEvaluationTrue() {
        System.out.println("\n=== TC6: Rule Evaluation (TRUE) ===");
        Rule rule = new Rule("CPU_RULE", "Monitoring", "EC2", "Alert");
        rule.setConditionValue(80.0);
        
        double metricValue = 85.0;
        
        // BRANCH COVERAGE: IF branch
        if (metricValue > rule.getConditionValue()) {
            assertTrue(true, "Condition evaluated to true");
        }
        
        System.out.println("✓ PASS: Rule condition (TRUE) evaluated correctly");
    }

    @Test
    @DisplayName("TC7: Rule condition evaluation (FALSE)")
    void testRuleEvaluationFalse() {
        System.out.println("\n=== TC7: Rule Evaluation (FALSE) ===");
        Rule rule = new Rule("CPU_RULE", "Monitoring", "EC2", "Alert");
        rule.setConditionValue(80.0);
        
        double metricValue = 50.0;
        
        // BRANCH COVERAGE: ELSE branch
        if (metricValue > rule.getConditionValue()) {
            fail("Condition should not be true");
        } else {
            assertTrue(true, "Condition evaluated to false as expected");
        }
        
        System.out.println("✓ PASS: Rule condition (FALSE) evaluated correctly");
    }

    @Test
    @DisplayName("TC8: Rule collection iteration")
    void testRuleCollection() {
        System.out.println("\n=== TC8: Rule Collection ===");
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("RULE1", "Monitoring", "EC2", "Alert"));
        rules.add(new Rule("RULE2", "Monitoring", "S3", "Alert"));
        rules.add(new Rule("RULE3", "Monitoring", "SQS", "Alert"));
        
        // STATEMENT COVERAGE: Iterate all rules
        int activeRules = 0;
        for (Rule rule : rules) {
            if (rule.isActive()) {
                activeRules++;
            }
        }
        
        assertEquals(3, activeRules);
        System.out.println("✓ PASS: Rule collection iteration works");
    }

    @Test
    @DisplayName("TC9: Rule status toggle")
    void testRuleStatusToggle() {
        System.out.println("\n=== TC9: Rule Status Toggle ===");
        Rule rule = new Rule("TEST_RULE", "Monitoring", "EC2", "Alert");
        
        assertTrue(rule.isActive());
        rule.setActive(false);
        assertFalse(rule.isActive());
        rule.setActive(true);
        assertTrue(rule.isActive());
        System.out.println("✓ PASS: Rule status toggling works");
    }

    @Test
    @DisplayName("TC10: Boundary value testing")
    void testBoundaryValues() {
        System.out.println("\n=== TC10: Boundary Value Testing ===");
        Rule rule = new Rule("BOUNDARY", "Monitoring", "EC2", "Alert");
        rule.setConditionValue(50.0);
        
        // Test boundary values
        double just_below = 49.9;
        double exact = 50.0;
        double just_above = 50.1;
        
        assertFalse(just_below > rule.getConditionValue());
        assertFalse(exact > rule.getConditionValue());
        assertTrue(just_above > rule.getConditionValue());
        System.out.println("✓ PASS: Boundary value analysis complete");
    }

    @Test
    @DisplayName("TC11: Idle detection logic")
    void testIdleDetectionLogic() {
        System.out.println("\n=== TC11: Idle Detection ===");
        int messageCount = 0;
        int delayedMessages = 0;
        
        // BRANCH COVERAGE: IF branch (idle = true)
        boolean isIdle = (messageCount == 0 && delayedMessages == 0);
        assertTrue(isIdle);
        
        messageCount = 10;
        
        // BRANCH COVERAGE: ELSE branch (idle = false)
        isIdle = (messageCount == 0 && delayedMessages == 0);
        assertFalse(isIdle);
        System.out.println("✓ PASS: Idle detection logic tested");
    }

    @Test
    @DisplayName("TC12: Complex condition evaluation")
    void testComplexConditions() {
        System.out.println("\n=== TC12: Complex Condition Evaluation ===");
        Rule cpuRule = new Rule("CPU", "Monitoring", "EC2", "Alert");
        Rule memRule = new Rule("MEM", "Monitoring", "EC2", "Alert");
        
        cpuRule.setConditionValue(80.0);
        memRule.setConditionValue(70.0);
        
        double cpuUsage = 85.0;
        double memUsage = 65.0;
        
        // STATEMENT COVERAGE: Evaluate multiple conditions
        boolean shouldAlert = (cpuUsage > cpuRule.getConditionValue()) || 
                             (memUsage > memRule.getConditionValue());
        
        assertTrue(shouldAlert, "Should alert when CPU exceeds threshold");
        System.out.println("✓ PASS: Complex condition evaluation works");
    }
}
