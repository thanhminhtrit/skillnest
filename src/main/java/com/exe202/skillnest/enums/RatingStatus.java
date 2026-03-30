package com.exe202.skillnest.enums;

public enum RatingStatus {
    HIDDEN,          // Default when created, waiting for other party
    PENDING_REVIEW,  // Both rated but score < 4, waiting for admin review
    APPROVED,        // Admin approved, now visible
    VISIBLE,         // Auto-visible (both >= 4, or 7 days expired, or auto-generated)
    FLAGGED          // Admin flagged as unfair, stays hidden permanently
}
