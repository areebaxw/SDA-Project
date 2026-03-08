package models;

import java.time.LocalDateTime;

/**
 * SQSQueueResource model class
 * Represents an AWS SQS queue being monitored
 */
public class SQSQueueResource {
    private int recordId;
    private String queueName;
    private String queueUrl;
    private String queueArn;
    private long messageCount;
    private long delayedMessageCount;
    private Boolean isIdle;
    private LocalDateTime lastChecked;
    private int userId;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public String getQueueArn() {
        return queueArn;
    }

    public void setQueueArn(String queueArn) {
        this.queueArn = queueArn;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public long getDelayedMessageCount() {
        return delayedMessageCount;
    }

    public void setDelayedMessageCount(long delayedMessageCount) {
        this.delayedMessageCount = delayedMessageCount;
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
