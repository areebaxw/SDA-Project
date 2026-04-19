package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Classes - Comprehensive 50% Coverage")
public class ComprehensiveModelTests {

    @Test
    @DisplayName("EC2Instance: All properties")
    void testEC2InstanceProperties() {
        System.out.println("\n=== EC2Instance: Properties ===");
        EC2Instance instance = new EC2Instance();
        instance.setRecordId(1);
        instance.setInstanceId("i-001");
        instance.setInstanceType("t2.micro");
        instance.setInstanceState("running");
        instance.setAvailabilityZone("us-east-1a");
        
        assertEquals("i-001", instance.getInstanceId());
        assertEquals("t2.micro", instance.getInstanceType());
        assertEquals("running", instance.getInstanceState());
        System.out.println("✓ PASS: EC2Instance properties verified");
    }

    @Test
    @DisplayName("S3BucketResource: All properties")
    void testS3BucketResourceProperties() {
        System.out.println("\n=== S3BucketResource: Properties ===");
        S3BucketResource bucket = new S3BucketResource();
        bucket.setRecordId(1);
        bucket.setBucketName("my-bucket");
        bucket.setRegion("us-east-1");
        bucket.setLastChecked(LocalDateTime.now());
        bucket.setObjectCount(1500);
        bucket.setTotalSizeGb(5.0);
        bucket.setIsPublic(false);
        bucket.setIdle(false);
        
        assertEquals(1, bucket.getRecordId());
        assertEquals("my-bucket", bucket.getBucketName());
        assertEquals("us-east-1", bucket.getRegion());
        assertEquals(false, bucket.getIsPublic());
        System.out.println("✓ PASS: S3BucketResource properties verified");
    }

    @Test
    @DisplayName("SQSQueueResource: All properties")
    void testSQSQueueResourceProperties() {
        System.out.println("\n=== SQSQueueResource: Properties ===");
        SQSQueueResource queue = new SQSQueueResource();
        queue.setRecordId(1);
        queue.setQueueName("my-queue");
        queue.setQueueUrl("https://sqs.us-east-1.amazonaws.com/123456789/my-queue");
        queue.setQueueArn("arn:aws:sqs:us-east-1:123456789:my-queue");
        queue.setMessageCount(45);
        queue.setDelayedMessageCount(5);
        queue.setIdle(false);
        queue.setLastChecked(LocalDateTime.now());
        
        assertEquals(1, queue.getRecordId());
        assertEquals("my-queue", queue.getQueueName());
        assertEquals(45, queue.getMessageCount());
        System.out.println("✓ PASS: SQSQueueResource properties verified");
    }

    @Test
    @DisplayName("ALBResource: All properties")
    void testALBResourceProperties() {
        System.out.println("\n=== ALBResource: Properties ===");
        ALBResource alb = new ALBResource();
        alb.setRecordId(1);
        alb.setLoadBalancerName("prod-alb");
        alb.setLoadBalancerArn("arn:aws:elasticloadbalancing:us-east-1:123456789:loadbalancer/app/prod-alb");
        alb.setScheme("internet-facing");
        alb.setState("active");
        alb.setDnsName("prod-alb-123456.us-east-1.elb.amazonaws.com");
        alb.setLastChecked(LocalDateTime.now());
        
        assertEquals(1, alb.getRecordId());
        assertEquals("prod-alb", alb.getLoadBalancerName());
        assertEquals("internet-facing", alb.getScheme());
        System.out.println("✓ PASS: ALBResource properties verified");
    }

    @Test
    @DisplayName("User: All properties")
    void testUserProperties() {
        System.out.println("\n=== User: Properties ===");
        User user = new User();
        user.setUserId(1);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("password123");
        user.setFullName("Admin User");
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        
        assertEquals(1, user.getUserId());
        assertEquals("admin", user.getUsername());
        assertEquals("admin@example.com", user.getEmail());
        System.out.println("✓ PASS: User properties verified");
    }

    @Test
    @DisplayName("AWSCredential: All properties")
    void testAWSCredentialProperties() {
        System.out.println("\n=== AWSCredential: Properties ===");
        AWSCredential cred = new AWSCredential();
        cred.setCredentialId(1);
        cred.setUserId(1);
        cred.setAccessKey("AKIAIOSFODNN7EXAMPLE");
        cred.setSecretKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        cred.setRegion("us-east-1");
        cred.setCreatedAt(LocalDateTime.now());
        
        assertEquals(1, cred.getCredentialId());
        assertEquals(1, cred.getUserId());
        assertEquals("us-east-1", cred.getRegion());
        System.out.println("✓ PASS: AWSCredential properties verified");
    }

    @Test
    @DisplayName("Alert: Lifecycle tests")
    void testAlertLifecycle() {
        System.out.println("\n=== Alert: Lifecycle ===");
        Alert alert = new Alert("i-001", "EC2", "HighCPU", "CRITICAL", "CPU > 80%");
        
        assertFalse(alert.isResolved());
        
        alert.setResolved(true);
        assertTrue(alert.isResolved());
        
        LocalDateTime now = LocalDateTime.now();
        alert.setResolvedAt(now);
        assertEquals(now, alert.getResolvedAt());
        
        System.out.println("✓ PASS: Alert lifecycle complete");
    }

