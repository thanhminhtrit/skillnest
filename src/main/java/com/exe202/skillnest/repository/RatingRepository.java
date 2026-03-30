package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Rating;
import com.exe202.skillnest.enums.RatingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByContract_ContractIdAndReviewer_UserId(Long contractId, Long reviewerId);

    Optional<Rating> findByContract_ContractIdAndReviewer_UserId(Long contractId, Long reviewerId);

    List<Rating> findByContract_ContractId(Long contractId);

    @Query("SELECT r FROM Rating r WHERE r.reviewee.userId = :userId AND (r.status = 'VISIBLE' OR r.status = 'APPROVED')")
    Page<Rating> findVisibleByRevieweeId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.reviewee.userId = :userId AND (r.status = 'VISIBLE' OR r.status = 'APPROVED')")
    Double findAverageScoreByRevieweeId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.reviewee.userId = :userId AND (r.status = 'VISIBLE' OR r.status = 'APPROVED')")
    Integer countVisibleByRevieweeId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.reviewee.userId = :userId AND (r.status = 'VISIBLE' OR r.status = 'APPROVED') AND r.score = :score")
    Integer countByRevieweeIdAndScore(@Param("userId") Long userId, @Param("score") Integer score);

    Page<Rating> findByStatus(RatingStatus status, Pageable pageable);

    @Query("SELECT r FROM Rating r WHERE r.status = 'PENDING_REVIEW' AND r.createdAt < :cutoff")
    List<Rating> findPendingReviewOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
