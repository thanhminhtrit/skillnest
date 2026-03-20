package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    @Query("SELECT s FROM UserSubscription s WHERE s.user.userId = :userId AND s.status = 'ACTIVE'")
    Optional<UserSubscription> findActiveByUserId(Long userId);
}
