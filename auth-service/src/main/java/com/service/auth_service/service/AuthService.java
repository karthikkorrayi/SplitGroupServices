package com.service.auth_service.service;

import com.service.auth_service.dto.AuthResponse;
import com.service.auth_service.dto.LoginRequest;
import com.service.auth_service.dto.RegisterRequest;
import com.service.auth_service.entity.User;
import com.service.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service";

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim()); // Normalize email
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
        user.setName(request.getName().trim());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Save user to database
        User savedUser = userRepository.save(user);

        // Automatically create user profile in User Service
        createUserProfile(savedUser);

        // Generate JWT token
        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getName()
        );

        return new AuthResponse(token, savedUser.getEmail(), savedUser.getName(), savedUser.getId());
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getName()
        );

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getId());
    }

    public AuthResponse validateToken(String token) {
        try {
            // Extract user information from token
            String email = jwtService.extractEmail(token);
            Long userId = jwtService.extractUserId(token);
            String name = jwtService.extractName(token);

            // Verify token is still valid
            if (!jwtService.isTokenValid(token, email)) {
                throw new RuntimeException("Token is expired or invalid");
            }

            // Optionally verify user still exists in database
            userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return new AuthResponse(token, email, name, userId);

        } catch (Exception e) {
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public long getTotalUserCount() {
        return userRepository.countAllUsers();
    }

    // Create user profile in User Service
    private void createUserProfile(User user) {
        try {
            UserProfileRequest profileRequest = new UserProfileRequest();
            profileRequest.setUserId(user.getId());
            profileRequest.setEmail(user.getEmail());
            profileRequest.setName(user.getName());

            String url = USER_SERVICE_URL + "/profiles";
            restTemplate.postForObject(url, profileRequest, Object.class);

            System.out.println("User profile created for user: " + user.getId());
        } catch (Exception e) {
            System.err.println("Failed to create user profile: " + e.getMessage());
            // Log error but don't fail registration
        }
    }

    // NEW: DTO for User Service communication
    public static class UserProfileRequest {
        private Long userId;
        private String email;
        private String name;

        public UserProfileRequest() {}

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}