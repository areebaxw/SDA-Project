package services;

/**
 * IdleDetectionStrategy - Strategy Pattern Interface
 * Defines different strategies for detecting idle resources
 */
public interface IdleDetectionStrategy {
    /**
     * Check if a resource is idle based on metrics
     * @param cpuUtilization CPU utilization percentage
     * @param networkActivity Network activity (or other metric)
     * @param threshold Threshold value for comparison
     * @return true if resource is considered idle
     */
    boolean isIdle(double cpuUtilization, double networkActivity, double threshold);
}
