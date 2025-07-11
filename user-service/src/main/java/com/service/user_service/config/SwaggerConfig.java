package com.service.user_service.config;

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
    public OpenAPI userServiceAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Direct User Service"),
                        new Server().url("http://localhost:8080/api/users").description("Via API Gateway")
                ))
                .info(new Info()
                        .title("User Service API")
                        .description("User Profile Management API for Split Group Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Split Group Team")
                                .email("support@splitgroup.com")));
    }
}