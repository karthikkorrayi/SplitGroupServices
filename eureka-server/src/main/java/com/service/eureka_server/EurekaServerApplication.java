package com.service.eureka_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer  // This annotation makes this application a Eureka Server
public class EurekaServerApplication {

	public static void main(String[] args)  {
		System.out.println("Starting Eureka Server...");
		SpringApplication.run(EurekaServerApplication.class, args);
		System.out.println("Eureka Server started successfully!");
		System.out.println("Access Eureka Dashboard at: http://localhost:8761");
	}

}
