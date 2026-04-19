package aws;

import dao.SQSQueueDAO;
import models.SQSQueueResource;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQSMonitoringService - AWS SQS monitoring wrapper
 */
public class SQSMonitoringService {
    private final SqsClient sqsClient;

    public SQSMonitoringService() {
        this.sqsClient = AWSClientFactory.getInstance().getSQSClient();
    }

    public List<SQSQueueResource> getAllQueues(int userId) {
        List<SQSQueueResource> queues = new ArrayList<>();
        try {
            ListQueuesResponse listResponse = sqsClient.listQueues();
            for (String queueUrl : listResponse.queueUrls()) {
                GetQueueAttributesResponse attrResponse = sqsClient.getQueueAttributes(
                        GetQueueAttributesRequest.builder()
                                .queueUrl(queueUrl)
                                .attributeNames(
                                        QueueAttributeName.QUEUE_ARN,
                                        QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                                        QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_DELAYED
                                )
                                .build());

                Map<QueueAttributeName, String> attrs = attrResponse.attributes();

                String arn = attrs.getOrDefault(QueueAttributeName.QUEUE_ARN, "");
                long msgCount = parseLong(attrs.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES));
                long delayed = parseLong(attrs.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_DELAYED));

                SQSQueueResource queue = new SQSQueueResource();
                queue.setQueueUrl(queueUrl);
                queue.setQueueArn(arn);
                queue.setQueueName(queueUrl.substring(queueUrl.lastIndexOf('/') + 1));
                queue.setMessageCount(msgCount);
                queue.setDelayedMessageCount(delayed);
                queue.setIdle(msgCount == 0 && delayed == 0);
                queue.setUserId(userId);
                queues.add(queue);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving SQS queues: " + e.getMessage());
        }
        return queues;
    }

    public int syncFromAWS(int userId) {
        SQSQueueDAO dao = new SQSQueueDAO();
        int synced = 0;
        for (SQSQueueResource queue : getAllQueues(userId)) {
            if (dao.saveOrUpdate(queue)) {
                synced++;
            }
        }
        return synced;
    }

    public boolean purgeQueue(String queueUrl) {
        try {
            sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(queueUrl).build());
            return true;
        } catch (Exception e) {
            System.err.println("Error purging SQS queue " + queueUrl + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteQueue(String queueUrl) {
        try {
            sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build());
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting SQS queue " + queueUrl + ": " + e.getMessage());
            return false;
        }
    }

    private long parseLong(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
