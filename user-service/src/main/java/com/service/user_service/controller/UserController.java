package com.service.user_service.controller;

import com.service.user_service.dto.UpdateProfileRequest;
import com.service.user_service.dto.UserProfileRequest;
import com.service.user_service.dto.UserProfileResponse;
import com.service.user_service.service.AuthClientService;
import com.service.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")  // Base path (API Gateway strips /api/users)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthClientService authClientService;

    @PostMapping("/profiles")
    public ResponseEntity<?> createProfile(@Valid @RequestBody UserProfileRequest request) {
        try {
            UserProfileResponse response = userService.createProfile(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/profiles/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Check privacy permissions
            Long requesterId = null;
            if (authHeader != null) {
                try {
                    requesterId = authClientService.extractUserIdFromToken(authHeader);
                } catch (Exception e) {
                    // If token is invalid, treat as anonymous request
                }
            }

            // Check if user can access this profile
            if (requesterId == null || !userService.canAccessProfile(userId, requesterId)) {
                // For now, allow access to all profiles (we can restrict later)
                // return ResponseEntity.status(HttpStatus.FORBIDDEN)
                //         .body(new ErrorResponse("Access denied to this profile"));
            }

            UserProfileResponse response = userService.getProfile(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/profiles/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId,
                                           @Valid @RequestBody UpdateProfileRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and get requester ID
            Long requesterId = authClientService.extractUserIdFromToken(authHeader);

            // Check if user is updating their own profile
            if (!userId.equals(requesterId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("You can only update your own profile"));
            }

            UserProfileResponse response = userService.updateProfile(userId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/search/email")
    public ResponseEntity<List<UserProfileResponse>> searchByEmail(@RequestParam("q") String email) {
        List<UserProfileResponse> results = userService.searchByEmail(email);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<UserProfileResponse>> searchByName(@RequestParam("q") String name) {
        List<UserProfileResponse> results = userService.searchByName(name);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<UserProfileResponse>> getUsersByLocation(@PathVariable String location) {
        List<UserProfileResponse> results = userService.getUsersByLocation(location);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/complete")
    public ResponseEntity<List<UserProfileResponse>> getUsersWithCompleteProfiles() {
        List<UserProfileResponse> results = userService.getUsersWithCompleteProfiles();
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/profiles/{userId}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long userId,
                                           @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and get requester ID
            Long requesterId = authClientService.extractUserIdFromToken(authHeader);

            // Check if user is deleting their own profile
            if (!userId.equals(requesterId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("You can only delete your own profile"));
            }

            userService.deleteProfile(userId);
            return ResponseEntity.ok(new SuccessResponse("Profile deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<UserService.ProfileStats> getProfileStats() {
        UserService.ProfileStats stats = userService.getProfileStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        UserService.ProfileStats stats = userService.getProfileStats();
        return ResponseEntity.ok("User Service is healthy! Total profiles: " + stats.getTotalProfiles());
    }

    @GetMapping("/info")
    public ResponseEntity<SuccessResponse> info() {
        return ResponseEntity.ok(new SuccessResponse("User Service v1.0 - User profile management"));
    }

    public static class ErrorResponse {
        private String message;
        private String timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }

        public String getMessage() {
            return message;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    public static class SuccessResponse {
        private String message;
        private String timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }

        public String getMessage() {
            return message;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}