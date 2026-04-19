package services;

import aws.CloudWatchService;
import dao.ALBDAO;
import dao.EC2DAO;
import dao.S3BucketDAO;
import dao.SQSQueueDAO;
import models.ALBResource;
import models.Alert;
import models.EC2Instance;
import models.S3BucketResource;
import models.SQSQueueResource;

import java.util.List;

/**
 * IdleDetectionService - Detects idle resources for the new monitoring scope.
 */
public class IdleDetectionService {
    private IdleDetectionStrategy strategy;
    private final CloudWatchService cloudWatchService;
    private final EC2DAO ec2DAO;
    private final S3BucketDAO s3BucketDAO;
    private final SQSQueueDAO sqsQueueDAO;
    private final ALBDAO albDAO;
    private final AlertService alertService;

    public IdleDetectionService() {
        this.strategy = new CPUBasedIdleStrategy();
        this.cloudWatchService = new CloudWatchService();
        this.ec2DAO = new EC2DAO();
        this.s3BucketDAO = new S3BucketDAO();
        this.sqsQueueDAO = new SQSQueueDAO();
        this.albDAO = new ALBDAO();
        this.alertService = AlertService.getInstance();
    }

    public void setStrategy(IdleDetectionStrategy strategy) {
        this.strategy = strategy;
    }

    public void detectIdleEC2Instances(int daysBack, double cpuThreshold) {
        List<EC2Instance> instances = ec2DAO.getAllEC2Instances();
        for (EC2Instance instance : instances) {
            if (!"running".equalsIgnoreCase(instance.getInstanceState())) {
                continue;
            }

            double cpuUtilization = cloudWatchService.getEC2CPUUtilization(instance.getInstanceId(), daysBack);
            double networkIn = cloudWatchService.getEC2NetworkIn(instance.getInstanceId(), daysBack);
            boolean isIdle = strategy.isIdle(cpuUtilization, networkIn, cpuThreshold);

            instance.setCpuUtilization(cpuUtilization);
            instance.setNetworkIn(networkIn);
            instance.setIdle(isIdle);
            ec2DAO.saveOrUpdateEC2Instance(instance);

            if (isIdle) {
                createIdleAlert(instance.getInstanceId(), "EC2",
                        String.format("EC2 %s appears idle (CPU %.2f%%, NetworkIn %.2f)",
                                instance.getInstanceId(), cpuUtilization, networkIn));
            }
        }
    }

    public void detectIdleS3Buckets(long objectThreshold) {
        List<S3BucketResource> buckets = s3BucketDAO.getAll();
        for (S3BucketResource bucket : buckets) {
            boolean isIdle = bucket.getObjectCount() <= objectThreshold;
            bucket.setIdle(isIdle);
            s3BucketDAO.saveOrUpdate(bucket);

            if (isIdle) {
                createIdleAlert(bucket.getBucketName(), "S3",
                        String.format("S3 bucket %s appears idle (object count %d)",
                                bucket.getBucketName(), bucket.getObjectCount()));
            }
        }
    }

    public void detectIdleSQSQueues(long messageThreshold) {
        List<SQSQueueResource> queues = sqsQueueDAO.getAll();
        for (SQSQueueResource queue : queues) {
            long total = queue.getMessageCount() + queue.getDelayedMessageCount();
            boolean isIdle = total <= messageThreshold;
            queue.setIdle(isIdle);
            sqsQueueDAO.saveOrUpdate(queue);

            if (isIdle) {
                createIdleAlert(queue.getQueueName(), "SQS",
                        String.format("SQS queue %s appears idle (messages %d)", queue.getQueueName(), total));
            }
        }
    }

    public void detectIdleALBs(long requestThreshold) {
        List<ALBResource> albs = albDAO.getAll();
        for (ALBResource alb : albs) {
            boolean isIdle = alb.getRequestCount() <= requestThreshold;
            alb.setIdle(isIdle);
            albDAO.saveOrUpdate(alb);

            if (isIdle) {
                createIdleAlert(alb.getLoadBalancerName(), "ALB",
                        String.format("ALB %s appears idle (requests %d)", alb.getLoadBalancerName(), alb.getRequestCount()));
            }
        }
    }

    public void runCompleteIdleDetection() {
        detectIdleEC2Instances(7, 5.0);
        detectIdleS3Buckets(0);
        detectIdleSQSQueues(0);
        detectIdleALBs(100);
    }

    private void createIdleAlert(String resourceId, String type, String message) {
        Alert alert = new Alert(resourceId, type, "IDLE_RESOURCE", "medium", message);
        alertService.createAlert(alert);
    }
}
