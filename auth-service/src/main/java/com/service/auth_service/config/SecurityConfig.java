package com.service.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection (not needed for stateless JWT-based auth)
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Allow public access to authentication endpoints
                        .requestMatchers("/register", "/login", "/health", "/info").permitAll()

                        // Allow public access to user endpoints (for now - we'll secure this later)
                        .requestMatchers("/users/**").permitAll()

                        // Allow public access to validation endpoint (for other services)
                        .requestMatchers("/validate").permitAll()

                        // Allow public access to actuator endpoints (for monitoring)
                        .requestMatchers("/actuator/**").permitAll()

                        // Allow public access to Swagger endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Configure session management (stateless for microservices)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 is good balance of security and performance
    }
}