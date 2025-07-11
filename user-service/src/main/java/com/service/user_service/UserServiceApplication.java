package com.service.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

	public static void main(String[] args) {
		System.out.println("Starting Simple User Service...");
		SpringApplication.run(UserServiceApplication.class, args);
		System.out.println("User Service started successfully!");
		System.out.println("Service available at: http://localhost:8082");
		System.out.println("Access via API Gateway: http://localhost:8080/api/users/*");
	}
}