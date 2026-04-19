package services;

import aws.CloudWatchService;
import dao.ALBDAO;
import dao.EC2DAO;
import dao.RuleDAO;
import dao.S3BucketDAO;
import dao.SQSQueueDAO;
import models.ALBResource;
import models.Alert;
import models.EC2Instance;
import models.Rule;
import models.S3BucketResource;
import models.SQSQueueResource;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RuleEvaluationService - Evaluates active governance rules and creates alerts.
 */
public class RuleEvaluationService {
    private final RuleDAO ruleDAO;
    private final AlertService alertService;
    private final EC2DAO ec2DAO;
    private final S3BucketDAO s3BucketDAO;
    private final SQSQueueDAO sqsQueueDAO;
    private final ALBDAO albDAO;
    private final CloudWatchService cloudWatchService;

    public RuleEvaluationService() {
        this.ruleDAO = new RuleDAO();
        this.alertService = AlertService.getInstance();
        this.ec2DAO = new EC2DAO();
        this.s3BucketDAO = new S3BucketDAO();
        this.sqsQueueDAO = new SQSQueueDAO();
        this.albDAO = new ALBDAO();
        this.cloudWatchService = new CloudWatchService();
    }

    public int evaluateAllRules() {
        int alertsCreated = 0;
        List<Rule> activeRules = ruleDAO.getAllActiveRules();
        for (Rule rule : activeRules) {
            try {
                alertsCreated += evaluateRule(rule);
            } catch (Exception e) {
                System.err.println("Error evaluating rule " + rule.getRuleName() + ": " + e.getMessage());
            }
        }
        return alertsCreated;
    }

    private int evaluateRule(Rule rule) {
        switch (rule.getResourceType()) {
            case "EC2":
                return evaluateEC2Rules(rule);
            case "S3":
                return evaluateS3Rules(rule);
            case "SQS":
                return evaluateSQSRules(rule);
            case "ALB":
                return evaluateALBRules(rule);
            default:
                System.out.println("Unsupported resource type in rule: " + rule.getResourceType());
                return 0;
        }
    }

    private int evaluateEC2Rules(Rule rule) {
        int created = 0;
        String metric = normalizeMetric(rule.getConditionMetric());
        int hours = convertToHours(rule.getConditionDuration(), rule.getDurationUnit());

        List<EC2Instance> instances = ec2DAO.getAllEC2Instances();
        for (EC2Instance instance : instances) {
            double actual;
            switch (metric) {
                case "CPUUTILIZATION":
                    actual = cloudWatchService.getEC2CPUUtilization(instance.getInstanceId(), hours);
                    break;
                case "NETWORKIN":
                    actual = cloudWatchService.getEC2NetworkIn(instance.getInstanceId(), hours);
                    break;
                case "NETWORKOUT":
                    actual = cloudWatchService.getEC2NetworkOut(instance.getInstanceId(), hours);
                    break;
                default:
                    continue;
            }

            if (evaluateCondition(actual, rule.getConditionOperator(), rule.getConditionValue())) {
                boolean ok = createAlert(rule, instance.getInstanceId(), "EC2",
                        String.format("EC2 %s %s is %.2f", instance.getInstanceId(), prettyMetricName(metric), actual));
                if (ok) created++;
            }
        }
        return created;
    }

    private int evaluateS3Rules(Rule rule) {
        int created = 0;
        String metric = normalizeMetric(rule.getConditionMetric());

        List<S3BucketResource> buckets = s3BucketDAO.getAll();
        for (S3BucketResource bucket : buckets) {
            double actual;
            switch (metric) {
                case "OBJECTCOUNT":
                    actual = bucket.getObjectCount();
                    break;
                case "TOTALSIZEGB":
                case "SIZEGB":
                    actual = bucket.getTotalSizeGb();
                    break;
                case "ISPUBLIC":
                    actual = Boolean.TRUE.equals(bucket.getIsPublic()) ? 1.0 : 0.0;
                    break;
                default:
                    continue;
            }

            if (evaluateCondition(actual, rule.getConditionOperator(), rule.getConditionValue())) {
                boolean ok = createAlert(rule, bucket.getBucketName(), "S3",
                        String.format("S3 bucket %s %s is %.2f", bucket.getBucketName(), prettyMetricName(metric), actual));
                if (ok) created++;
            }
        }
        return created;
    }

