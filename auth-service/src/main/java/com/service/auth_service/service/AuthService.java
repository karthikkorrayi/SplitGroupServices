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

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

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

        // Save user to database
        User savedUser = userRepository.save(user);

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
}