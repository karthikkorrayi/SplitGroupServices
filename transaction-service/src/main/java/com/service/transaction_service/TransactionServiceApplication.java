package com.service.transaction_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka for service discovery
public class TransactionServiceApplication {

	public static void main(String[] args) {
		System.out.println("Starting Transaction Service...");
		SpringApplication.run(TransactionServiceApplication.class, args);
		System.out.println("Transaction Service started successfully!");
		System.out.println("Service available at: http://localhost:8083");
		System.out.println("Access via API Gateway: http://localhost:8080/api/transactions/*");
		System.out.println("Ready to track expenses and split bills!");
	}
}