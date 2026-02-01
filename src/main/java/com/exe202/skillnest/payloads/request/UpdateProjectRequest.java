package com.exe202.skillnest.payloads.request;

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
public class UpdateProjectRequest {
    private String title;
    private String description;
    private String projectType;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String currency;
    private Set<Long> skillIds;
}
