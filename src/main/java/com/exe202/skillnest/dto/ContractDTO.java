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
public class ContractDTO {
    private Long contractId;
    private Long projectId;
    private String projectTitle;
    private Long proposalId;
    private Long clientId;
    private String clientName;
    private Long studentId;
    private String studentName;
    private BigDecimal agreedPrice;
    private String currency;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

