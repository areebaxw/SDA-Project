package aws;

import dao.ALBDAO;
import models.ALBResource;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import java.util.ArrayList;
import java.util.List;

/**
 * ALBMonitoringService - AWS ALB monitoring wrapper
 */
public class ALBMonitoringService {
    private final ElasticLoadBalancingV2Client albClient;
    private final CloudWatchService cloudWatchService;

    public ALBMonitoringService() {
        this.albClient = AWSClientFactory.getInstance().getALBClient();
        this.cloudWatchService = new CloudWatchService();
    }

    public List<ALBResource> getAllALBs(int userId) {
        List<ALBResource> resources = new ArrayList<>();
        try {
            DescribeLoadBalancersResponse response = albClient.describeLoadBalancers(
                    DescribeLoadBalancersRequest.builder().build());

            for (LoadBalancer lb : response.loadBalancers()) {
                ALBResource resource = new ALBResource();
                resource.setLoadBalancerArn(lb.loadBalancerArn());
                resource.setLoadBalancerName(lb.loadBalancerName());
                resource.setDnsName(lb.dnsName());
                resource.setScheme(lb.schemeAsString());
                resource.setState(lb.state() != null ? lb.state().codeAsString() : "unknown");
                long requests = cloudWatchService.getALBRequestCount(lb.loadBalancerArn(), 7);
                resource.setRequestCount(requests);
                resource.setIdle(requests < 100);
                resource.setUserId(userId);
                resources.add(resource);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving ALBs: " + e.getMessage());
        }
        return resources;
    }

    public int syncFromAWS(int userId) {
        ALBDAO dao = new ALBDAO();
        int synced = 0;
        for (ALBResource alb : getAllALBs(userId)) {
            if (dao.saveOrUpdate(alb)) {
                synced++;
            }
        }
        return synced;
    }

    public boolean deleteALB(String loadBalancerArn) {
        try {
            albClient.deleteLoadBalancer(DeleteLoadBalancerRequest.builder()
                    .loadBalancerArn(loadBalancerArn)
                    .build());
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting ALB " + loadBalancerArn + ": " + e.getMessage());
            return false;
        }
    }
}
