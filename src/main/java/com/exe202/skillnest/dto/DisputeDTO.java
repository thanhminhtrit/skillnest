package com.exe202.skillnest.dto;

import com.exe202.skillnest.enums.DisputeStatus;
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
    private Long raisedBy;
    private String raisedByName;
    private String reason;
    private DisputeStatus status;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
