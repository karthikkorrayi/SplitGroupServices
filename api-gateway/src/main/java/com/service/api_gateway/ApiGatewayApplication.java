package com.service.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // This allows the gateway to discover other services via Eureka
public class ApiGatewayApplication {

	public static void main(String[] args) {
		System.out.println("Starting API Gateway...");
		SpringApplication.run(ApiGatewayApplication.class, args);
		System.out.println("API Gateway started successfully!");
		System.out.println("All API requests should now go through: http://localhost:8080");
		System.out.println("Available routes will be configured in application.yml");
	}
}