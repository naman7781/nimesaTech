package com.example.demo.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Job;
import com.example.demo.entity.Resource;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.ResourceRepository;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class JobResourceService {

	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private ResourceRepository resourceRepository;
	
	private final Ec2Client EC2;
    private final S3Client S3;
    
    public JobResourceService(@Value("${aws.accessKeyId}") String accessKeyId, 
            @Value("${aws.secretAccessKey}") String secretAccessKey, 
            @Value("${aws.region}") String region) {
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
		this.EC2 = Ec2Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds)).region(Region.of(region)).build();
		this.S3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds)).region(Region.of(region)).build();
	}
	
    
    @Async
    public CompletableFuture<Void> discoverEC2Instances(Long jobId) {
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            DescribeInstancesResponse response = EC2.describeInstances(request);

            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    Resource resource = new Resource();
                    resource.setJobId(jobId);
                    resource.setService("EC2");
                    resource.setIdentifier(instance.instanceId());
                    resourceRepository.save(resource);
                }
            }

            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("SUCCESS");
            jobRepository.save(job);
        } catch (Exception e) {
            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("FAILED");
            jobRepository.save(job);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> discoverS3Buckets(Long jobId) {
        try {
            ListBucketsResponse response = S3.listBuckets();

            for (Bucket b : response.buckets()) {
                Resource resource = new Resource();
                resource.setJobId(jobId);
                resource.setService("S3");
                resource.setIdentifier(b.name());
                resourceRepository.save(resource);
            }

            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("SUCCESS");
            jobRepository.save(job);
        } catch (Exception e) {
            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("FAILED");
            jobRepository.save(job);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async
    public CompletableFuture<Void> discoverS3BucketObjects(Long jobId, String bucketName) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                                                               .bucket(bucketName)
                                                               .build();
            ListObjectsV2Response response = S3.listObjectsV2(request);

            for (S3Object s3 : response.contents()) {
                Resource resource = new Resource();
                resource.setJobId(jobId);
                resource.setService("S3");
                resource.setIdentifier(bucketName + "/" + s3.key());
                resourceRepository.save(resource);
            }

            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("SUCCESS");
            jobRepository.save(job);
        } catch (Exception e) {
            Job job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("FAILED");
            jobRepository.save(job);
        }
        return CompletableFuture.completedFuture(null);
    }
}
