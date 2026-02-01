package com.exe202.skillnest.payloads.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProposalRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    private String coverLetter;

    private BigDecimal proposedPrice;
    private String currency = "VND";
    private Integer durationDays;
}

