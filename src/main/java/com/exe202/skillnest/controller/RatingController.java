package com.exe202.skillnest.controller;

import com.exe202.skillnest.config.security.IsManager;
import com.exe202.skillnest.dto.CreateRatingRequest;
import com.exe202.skillnest.dto.RatingDTO;
import com.exe202.skillnest.dto.UserRatingStatsDTO;
import com.exe202.skillnest.payloads.response.BaseResponse;
import com.exe202.skillnest.service.RatingService;
import com.exe202.skillnest.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Rating & Review", description = "Two-way rating system with admin moderation for low scores")
public class RatingController {

    private final RatingService ratingService;
    private final SecurityUtil securityUtil;

    // ========== USER ENDPOINTS ==========

    @PostMapping
    @Operation(summary = "Rate the other party (after contract COMPLETED)")
    public ResponseEntity<BaseResponse> createRating(
            @Valid @RequestBody CreateRatingRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        RatingDTO rating = ratingService.createRating(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201,
                        "Rating submitted. Ratings >= 4 stars are visible immediately. " +
                        "Lower ratings require admin review (up to 7 days).", rating));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get visible ratings for a user (public)")
    public ResponseEntity<BaseResponse> getUserRatings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RatingDTO> ratings = ratingService.getUserRatings(userId, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Ratings retrieved successfully", ratings));
    }

    @GetMapping("/user/{userId}/stats")
    @Operation(summary = "Get rating statistics for a user (public)")
    public ResponseEntity<BaseResponse> getUserRatingStats(@PathVariable Long userId) {
        UserRatingStatsDTO stats = ratingService.getUserRatingStats(userId);
        return ResponseEntity.ok(new BaseResponse(200, "Rating stats retrieved successfully", stats));
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get ratings for a contract (participants only)")
    public ResponseEntity<BaseResponse> getContractRatings(
            @PathVariable Long contractId,
            Authentication authentication) {
        String email = authentication.getName();
        List<RatingDTO> ratings = ratingService.getContractRatings(contractId, email);
        return ResponseEntity.ok(new BaseResponse(200, "Contract ratings retrieved successfully", ratings));
    }

    @GetMapping("/my")
    @Operation(summary = "Get ratings I have received")
    public ResponseEntity<BaseResponse> getMyReceivedRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RatingDTO> ratings = ratingService.getMyReceivedRatings(email, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "My ratings retrieved successfully", ratings));
    }

    // ========== ADMIN ENDPOINTS ==========

    @GetMapping("/admin/pending")
    @IsManager
    @Operation(summary = "Get ratings pending admin review (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> getPendingReviewRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<RatingDTO> ratings = ratingService.getPendingReviewRatings(pageable);
        return ResponseEntity.ok(new BaseResponse(200, "Pending ratings retrieved successfully", ratings));
    }

    @PostMapping("/admin/{ratingId}/approve")
    @IsManager
    @Operation(summary = "Approve rating and make visible (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> approveRating(
            @PathVariable Long ratingId,
            @RequestParam(required = false) String adminNote) {
        Long adminId = securityUtil.getCurrentUserId();
        RatingDTO rating = ratingService.approveRating(ratingId, adminId, adminNote);
        return ResponseEntity.ok(new BaseResponse(200, "Rating approved and now visible", rating));
    }

    @PostMapping("/admin/{ratingId}/flag")
    @IsManager
    @Operation(summary = "Flag rating with note and make visible (ADMIN/MANAGER)")
    public ResponseEntity<BaseResponse> flagRating(
            @PathVariable Long ratingId,
            @RequestParam String adminNote) {
        Long adminId = securityUtil.getCurrentUserId();
        RatingDTO rating = ratingService.flagRating(ratingId, adminId, adminNote);
        return ResponseEntity.ok(new BaseResponse(200, "Rating flagged and now visible with admin note", rating));
    }
}
