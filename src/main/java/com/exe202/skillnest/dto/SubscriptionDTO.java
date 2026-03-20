package com.exe202.skillnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private Long subscriptionId;
    private String planName;
    private BigDecimal price;
    private Integer postLimit;
    private Integer postsUsed;
    private Integer postsRemaining;
    private Integer aiMatchingLimit;
    private Integer aiMatchingUsed;
    private Integer aiMatchingRemaining;
    private LocalDateTime endDate;
    private String status;
}
