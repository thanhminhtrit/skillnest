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
public class ProposalDTO {
    private Long proposalId;
    private Long projectId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private String coverLetter;
    private BigDecimal proposedPrice;
    private String currency;
    private Integer durationDays;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

