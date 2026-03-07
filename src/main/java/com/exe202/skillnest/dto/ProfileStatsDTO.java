package com.exe202.skillnest.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileStatsDTO {
    private int totalApplications;
    private int profileCompletion;

    private StatusDetail pending;
    private StatusDetail interviewed;
    private StatusDetail rejected;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusDetail {
        private int count;
        private List<Long> jobIds = new ArrayList<>();
    }
}

