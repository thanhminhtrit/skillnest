package com.exe202.skillnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long projectId;
    private Long clientId;
    private String clientName;
    private String title;
    private String description;
    private String projectType;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String currency;
    private String status;
    private Set<String> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