    private int evaluateSQSRules(Rule rule) {
        int created = 0;
        String metric = normalizeMetric(rule.getConditionMetric());

        List<SQSQueueResource> queues = sqsQueueDAO.getAll();
        for (SQSQueueResource queue : queues) {
            double actual;
            switch (metric) {
                case "MESSAGECOUNT":
                    actual = queue.getMessageCount();
                    break;
                case "DELAYEDMESSAGECOUNT":
                    actual = queue.getDelayedMessageCount();
                    break;
                case "TOTALMESSAGES":
                    actual = queue.getMessageCount() + queue.getDelayedMessageCount();
                    break;
                default:
                    continue;
            }

            if (evaluateCondition(actual, rule.getConditionOperator(), rule.getConditionValue())) {
                boolean ok = createAlert(rule, queue.getQueueName(), "SQS",
                        String.format("SQS queue %s %s is %.0f", queue.getQueueName(), prettyMetricName(metric), actual));
                if (ok) created++;
            }
        }
        return created;
    }

    private int evaluateALBRules(Rule rule) {
        int created = 0;
        String metric = normalizeMetric(rule.getConditionMetric());
        int hours = convertToHours(rule.getConditionDuration(), rule.getDurationUnit());

        List<ALBResource> albs = albDAO.getAll();
        for (ALBResource alb : albs) {
            double actual;
            switch (metric) {
                case "REQUESTCOUNT":
                    actual = cloudWatchService.getALBRequestCount(alb.getLoadBalancerArn(), Math.max(1, hours / 24));
                    break;
                default:
                    continue;
            }

            if (evaluateCondition(actual, rule.getConditionOperator(), rule.getConditionValue())) {
                boolean ok = createAlert(rule, alb.getLoadBalancerName(), "ALB",
                        String.format("ALB %s %s is %.0f", alb.getLoadBalancerName(), prettyMetricName(metric), actual));
                if (ok) created++;
            }
        }
        return created;
    }

    private int convertToHours(int duration, String unit) {
        if (unit == null || "hours".equalsIgnoreCase(unit)) {
            return duration;
        }
        if ("days".equalsIgnoreCase(unit)) {
            return duration * 24;
        }
        if ("minutes".equalsIgnoreCase(unit)) {
            return Math.max(1, duration / 60);
        }
        return duration;
    }

    private boolean evaluateCondition(double actualValue, String operator, double threshold) {
        switch (operator) {
            case "<":
                return actualValue < threshold;
            case ">":
                return actualValue > threshold;
            case "=":
                return Math.abs(actualValue - threshold) < 0.01;
            case "<=":
                return actualValue <= threshold;
            case ">=":
                return actualValue >= threshold;
            default:
                return false;
        }
    }

    private boolean createAlert(Rule rule, String resourceId, String resourceType, String message) {
        Alert alert = new Alert();
        alert.setResourceId(resourceId);
        alert.setResourceType(resourceType);
        alert.setAlertType("RULE_BREACH");
        alert.setSeverity(determineSeverity(rule));
        alert.setMessage("[" + rule.getActionType() + "] " + message);
        alert.setRuleId(rule.getRuleId());
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());

        return alertService.createAlert(alert);
    }

    private String normalizeMetric(String metric) {
        if (metric == null) return "";
        return metric.trim().replace(" ", "").replace("_", "").toUpperCase();
    }

    private String prettyMetricName(String metric) {
        switch (metric) {
            case "CPUUTILIZATION": return "CPU Utilization";
            case "NETWORKIN": return "Network In";
            case "NETWORKOUT": return "Network Out";
            case "OBJECTCOUNT": return "Object Count";
            case "TOTALSIZEGB":
            case "SIZEGB": return "Total Size (GB)";
            case "ISPUBLIC": return "Public Flag";
            case "MESSAGECOUNT": return "Message Count";
            case "DELAYEDMESSAGECOUNT": return "Delayed Message Count";
            case "TOTALMESSAGES": return "Total Messages";
            case "REQUESTCOUNT": return "Request Count";
            default: return metric;
        }
    }

    private String determineSeverity(Rule rule) {
        switch (rule.getRuleType()) {
            case "security":
                return "HIGH";
            case "cost_optimization":
                return "MEDIUM";
            case "performance":
                return "MEDIUM";
            case "resource_optimization":
                return "LOW";
            default:
                return "LOW";
        }
    }
}
