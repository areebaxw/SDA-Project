package services;

/**
 * CombinedIdleStrategy - Strategy implementation combining CPU and network metrics
 */
public class CombinedIdleStrategy implements IdleDetectionStrategy {
    
    @Override
    public boolean isIdle(double cpuUtilization, double networkActivity, double threshold) {
        // Resource is idle if BOTH CPU and network activity are below threshold
        return cpuUtilization < threshold && networkActivity < threshold;
    }
}
