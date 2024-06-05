package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Job;
import com.example.demo.entity.Resource;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.ResourceRepository;
import com.example.demo.service.JobResourceService;

@RestController
@RequestMapping("/nmt")
public class JobResourceController {

	@Autowired
	private JobResourceService jobResourceService;
	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private ResourceRepository resourceRepository;
	
	 @PostMapping("/discoverServices")  //Task 1
	    public ResponseEntity<Long> discoverServices(@RequestBody List<String> services) {
	        Job job = new Job();
	        job.setStatus("IN_PROGRESS");
	        jobRepository.save(job);

	        Long jobId = job.getId();	        
	        for (String service : services) {
	            if (service.equalsIgnoreCase("EC2")) {
	            	jobResourceService.discoverEC2Instances(jobId);
	            } else if (service.equalsIgnoreCase("S3")) {
	            	jobResourceService.discoverS3Buckets(jobId);
	            }
	        }
	        return ResponseEntity.ok(jobId);
	   }
	 
	 	@GetMapping("/getJobResult/{jobId}")  //Task 2
	    public ResponseEntity<String> getJobResult(@PathVariable Long jobId) {
	        Job job = jobRepository.findById(jobId).orElseThrow();
	        return ResponseEntity.ok(job.getStatus());
	    }
	 	
	 	@GetMapping("/getDiscoveryResult/{service}") // Task 3
	    public ResponseEntity<List<String>> getDiscoveryResult(@PathVariable String service) {
	        List<Resource> resources = resourceRepository.findByService(service.toUpperCase());
	        List<String> identifiers = resources.stream().map(Resource::getIdentifier).collect(Collectors.toList());
	        return ResponseEntity.ok(identifiers);
	    }
	 	
	 	@PostMapping("/getS3BucketObjects")  //Task 4
	 	public ResponseEntity<Long> getS3BucketObjects(@RequestBody Map<String, String> request) {
	 	    String bucketName = request.get("bucketName");
	 	    
	 	    if (bucketName == null) {
	 	        return ResponseEntity.badRequest().body(null); 
	 	    }
	 	    
	 	    Job job = new Job();
	 	    job.setService("S3");
	 	    job.setStatus("IN_PROGRESS");
	 	    jobRepository.save(job);

	 	    Long jobId = job.getId();
	 	    jobResourceService.discoverS3BucketObjects(jobId, bucketName);

	 	    return ResponseEntity.ok(jobId);
	 	}
	 	
	 	@GetMapping("/getS3BucketObjectCount/{bucketName}")  //Task 5
	    public ResponseEntity<Long> getS3BucketObjectCount(@PathVariable String bucketName) {
	        List<Resource> resources = resourceRepository.findByServiceAndIdentifierContaining("S3", bucketName);
	        return ResponseEntity.ok((long) resources.size());
	    }

	    @GetMapping("/getS3BucketObjectLike")  //Task 6
	    public ResponseEntity<List<String>> getS3BucketObjectLike(@RequestParam String bucketName, @RequestParam String pattern) {
	        List<Resource> resources = resourceRepository.findByServiceAndIdentifierContaining("S3", bucketName + "/" + pattern);
	        List<String> identifiers = resources.stream().map(Resource::getIdentifier).collect(Collectors.toList());
	        return ResponseEntity.ok(identifiers);
	    }
}
