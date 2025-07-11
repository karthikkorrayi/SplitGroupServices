package com.service.user_service.repository;

import com.service.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByEmail(String email);

    @Query("SELECT up FROM UserProfile up WHERE LOWER(up.email) = LOWER(:email)")
    Optional<UserProfile> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT up FROM UserProfile up WHERE LOWER(up.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserProfile> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT up FROM UserProfile up WHERE LOWER(up.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<UserProfile> findByEmailContainingIgnoreCase(@Param("email") String email);

    @Query("SELECT up FROM UserProfile up WHERE LOWER(up.location) = LOWER(:location)")
    List<UserProfile> findByLocation(@Param("location") String location);

    @Query("SELECT up FROM UserProfile up WHERE up.profileCompleted = true")
    List<UserProfile> findUsersWithCompleteProfiles();

    List<UserProfile> findByPrivacyLevel(UserProfile.PrivacyLevel privacyLevel);

    @Query("SELECT COUNT(up) FROM UserProfile up")
    long countAllProfiles();

    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.phone IS NOT NULL AND up.phone != ''")
    long countProfilesWithPhone();

    boolean existsByUserId(Long userId);
}