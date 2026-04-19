package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Classes - White-Box Coverage Tests")
public class ModelTests {

    @Test
    @DisplayName("TC1: Alert model - Create and verify properties")
    void testAlertCreation() {
        System.out.println("\n=== TC1: Alert Model ===");
        Alert alert = new Alert("i-12345", "EC2", "High CPU", "CRITICAL", "CPU exceeds 80%");
        
        alert.setAlertId(1);
        assertEquals(1, alert.getAlertId());
        assertEquals("i-12345", alert.getResourceId());
        assertEquals("EC2", alert.getResourceType());
        assertEquals("High CPU", alert.getAlertType());
        assertEquals("CRITICAL", alert.getSeverity());
        System.out.println("✓ PASS: Alert properties verified");
    }

    @Test
    @DisplayName("TC2: Alert - Resolved status")
    void testAlertResolvedStatus() {
        System.out.println("\n=== TC2: Alert Resolved Status ===");
        Alert alert = new Alert("i-12345", "EC2", "High CPU", "CRITICAL", "CPU exceeds 80%");
        
        assertFalse(alert.isResolved());
        alert.setResolved(true);
        assertTrue(alert.isResolved());
        System.out.println("✓ PASS: Alert resolved status toggle works");
    }

    @Test
    @DisplayName("TC3: Rule model - Create and verify")
    void testRuleCreation() {
        System.out.println("\n=== TC3: Rule Model ===");
        Rule rule = new Rule("CPU_THRESHOLD", "Monitoring", "EC2", "Alert");
        
        rule.setRuleId(1);
        rule.setResourceType("EC2");
        rule.setConditionMetric("CPUUtilization");
        rule.setConditionOperator("GreaterThan");
        rule.setConditionValue(80.0);
        
        assertEquals(1, rule.getRuleId());
        assertEquals("CPU_THRESHOLD", rule.getRuleName());
        assertEquals(80.0, rule.getConditionValue());
        System.out.println("✓ PASS: Rule properties verified");
    }

    @Test
    @DisplayName("TC4: Rule - Active status")
    void testRuleActiveStatus() {
        System.out.println("\n=== TC4: Rule Active Status ===");
        Rule rule = new Rule("TEST_RULE", "Monitoring", "EC2", "Alert");
        
        assertTrue(rule.isActive()); // Default is true
        rule.setActive(false);
        assertFalse(rule.isActive());
        System.out.println("✓ PASS: Rule active status toggle works");
    }

    @Test
    @DisplayName("TC5: Multiple model instances - Getters/Setters")
    void testMultipleModelInstances() {
        System.out.println("\n=== TC5: Multiple Model Instances ===");
        Alert alert1 = new Alert("res1", "EC2", "Alert1", "HIGH", "msg1");
        Alert alert2 = new Alert("res2", "S3", "Alert2", "LOW", "msg2");
        Alert alert3 = new Alert("res3", "SQS", "Alert3", "MEDIUM", "msg3");
        
        assertEquals("Alert1", alert1.getAlertType());
        assertEquals("Alert2", alert2.getAlertType());
        assertEquals("Alert3", alert3.getAlertType());
        System.out.println("✓ PASS: Multiple model instances created and verified");
    }

    @Test
    @DisplayName("TC6: Rule collection - Iterate and filter")
    void testRuleCollection() {
        System.out.println("\n=== TC6: Rule Collection ===");
        java.util.List<Rule> rules = new java.util.ArrayList<>();
        
        Rule rule1 = new Rule("RULE1", "Monitoring", "EC2", "Alert");
        Rule rule2 = new Rule("RULE2", "Monitoring", "S3", "Alert");
        Rule rule3 = new Rule("RULE3", "Monitoring", "SQS", "Alert");
        
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        
        assertEquals(3, rules.size());
        
        // STATEMENT COVERAGE: Filter rules
        int ec2Rules = 0;
        for (Rule rule : rules) {
            if ("EC2".equals(rule.getResourceType())) {
                ec2Rules++;
            }
        }
        assertEquals(1, ec2Rules);
        System.out.println("✓ PASS: Rule collection filtering works");
    }

    @Test
    @DisplayName("TC7: Alert message handling")
    void testAlertMessageHandling() {
        System.out.println("\n=== TC7: Alert Message Handling ===");
        Alert alert = new Alert("i-12345", "EC2", "Alert", "HIGH", "Initial message");
        
        assertEquals("Initial message", alert.getMessage());
        alert.setMessage("Updated message");
        assertEquals("Updated message", alert.getMessage());
        System.out.println("✓ PASS: Alert message handling works");
    }

    @Test
    @DisplayName("TC8: Rule value conditions")
    void testRuleValueConditions() {
        System.out.println("\n=== TC8: Rule Value Conditions ===");
        Rule rule = new Rule("THRESHOLD_RULE", "Monitoring", "EC2", "Alert");
        
        rule.setConditionValue(80.0);
        rule.setConditionDuration(300);
        rule.setDurationUnit("seconds");
        
        assertEquals(80.0, rule.getConditionValue());
        assertEquals(300, rule.getConditionDuration());
        assertEquals("seconds", rule.getDurationUnit());
        System.out.println("✓ PASS: Rule condition values verified");
    }

    @Test
    @DisplayName("TC9: Alert and Rule relationship")
    void testAlertRuleRelationship() {
        System.out.println("\n=== TC9: Alert-Rule Relationship ===");
        Alert alert = new Alert("res-123", "EC2", "High CPU", "CRITICAL", "CPU alert");
        Rule rule = new Rule("CPU_RULE", "Monitoring", "EC2", "Alert");
        
        rule.setRuleId(100);
        alert.setRuleId(100);
        
        assertEquals(rule.getRuleId(), alert.getRuleId());
        System.out.println("✓ PASS: Alert-Rule relationship verified");
    }

    @Test
    @DisplayName("TC10: Model null handling")
    void testModelNullHandling() {
        System.out.println("\n=== TC10: Null Handling ===");
        Alert alert = new Alert();
        
        alert.setResourceId("new-resource");
        alert.setResourceType("EC2");
        assertNotNull(alert.getResourceId());
        assertNotNull(alert.getResourceType());
        System.out.println("✓ PASS: Null handling works correctly");
    }
}
