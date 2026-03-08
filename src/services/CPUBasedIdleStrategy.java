package services;

/**
 * CPUBasedIdleStrategy - Strategy implementation for CPU-based idle detection
 */
public class CPUBasedIdleStrategy implements IdleDetectionStrategy {
    
    @Override
    public boolean isIdle(double cpuUtilization, double networkActivity, double threshold) {
        // Resource is idle if CPU utilization is below threshold
        return cpuUtilization < threshold;
    }
}
