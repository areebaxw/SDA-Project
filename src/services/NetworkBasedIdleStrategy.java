package services;

/**
 * NetworkBasedIdleStrategy - Strategy implementation for network-based idle detection
 */
public class NetworkBasedIdleStrategy implements IdleDetectionStrategy {
    
    @Override
    public boolean isIdle(double cpuUtilization, double networkActivity, double threshold) {
        // Resource is idle if network activity is below threshold
        return networkActivity < threshold;
    }
}
