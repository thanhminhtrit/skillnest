package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.CreateRatingRequest;
import com.exe202.skillnest.dto.RatingDTO;
import com.exe202.skillnest.dto.UserRatingStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RatingService {
    RatingDTO createRating(CreateRatingRequest request, String email);
    Page<RatingDTO> getUserRatings(Long userId, Pageable pageable);
    List<RatingDTO> getContractRatings(Long contractId, String email);
    Page<RatingDTO> getMyReceivedRatings(String email, Pageable pageable);
    UserRatingStatsDTO getUserRatingStats(Long userId);

    // Admin moderation
    Page<RatingDTO> getPendingReviewRatings(Pageable pageable);
    RatingDTO approveRating(Long ratingId, Long adminId, String adminNote);
    RatingDTO flagRating(Long ratingId, Long adminId, String adminNote);

    // Scheduled tasks
    void autoRevealExpiredPendingRatings();
    void autoRateExpiredContracts();
}
