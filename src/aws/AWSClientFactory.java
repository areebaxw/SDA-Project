package aws;
//comment1
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sts.StsClient;

/**
 * AWSClientFactory - Factory Pattern for AWS Client Creation
 * Singleton pattern to manage AWS SDK clients
 */
public class AWSClientFactory {
    private static AWSClientFactory instance;
    
    private String accessKey;
    private String secretKey;
    private Region region;
    
    private Ec2Client ec2Client;
    private S3Client s3Client;
    private SqsClient sqsClient;
    private ElasticLoadBalancingV2Client elbv2Client;
    private CloudWatchClient cloudWatchClient;
    private CostExplorerClient costExplorerClient;
    private StsClient stsClient;
    
    /**
     * Private constructor for Singleton pattern
     */
    private AWSClientFactory() {}
    
    /**
     * Get singleton instance
     */
    public static synchronized AWSClientFactory getInstance() {
        if (instance == null) {
            instance = new AWSClientFactory();
        }
        return instance;
    }
    
    /**
     * Initialize AWS credentials
     */
    public void initializeCredentials(String accessKey, String secretKey, String regionStr) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = Region.of(regionStr);
        
        // Close existing clients if any
        closeAllClients();
    }
    
    /**
     * Get credentials provider
     */
    private StaticCredentialsProvider getCredentialsProvider() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        return StaticCredentialsProvider.create(awsCreds);
    }
    
    /**
     * Factory Method: Get EC2 Client
     */
    public Ec2Client getEC2Client() {
        if (ec2Client == null) {
            ec2Client = Ec2Client.builder()
                    .region(region)
                    .credentialsProvider(getCredentialsProvider())
                    .build();
        }
        return ec2Client;
    }
    
    /**
     * Factory Method: Get S3 Client
     */
    public S3Client getS3Client() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .region(region)
                    .credentialsProvider(getCredentialsProvider())
                    .build();
        }
        return s3Client;
    }
    
    /**
     * Factory Method: Get SQS Client
     */
    public SqsClient getSQSClient() {
        if (sqsClient == null) {
            sqsClient = SqsClient.builder()
                    .region(region)
                    .credentialsProvider(getCredentialsProvider())
                    .build();
        }
        return sqsClient;
    }
    
    /**
     * Factory Method: Get ALB v2 Client
     */
    public ElasticLoadBalancingV2Client getALBClient() {
        if (elbv2Client == null) {
            elbv2Client = ElasticLoadBalancingV2Client.builder()
                    .region(region)
                    .credentialsProvider(getCredentialsProvider())
                    .build();
        }
        return elbv2Client;
    }
    
    /**
     * Factory Method: Get CloudWatch Client
     */
    public CloudWatchClient getCloudWatchClient() {
        if (cloudWatchClient == null) {
            cloudWatchClient = CloudWatchClient.builder()
                    .region(region)
                    .credentialsProvider(getCredentialsProvider())
                    .build();
        }
        return cloudWatchClient;
    }
    
    /**
     * Factory Method: Get Cost Explorer Client
     */
    public CostExplorerClient getCostExplorerClient() {
        if (costExplorerClient == null) {
            // Cost Explorer is only available in us-east-1
            costExplorerClient = CostExplorerClient.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(getCredentialsProvider())
                    .build();
        }
        return costExplorerClient;
    }
    
    /**
     * Factory Method: Get STS Client (for credential validation)
     */
    public StsClient getSTSClient() {
        if (stsClient == null) {
            stsClient = StsClient.builder()
                    .region(region)
                    .credentialsProvider(getCredentialsProvider())
                    .build();
        }
        return stsClient;
    }
    
    /**
     * Validate credentials using STS
     */
    public boolean validateCredentials() {
        try {
            StsClient client = getSTSClient();
            client.getCallerIdentity();
            System.out.println("AWS credentials validated successfully!");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to validate AWS credentials: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Close all AWS clients
     */
    public void closeAllClients() {
        if (ec2Client != null) {
            ec2Client.close();
            ec2Client = null;
        }
        if (s3Client != null) {
            s3Client.close();
            s3Client = null;
        }
        if (sqsClient != null) {
            sqsClient.close();
            sqsClient = null;
        }
        if (elbv2Client != null) {
            elbv2Client.close();
            elbv2Client = null;
        }
        if (cloudWatchClient != null) {
            cloudWatchClient.close();
            cloudWatchClient = null;
        }
        if (costExplorerClient != null) {
            costExplorerClient.close();
            costExplorerClient = null;
        }
        if (stsClient != null) {
            stsClient.close();
            stsClient = null;
        }
    }
    
    /**
     * Check if credentials are initialized
     */
    public boolean isInitialized() {
        return accessKey != null && secretKey != null && region != null;
    }
}