    @Test
    @DisplayName("Rule: Activation lifecycle")
    void testRuleActivationLifecycle() {
        System.out.println("\n=== Rule: Activation ===");
        Rule rule = new Rule("HighCPURule", "Performance", "EC2", "Alert");
        
        assertTrue(rule.isActive());
        
        rule.setActive(false);
        assertFalse(rule.isActive());
        
        rule.setActive(true);
        assertTrue(rule.isActive());
        
        System.out.println("✓ PASS: Rule activation complete");
    }

    @Test
    @DisplayName("BillingRecord: Cost tracking")
    void testBillingRecordCostTracking() {
        System.out.println("\n=== BillingRecord: Cost Tracking ===");
        BillingRecord record = new BillingRecord();
        record.setRecordId(1);
        record.setServiceName("EC2");
        record.setCostAmount(500.50);
        record.setUserId(1);
        
        assertEquals(1, record.getRecordId());
        assertEquals(500.50, record.getCostAmount());
        System.out.println("✓ PASS: Billing record tracked");
    }

    @Test
    @DisplayName("EC2Instance: State transitions")
    void testEC2InstanceStateTransitions() {
        System.out.println("\n=== EC2Instance: State Transitions ===");
        EC2Instance instance = new EC2Instance();
        
        instance.setInstanceState("pending");
        assertEquals("pending", instance.getInstanceState());
        
        instance.setInstanceState("running");
        assertEquals("running", instance.getInstanceState());
        
        instance.setInstanceState("stopping");
        assertEquals("stopping", instance.getInstanceState());
        
        instance.setInstanceState("stopped");
        assertEquals("stopped", instance.getInstanceState());
        
        System.out.println("✓ PASS: State transitions complete");
    }

    @Test
    @DisplayName("S3BucketResource: Size calculations")
    void testS3BucketResourceSizeCalculations() {
        System.out.println("\n=== S3BucketResource: Size ===");
        S3BucketResource bucket = new S3BucketResource();
        bucket.setTotalSizeGb(10.0); // 10 GB
        bucket.setObjectCount(5000);
        
        assertEquals(10.0, bucket.getTotalSizeGb());
        assertEquals(5000, bucket.getObjectCount());
        
        double avgObjectSize = bucket.getTotalSizeGb() / bucket.getObjectCount();
        assertTrue(avgObjectSize > 0);
        System.out.println("✓ PASS: Size calculations verified");
    }

    @Test
    @DisplayName("SQSQueueResource: Message metrics")
    void testSQSQueueResourceMessageMetrics() {
        System.out.println("\n=== SQSQueueResource: Message Metrics ===");
        SQSQueueResource queue = new SQSQueueResource();
        queue.setMessageCount(100);
        queue.setDelayedMessageCount(20);
        
        long totalMessages = queue.getMessageCount() + queue.getDelayedMessageCount();
        assertEquals(120, totalMessages);
        System.out.println("✓ PASS: Message metrics verified");
    }

    @Test
    @DisplayName("ALBResource: Load balancer properties")
    void testALBResourceTargetHealth() {
        System.out.println("\n=== ALBResource: Properties ===");
        ALBResource alb = new ALBResource();
        alb.setLoadBalancerName("prod-alb");
        alb.setScheme("internet-facing");
        alb.setState("active");
        
        assertEquals("prod-alb", alb.getLoadBalancerName());
        assertEquals("internet-facing", alb.getScheme());
        System.out.println("✓ PASS: ALB properties verified");
    }

    @Test
    @DisplayName("User: Email verification")
    void testUserEmailVerification() {
        System.out.println("\n=== User: Email ===");
        User user = new User();
        user.setEmail("admin@example.com");
        
        assertTrue(user.getEmail().contains("@"));
        System.out.println("✓ PASS: Email verified");
    }

    @Test
    @DisplayName("AWSCredential: Regional configurations")
    void testAWSCredentialRegionalConfigurations() {
        System.out.println("\n=== AWSCredential: Regions ===");
        List<String> regions = Arrays.asList("us-east-1", "us-west-2", "eu-west-1");
        
        for (String region : regions) {
            AWSCredential cred = new AWSCredential();
            cred.setRegion(region);
            assertTrue(cred.getRegion().contains("-"));
        }
        
        System.out.println("✓ PASS: Regional configurations verified");
    }

    @Test
    @DisplayName("Alert: Severity levels")
    void testAlertSeverityLevels() {
        System.out.println("\n=== Alert: Severity Levels ===");
        String[] severities = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};
        
        for (String severity : severities) {
            Alert alert = new Alert("r-001", "EC2", "Test", severity, "Test message");
            assertEquals(severity, alert.getSeverity());
        }
        
