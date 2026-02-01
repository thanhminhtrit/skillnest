package com.exe202.skillnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeDTO {
    private Long disputeId;
    private Long contractId;
    private Long openedBy;
    private String openedByName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

