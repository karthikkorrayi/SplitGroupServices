package com.service.user_service.dto;

import com.service.user_service.entity.UserProfile;
import java.time.LocalDateTime;

public class UserProfileResponse {

    private Long userId;
    private String email;
    private String name;
    private String phone;
    private String bio;
    private String location;
    private String profilePictureUrl;
    private LocalDateTime dateOfBirth;
    private Boolean notificationEnabled;
    private Boolean emailNotifications;
    private UserProfile.PrivacyLevel privacyLevel;
    private Boolean profileCompleted;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public UserProfileResponse() {
    }

    // Constructor from UserProfile entity
    public UserProfileResponse(UserProfile profile) {
        this.userId = profile.getUserId();
        this.email = profile.getEmail();
        this.name = profile.getName();
        this.phone = profile.getPhone();
        this.bio = profile.getBio();
        this.location = profile.getLocation();
        this.profilePictureUrl = profile.getProfilePictureUrl();
        this.dateOfBirth = profile.getDateOfBirth();
        this.notificationEnabled = profile.getNotificationEnabled();
        this.emailNotifications = profile.getEmailNotifications();
        this.privacyLevel = profile.getPrivacyLevel();
        this.profileCompleted = profile.getProfileCompleted();
        this.lastActive = profile.getLastActive();
        this.createdAt = profile.getCreatedAt();
        this.updatedAt = profile.getUpdatedAt();
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public UserProfile.PrivacyLevel getPrivacyLevel() {
        return privacyLevel;
    }

    public void setPrivacyLevel(UserProfile.PrivacyLevel privacyLevel) {
        this.privacyLevel = privacyLevel;
    }

    public Boolean getProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public LocalDateTime getLastActive() {
        return lastActive;
    }

    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}