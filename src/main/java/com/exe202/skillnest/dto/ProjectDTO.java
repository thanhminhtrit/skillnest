package com.exe202.skillnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    // ========== EXISTING FIELDS (KHÔNG ĐƯỢC THAY ĐỔI) ==========
    private Long projectId;
    private Long clientId;
    private String clientName;
    private String title;
    private String description;
    private String projectType;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String currency;
    private String status;
    private Set<String> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== NEW FIELDS FOR JOB DETAIL UI ==========
    private String location;
    private String employmentType;
    private String salaryUnit; // "MONTH" or "YEAR"
    private List<String> requirements;
    private CompanyInfoDTO company;
    private Boolean isSaved;
    private Boolean hasApplied;
    private String postedAgo; // e.g., "Đăng 2 ngày trước"

    // ========== NEW FIELDS FOR RECRUITMENT FORM ==========
    private Integer headcountMin;
    private Integer headcountMax;
    private LocalDate deadline;
    private List<String> benefits;
}
