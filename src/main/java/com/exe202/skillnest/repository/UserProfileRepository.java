package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
}

