package com.exe202.skillnest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingStatsDTO {
    private int totalMatches;
    private int highMatches;
    private int averageScore;
}
