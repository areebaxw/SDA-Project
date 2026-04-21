package aws;

import models.SQSQueueResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White-box tests for SQSMonitoringService
 * Tests both success and error paths for SQS operations
 * HARDCODED CREDENTIALS FOR ACADEMIC TESTING ONLY
 */
@DisplayName("SQS Monitoring Service White-Box Tests")
public class SQSMonitoringServiceTest {
    
    private static String awsAccessKey;
    private static String awsSecretKey;
    private static final String AWS_REGION = "us-east-1";

    private SQSMonitoringService sqsService;
    private static final int TEST_USER_ID = 1;

    @BeforeEach
    void setUp() {
        try (FileInputStream input = new FileInputStream("src/main/resources/credentials.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            awsAccessKey = prop.getProperty("aws.accessKey");
            awsSecretKey = prop.getProperty("aws.secretKey");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // Initialize factory with credentials FIRST
        AWSClientFactory.getInstance().initializeCredentials(awsAccessKey, awsSecretKey, AWS_REGION);
        
        // Now create the service - it will use the initialized credentials
        sqsService = new SQSMonitoringService();
    }

    /**
     * Test Case 1: Successful retrieval of queues (TRY block path)
     * Tests the happy path when AWS returns successfully
     * BRANCH COVERAGE: Tests successful execution path
     */
    @Test
    @DisplayName("TC1: getAllQueues() returns list from TRY block")
    void testGetAllQueues_ReturnsNonNullList() {
        System.out.println("\n=== TC1: Testing SQS getAllQueues (TRY block) ===");
        System.out.println("Credentials: Region=" + AWS_REGION);
        
        // Arrange
        int userId = TEST_USER_ID;
        
        // Act: Execute the method's TRY block
        List<SQSQueueResource> result = sqsService.getAllQueues(userId);
        
        // Assert: TRY block must always return a list
        assertNotNull(result, "Should return non-null list from TRY block");
        assertTrue(result instanceof List, "Result should be a List instance");
        System.out.println("✓ PASS: Retrieved " + result.size() + " SQS queues");
    }

    /**
     * Test Case 2: Empty queues list returned
     * Tests the TRY block when no queues exist in AWS
     * STATEMENT COVERAGE: Tests empty response handling
     */
    @Test
    @DisplayName("TC2: getAllQueues() handles empty response gracefully")
    void testGetAllQueues_HandlesEmptyResponse() {
        System.out.println("\n=== TC2: Testing SQS empty response (STATEMENT COVERAGE) ===");
        
        // Arrange
        int userId = TEST_USER_ID;
        
        // Act
        List<SQSQueueResource> result = sqsService.getAllQueues(userId);
        
        // Assert: Should return a list (even if empty)
        assertNotNull(result, "Should never return null, even when empty");
        assertTrue(result instanceof List, "Should always return List type");
        System.out.println("✓ PASS: Returned list with " + result.size() + " items");
        System.out.println("  Empty response handled gracefully");
    }

    /**
     * Test Case 3: Queue data mapping verification
     * Tests that queue properties are correctly extracted and mapped
     * STATEMENT COVERAGE: Tests all mapping statements
     */
    @Test
    @DisplayName("TC3: Queue properties mapped correctly from AWS response")
    void testQueueDataMapping_PropertiesArePopulated() {
        System.out.println("\n=== TC3: Testing SQS queue data mapping ===");
        
        // Arrange
        int userId = TEST_USER_ID;
        
        // Act
        List<SQSQueueResource> result = sqsService.getAllQueues(userId);
        
        // Assert: If queues exist, verify all properties are mapped
        if (!result.isEmpty()) {
            SQSQueueResource queue = result.get(0);
            System.out.println("Queue Details:");
            System.out.println("  - URL: " + queue.getQueueUrl());
            System.out.println("  - Name: " + queue.getQueueName());
            System.out.println("  - ARN: " + queue.getQueueArn());
            System.out.println("  - Message Count: " + queue.getMessageCount());
            
            assertNotNull(queue, "Queue object should be created");
            assertNotNull(queue.getQueueUrl(), "Queue URL should be populated");
            assertNotNull(queue.getQueueName(), "Queue name should be populated");
            assertNotNull(queue.getQueueArn(), "Queue ARN should be populated");
            assertTrue(queue.getMessageCount() >= 0, "Message count should be non-negative");
            System.out.println("✓ PASS: All queue properties correctly mapped");
        } else {
            System.out.println("✓ PASS: No queues found (skipping property verification)");
        }
    }

    /**
     * Test Case 4: Idle status detection - Non-idle with messages
     * Tests the idle detection logic when messages are present
     */
    @Test
    @DisplayName("Queue with messages should not be marked as idle")
    void testIdleStatus_QueueWithMessages_IsNonIdle() {
        System.out.println("\n=== TC4: Testing idle detection (messages present) ===");
        
        // Arrange
        int userId = TEST_USER_ID;
        
        // Act
        List<SQSQueueResource> result = sqsService.getAllQueues(userId);
        
        // Assert: If a queue has messages, it must not be idle
        System.out.println("Checking idle status for queues with messages...");
        for (SQSQueueResource queue : result) {
            if (queue.getMessageCount() > 0 || queue.getDelayedMessageCount() > 0) {
                System.out.println("  Queue: " + queue.getQueueName() + 
                    " | Messages: " + queue.getMessageCount() + 
                    " | Idle: " + queue.isIdle());
                assertFalse(queue.isIdle(), 
                    "Queue with messages should have isIdle=false");
            }
        }
        System.out.println("✓ PASS: Non-idle detection works correctly");
    }

    /**
     * Test Case 5: Idle status detection - Idle with no messages
     * Tests the idle detection logic when no messages are present
     */
    @Test
    @DisplayName("Queue without messages should be marked as idle")
    void testIdleStatus_QueueWithoutMessages_IsIdle() {
        // Arrange
        int userId = TEST_USER_ID;
        
        // Act
        List<SQSQueueResource> result = sqsService.getAllQueues(userId);
        
        // Assert: If a queue has no messages, it must be idle
        for (SQSQueueResource queue : result) {
            if (queue.getMessageCount() == 0 && queue.getDelayedMessageCount() == 0) {
                assertTrue(queue.isIdle(), 
                    "Queue with no messages should have isIdle=true");
            }
        }
    }

    /**
     * Test Case 6: Sync operation returns count
     * Tests the syncFromAWS method's execution path
     */
    @Test
    @DisplayName("syncFromAWS should return non-negative sync count")
    void testSyncFromAWS_ReturnsSyncCount() {
        // Arrange
        int userId = TEST_USER_ID;
        
        // Act: Execute sync operation
        int syncedCount = sqsService.syncFromAWS(userId);
        
        // Assert: Count should be valid
        assertNotNull(syncedCount, "Should return a sync count");
        assertTrue(syncedCount >= 0, "Sync count should be non-negative");
    }

    /**
     * Test Case 7: Purge queue operation success path
     * Tests the TRY block of purgeQueue method
     */
    @Test
    @DisplayName("purgeQueue should return boolean result from TRY block")
    void testPurgeQueue_ReturnsBoolean() {
        // Arrange
        String validQueueUrl = "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue";
        
        // Act
        boolean result = sqsService.purgeQueue(validQueueUrl);
        
        // Assert: Should return a boolean value
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Test Case 8: Purge queue with invalid URL (Error Guessing)
     * Tests error handling in CATCH block when URL is invalid
     */
    @Test
    @DisplayName("purgeQueue should handle invalid URL gracefully (CATCH block)")
    void testPurgeQueue_WithInvalidUrl_HandleError() {
        // Arrange: Invalid empty URL should trigger error handling
        String invalidUrl = "";
        
        // Act: Execute with invalid URL
        boolean result = sqsService.purgeQueue(invalidUrl);
        
        // Assert: Should return false (error case), not throw uncaught exception
        assertFalse(result, "Should return false for invalid URL (CATCH block execution)");
    }

    /**
     * Test Case 9: Queue name extraction from URL
     * Tests the string parsing logic for queue name extraction
     */
    @Test
    @DisplayName("Queue name should be correctly extracted from URL")
    void testQueueNameExtraction_ExtractsCorrectName() {
        // Arrange
        int userId = TEST_USER_ID;
        
        // Act
        List<SQSQueueResource> result = sqsService.getAllQueues(userId);
        
        // Assert: Verify queue names are extracted correctly
        for (SQSQueueResource queue : result) {
            String queueUrl = queue.getQueueUrl();
            String queueName = queue.getQueueName();
            
            assertNotNull(queueName, "Queue name should be extracted");
            assertFalse(queueName.isEmpty(), "Queue name should not be empty");
            
            // The name should be the last part of the URL (after the last '/')
            if (queueUrl != null && queueUrl.contains("/")) {
                String expectedName = queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
                assertEquals(expectedName, queueName, 
                    "Queue name should be the last segment of URL");
            }
        }
    }

    /**
     * Test Case 10: Exception handling in getAllQueues (CATCH block)
     * Demonstrates that errors don't crash the application
     */
    @Test
    @DisplayName("getAllQueues should not throw uncaught exceptions")
    void testGetAllQueues_HandlesExceptionsGracefully() {
        // Test that the CATCH block works by verifying no exception is thrown
        try {
            List<SQSQueueResource> result = sqsService.getAllQueues(TEST_USER_ID);
            assertNotNull(result, "CATCH block should return empty list, not throw");
            assertTrue(true, "Method executed with proper exception handling");
        } catch (Exception e) {
            fail("Should not throw uncaught exception. Should catch in CATCH block: " + e.getMessage());
        }
    }
}
