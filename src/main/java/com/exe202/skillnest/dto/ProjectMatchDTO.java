package com.exe202.skillnest.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMatchDTO {
    private Long projectId;
    private String title;
    private String companyName;
    private String location;
    private String employmentType;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String currency;
    private String postedAgo;

    // Matching
    private Integer matchScore;
    private String matchReason;
}
