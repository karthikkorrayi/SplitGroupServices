package com.service.balance_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka for service discovery
public class BalanceServiceApplication {

	public static void main(String[] args) {
		System.out.println("Starting Balance Service...");
		SpringApplication.run(BalanceServiceApplication.class, args);
		System.out.println("Balance Service started successfully!");
		System.out.println("Service available at: http://localhost:8084");
		System.out.println("Access via API Gateway: http://localhost:8080/api/balances/*");
		System.out.println("Ready to manage balances and settlements!");
	}
}