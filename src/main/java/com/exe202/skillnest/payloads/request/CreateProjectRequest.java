package com.exe202.skillnest.payloads.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 250, message = "Title must not exceed 250 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String projectType = "FIXED_PRICE"; // FIXED_PRICE or HOURLY

    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String currency = "VND";

    private Set<Long> skillIds;
}

