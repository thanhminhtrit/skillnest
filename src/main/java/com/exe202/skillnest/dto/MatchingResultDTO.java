package com.exe202.skillnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResultDTO {
    private Long entityId;
    private String entityType;
    private String name;
    private String avatarUrl;
    private Double matchScore;
    private Integer matchPercentage;
    private String matchReason;
    private List<String> matchingSkills;
}
