package com.exe202.skillnest.payloads.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {
    // ========== EXISTING FIELDS ==========
    private String title;
    private String description;
    private String projectType;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String currency;
    private Set<String> skillIds; // Changed from Set<Long> to Set<String>, nullable

    // ========== PREVIOUS EXTENSION FIELDS ==========
    private String location;
    private String employmentType;
    private String salaryUnit;
    private List<String> requirements;

    // ========== NEW FIELDS FOR FORM UI ==========
    private Integer headcountMin;
    private Integer headcountMax;
    private LocalDate deadline;
    private List<String> benefits;
}
