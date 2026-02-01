package com.exe202.skillnest.payloads.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDisputeRequest {
    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotBlank(message = "Reason is required")
    private String reason;
}

