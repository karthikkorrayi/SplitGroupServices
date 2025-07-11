package com.service.user_service.service;

import com.service.user_service.dto.UpdateProfileRequest;
import com.service.user_service.dto.UserProfileRequest;
import com.service.user_service.dto.UserProfileResponse;
import com.service.user_service.entity.UserProfile;
import com.service.user_service.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuthClientService authClientService;

    public UserProfileResponse createProfile(UserProfileRequest request) {
        // Check if profile already exists
        if (userProfileRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Profile already exists for user ID: " + request.getUserId());
        }

        // Create new profile
        UserProfile profile = new UserProfile(
                request.getUserId(),
                request.getEmail(),
                request.getName()
        );

        UserProfile savedProfile = userProfileRepository.save(profile);
        return new UserProfileResponse(savedProfile);
    }

    public UserProfileResponse getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));

        // Update last active time
        profile.setLastActive(LocalDateTime.now());
        userProfileRepository.save(profile);

        return new UserProfileResponse(profile);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));

        // Update fields if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            profile.setName(request.getName().trim());
        }

        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone().trim());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }

        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation().trim());
        }

        if (request.getProfilePictureUrl() != null) {
            profile.setProfilePictureUrl(request.getProfilePictureUrl().trim());
        }

        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getNotificationEnabled() != null) {
            profile.setNotificationEnabled(request.getNotificationEnabled());
        }

        if (request.getEmailNotifications() != null) {
            profile.setEmailNotifications(request.getEmailNotifications());
        }

        if (request.getPrivacyLevel() != null) {
            profile.setPrivacyLevel(request.getPrivacyLevel());
        }

        // Check if profile is complete after update
        profile.setProfileCompleted(profile.isProfileComplete());

        UserProfile updatedProfile = userProfileRepository.save(profile);
        return new UserProfileResponse(updatedProfile);
    }

    public List<UserProfileResponse> searchByEmail(String email) {
        List<UserProfile> profiles = userProfileRepository.findByEmailContainingIgnoreCase(email);
        return profiles.stream()
                .map(UserProfileResponse::new)
                .collect(Collectors.toList());
    }

    public List<UserProfileResponse> searchByName(String name) {
        List<UserProfile> profiles = userProfileRepository.findByNameContainingIgnoreCase(name);
        return profiles.stream()
                .map(UserProfileResponse::new)
                .collect(Collectors.toList());
    }

    public List<UserProfileResponse> getUsersByLocation(String location) {
        List<UserProfile> profiles = userProfileRepository.findByLocation(location);
        return profiles.stream()
                .map(UserProfileResponse::new)
                .collect(Collectors.toList());
    }

    public List<UserProfileResponse> getUsersWithCompleteProfiles() {
        List<UserProfile> profiles = userProfileRepository.findUsersWithCompleteProfiles();
        return profiles.stream()
                .map(UserProfileResponse::new)
                .collect(Collectors.toList());
    }

    public void deleteProfile(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));

        userProfileRepository.delete(profile);
    }

    public ProfileStats getProfileStats() {
        long totalProfiles = userProfileRepository.countAllProfiles();
        long profilesWithPhone = userProfileRepository.countProfilesWithPhone();
        long completeProfiles = userProfileRepository.findUsersWithCompleteProfiles().size();

        return new ProfileStats(totalProfiles, profilesWithPhone, completeProfiles);
    }

    public boolean canAccessProfile(Long profileOwnerId, Long requesterId) {
        // User can always access their own profile
        if (profileOwnerId.equals(requesterId)) {
            return true;
        }

        UserProfile profile = userProfileRepository.findById(profileOwnerId).orElse(null);
        if (profile == null) {
            return false;
        }

        // Check privacy level
        switch (profile.getPrivacyLevel()) {
            case PUBLIC:
                return true;
            case FRIENDS_ONLY:
                // TODO: Implement friends check when we add friend functionality
                return false;
            case PRIVATE:
                return false;
            default:
                return false;
        }
    }

    public static class ProfileStats {
        private long totalProfiles;
        private long profilesWithPhone;
        private long completeProfiles;

        public ProfileStats(long totalProfiles, long profilesWithPhone, long completeProfiles) {
            this.totalProfiles = totalProfiles;
            this.profilesWithPhone = profilesWithPhone;
            this.completeProfiles = completeProfiles;
        }

        // Getters
        public long getTotalProfiles() {
            return totalProfiles;
        }

        public long getProfilesWithPhone() {
            return profilesWithPhone;
        }

        public long getCompleteProfiles() {
            return completeProfiles;
        }
    }
}