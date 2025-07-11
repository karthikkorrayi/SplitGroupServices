package com.service.transaction_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI transactionServiceAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Direct Transaction Service"),
                        new Server().url("http://localhost:8080/api/transactions").description("Via API Gateway")
                ))
                .info(new Info()
                        .title("Transaction Service API")
                        .description("Expense Tracking and Bill Splitting API for Split Group Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Split Group Team")
                                .email("support@splitgroup.com")));
    }
}