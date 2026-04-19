package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White-box tests for CPUBasedIdleStrategy
 * Tests both the if (idle) and else (active) branches
 */
@DisplayName("CPU-Based Idle Strategy Tests")
public class CPUBasedIdleStrategyTest {
    
    private CPUBasedIdleStrategy strategy;
    private static final double THRESHOLD = 5.0; // 5% CPU threshold

    @BeforeEach
    void setUp() {
        strategy = new CPUBasedIdleStrategy();
    }

    /**
     * Test Case 1: CPU below threshold - Should return true (is idle)
     * This tests the IF branch: return cpuUtilization < threshold
     */
    @Test
    @DisplayName("Should return true when CPU utilization is below threshold")
    void testIsIdle_WhenCPUBelowThreshold_ShouldReturnTrue() {
        // Arrange
        double cpuUtilization = 2.5; // 2.5% < 5%
        double networkActivity = 100.0;
        
        // Act
        boolean result = strategy.isIdle(cpuUtilization, networkActivity, THRESHOLD);
        
        // Assert
        assertTrue(result, "Strategy should return true when CPU is below threshold");
    }

    /**
     * Test Case 2: CPU at threshold boundary - Should return false (not idle)
     * This tests the ELSE branch: return cpuUtilization < threshold
     */
    @Test
    @DisplayName("Should return false when CPU utilization is equal to threshold")
    void testIsIdle_WhenCPUEqualToThreshold_ShouldReturnFalse() {
        // Arrange
        double cpuUtilization = 5.0; // 5% = 5% (boundary)
        double networkActivity = 100.0;
        
        // Act
        boolean result = strategy.isIdle(cpuUtilization, networkActivity, THRESHOLD);
        
        // Assert
        assertFalse(result, "Strategy should return false when CPU equals threshold");
    }

    /**
     * Test Case 3: CPU above threshold - Should return false (not idle)
     * This tests the ELSE branch with high CPU
     */
    @Test
    @DisplayName("Should return false when CPU utilization is above threshold")
    void testIsIdle_WhenCPUAboveThreshold_ShouldReturnFalse() {
        // Arrange
        double cpuUtilization = 50.0; // 50% > 5%
        double networkActivity = 100.0;
        
        // Act
        boolean result = strategy.isIdle(cpuUtilization, networkActivity, THRESHOLD);
        
        // Assert
        assertFalse(result, "Strategy should return false when CPU is above threshold");
    }

    /**
     * Test Case 4: CPU just below threshold - Boundary value analysis
     * Tests behavior very close to the boundary
     */
    @Test
    @DisplayName("Should return true when CPU is just below threshold (boundary)")
    void testIsIdle_WhenCPUJustBelowThreshold_ShouldReturnTrue() {
        // Arrange
        double cpuUtilization = 4.99; // Just below 5%
        double networkActivity = 100.0;
        
        // Act
        boolean result = strategy.isIdle(cpuUtilization, networkActivity, THRESHOLD);
        
        // Assert
        assertTrue(result, "Strategy should return true when CPU is just below threshold");
    }

    /**
     * Test Case 5: Zero CPU utilization - Minimum valid value
     * Tests with minimum realistic CPU usage
     */
    @Test
    @DisplayName("Should return true when CPU utilization is zero")
    void testIsIdle_WhenCPUIsZero_ShouldReturnTrue() {
        // Arrange
        double cpuUtilization = 0.0;
        double networkActivity = 0.0;
        
        // Act
        boolean result = strategy.isIdle(cpuUtilization, networkActivity, THRESHOLD);
        
        // Assert
        assertTrue(result, "Strategy should return true when CPU is zero");
    }

    /**
     * Test Case 6: Very high CPU utilization - Maximum realistic value
     * Tests with high CPU usage
     */
    @Test
    @DisplayName("Should return false when CPU utilization is very high")
    void testIsIdle_WhenCPUIsVeryHigh_ShouldReturnFalse() {
        // Arrange
        double cpuUtilization = 99.5;
        double networkActivity = 1000.0;
        
        // Act
        boolean result = strategy.isIdle(cpuUtilization, networkActivity, THRESHOLD);
        
        // Assert
        assertFalse(result, "Strategy should return false when CPU is very high");
    }

    /**
     * Test Case 7: Network activity is ignored in CPU-based strategy
     * Ensures network parameter doesn't affect idle detection
     */
    @Test
    @DisplayName("Should ignore network activity when determining idle status")
    void testIsIdle_NetworkActivityIgnored_ShouldDependOnlyOnCPU() {
        // Arrange - Low CPU with high network activity
        double cpuUtilization = 2.0;
        double highNetworkActivity = 5000.0;
        
        // Act
        boolean result = strategy.isIdle(cpuUtilization, highNetworkActivity, THRESHOLD);
        
        // Assert - Should still be idle because CPU is low
        assertTrue(result, "Network activity should not affect CPU-based idle detection");
    }
}
