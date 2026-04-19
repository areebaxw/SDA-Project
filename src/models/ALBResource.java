package models;

import java.time.LocalDateTime;

/**
 * ALBResource model class
 * Represents an AWS Application Load Balancer being monitored
 */
public class ALBResource {
    private int recordId;
    private String loadBalancerName;
    private String loadBalancerArn;
    private String dnsName;
    private String scheme;
    private String state;
    private long requestCount;
    private Boolean isIdle;
    private LocalDateTime lastChecked;
    private int userId;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public void setLoadBalancerName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    public String getLoadBalancerArn() {
        return loadBalancerArn;
    }

    public void setLoadBalancerArn(String loadBalancerArn) {
        this.loadBalancerArn = loadBalancerArn;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public Boolean isIdle() {
        return isIdle;
    }

    public void setIdle(Boolean idle) {
        isIdle = idle;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
