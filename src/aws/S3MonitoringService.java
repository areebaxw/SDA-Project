package aws;

import dao.S3BucketDAO;
import models.S3BucketResource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * S3MonitoringService - AWS S3 monitoring wrapper
 */
public class S3MonitoringService {
    private final S3Client s3Client;

    public S3MonitoringService() {
        this.s3Client = AWSClientFactory.getInstance().getS3Client();
    }

    public List<S3BucketResource> getAllBuckets(int userId) {
        List<S3BucketResource> result = new ArrayList<>();
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            for (Bucket bucket : response.buckets()) {
                S3BucketResource resource = new S3BucketResource();
                resource.setBucketName(bucket.name());
                resource.setBucketArn("arn:aws:s3:::" + bucket.name());
                resource.setRegion(resolveBucketRegion(bucket.name()));

                long totalObjects = 0;
                long totalBytes = 0;
                try {
                    ListObjectsV2Request req = ListObjectsV2Request.builder()
                            .bucket(bucket.name())
                            .build();

                    for (ListObjectsV2Response page : s3Client.listObjectsV2Paginator(req)) {
                        totalObjects += page.contents().size();
                        for (var obj : page.contents()) {
                            totalBytes += obj.size();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Unable to list objects for bucket " + bucket.name() + ": " + e.getMessage());
                }

                resource.setObjectCount(totalObjects);
                resource.setTotalSizeGb(bytesToGb(totalBytes));
                resource.setIsPublic(false);
                resource.setIdle(totalObjects == 0);
                resource.setUserId(userId);
                result.add(resource);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving S3 buckets: " + e.getMessage());
        }
        return result;
    }

    public int syncFromAWS(int userId) {
        S3BucketDAO dao = new S3BucketDAO();
        int synced = 0;
        for (S3BucketResource bucket : getAllBuckets(userId)) {
            if (dao.saveOrUpdate(bucket)) {
                synced++;
            }
        }
        return synced;
    }

    public boolean deleteBucket(String bucketName) {
        try {
            s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting S3 bucket " + bucketName + ": " + e.getMessage());
            return false;
        }
    }

    private String resolveBucketRegion(String bucketName) {
        try {
            GetBucketLocationResponse response = s3Client.getBucketLocation(
                    GetBucketLocationRequest.builder().bucket(bucketName).build());
            String value = response.locationConstraintAsString();
            return (value == null || value.isBlank()) ? "us-east-1" : value;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private double bytesToGb(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }
}
