package com.exe202.skillnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private Long planId;
    private String name;
    private String displayName;
    private BigDecimal price;
    private Integer postLimit;
    private Integer aiMatchingLimit;
    private Integer durationDays;
}
