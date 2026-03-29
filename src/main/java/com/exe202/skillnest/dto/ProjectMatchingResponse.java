package com.exe202.skillnest.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMatchingResponse {
    private ProfileResponseDTO studentProfile;
    private MatchingStatsDTO stats;
    private List<ProjectMatchDTO> projects;
    private int total;
    private int page;
    private int limit;
}
