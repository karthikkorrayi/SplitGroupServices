package com.service.auth_service.config;

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
    public OpenAPI authServiceAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Direct Auth Service"),
                        new Server().url("http://localhost:8080/api/auth").description("Via API Gateway")
                ))
                .info(new Info()
                        .title("Auth Service API")
                        .description("Authentication and User Management API for Split Group Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Split Group Team")
                                .email("support@splitgroup.com")));
    }
}