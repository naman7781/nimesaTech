package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

	List<Resource> findByService(String service);
    List<Resource> findByServiceAndIdentifierContaining(String service, String identifier);
}