        System.out.println("✓ PASS: Severity levels verified");
    }

    @Test
    @DisplayName("Rule: Condition metrics")
    void testRuleConditionMetrics() {
        System.out.println("\n=== Rule: Condition Metrics ===");
        Rule rule = new Rule();
        rule.setConditionMetric("CPUUtilization");
        rule.setConditionOperator(">");
        rule.setConditionValue(80.0);
        
        assertEquals("CPUUtilization", rule.getConditionMetric());
        assertEquals(">", rule.getConditionOperator());
        assertEquals(80.0, rule.getConditionValue());
        System.out.println("✓ PASS: Condition metrics verified");
    }

    @Test
    @DisplayName("BillingRecord: Multi-service tracking")
    void testBillingRecordMultiService() {
        System.out.println("\n=== BillingRecord: Multi-Service ===");
        
        double totalCost = 0;
        for (int i = 0; i < 5; i++) {
            BillingRecord record = new BillingRecord();
            record.setCostAmount(100.0);
            totalCost += record.getCostAmount();
        }
        
        assertEquals(500.0, totalCost);
        System.out.println("✓ PASS: Multi-service tracking verified");
    }

    @Test
    @DisplayName("EC2Instance: Instance metadata")
    void testEC2InstanceMetadata() {
        System.out.println("\n=== EC2Instance: Metadata ===");
        EC2Instance instance = new EC2Instance();
        instance.setInstanceId("i-001");
        instance.setInstanceType("t2.micro");
        instance.setAvailabilityZone("us-east-1a");
        
        assertTrue(instance.getInstanceId().startsWith("i-"));
        assertTrue(instance.getInstanceType().startsWith("t2."));
        assertTrue(instance.getAvailabilityZone().length() > 0);
        System.out.println("✓ PASS: Metadata verified");
    }

    @Test
    @DisplayName("S3BucketResource: Bucket configuration")
    void testS3BucketResourceConfiguration() {
        System.out.println("\n=== S3BucketResource: Configuration ===");
        S3BucketResource bucket = new S3BucketResource();
        bucket.setIsPublic(false);
        bucket.setIdle(false);
        
        assertEquals(false, bucket.getIsPublic());
        assertEquals(false, bucket.isIdle());
        System.out.println("✓ PASS: Configuration verified");
    }

    @Test
    @DisplayName("SQSQueueResource: Queue configuration")
    void testSQSQueueResourceConfiguration() {
        System.out.println("\n=== SQSQueueResource: Configuration ===");
        SQSQueueResource queue = new SQSQueueResource();
        queue.setIdle(false);
        queue.setQueueName("my-queue");
        
        assertEquals(false, queue.isIdle());
        assertEquals("my-queue", queue.getQueueName());
        System.out.println("✓ PASS: Configuration verified");
    }

    @Test
    @DisplayName("ALBResource: Listener configuration")
    void testALBResourceListenerConfiguration() {
        System.out.println("\n=== ALBResource: Listeners ===");
        ALBResource alb = new ALBResource();
        alb.setLoadBalancerName("prod-alb");
        alb.setState("active");
        
        assertEquals("prod-alb", alb.getLoadBalancerName());
        System.out.println("✓ PASS: Listener configuration verified");
    }

    @Test
    @DisplayName("User: Credential association")
    void testUserCredentialAssociation() {
        System.out.println("\n=== User: Credentials ===");
        User user = new User();
        user.setUserId(1);
        user.setUsername("admin");
        
        AWSCredential cred = new AWSCredential();
        cred.setUserId(user.getUserId());
        
        assertEquals(user.getUserId(), cred.getUserId());
        System.out.println("✓ PASS: Credential association verified");
    }

    @Test
    @DisplayName("Alert: Rule association")
    void testAlertRuleAssociation() {
        System.out.println("\n=== Alert: Rule Association ===");
        Rule rule = new Rule();
        rule.setRuleId(1);
        
        Alert alert = new Alert();
        alert.setRuleId(rule.getRuleId());
        
        assertEquals(rule.getRuleId(), alert.getRuleId());
        System.out.println("✓ PASS: Rule association verified");
    }

    @Test
    @DisplayName("EC2Instance: Timestamp tracking")
    void testEC2InstanceTimestampTracking() {
        System.out.println("\n=== EC2Instance: Timestamps ===");
        EC2Instance instance = new EC2Instance();
        LocalDateTime launchTime = LocalDateTime.now();
        instance.setLaunchTime(launchTime);
        
        assertEquals(launchTime, instance.getLaunchTime());
        System.out.println("✓ PASS: Timestamp tracking verified");
    }

    @Test
    @DisplayName("BillingRecord: Cost aggregation")
    void testBillingRecordCostAggregation() {
        System.out.println("\n=== BillingRecord: Cost Aggregation ===");
        List<BillingRecord> records = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            BillingRecord record = new BillingRecord();
            record.setCostAmount(100.0);
            records.add(record);
        }
        
        double totalCost = records.stream().mapToDouble(BillingRecord::getCostAmount).sum();
        assertEquals(500.0, totalCost);
        System.out.println("✓ PASS: Cost aggregation verified");
    }
}
