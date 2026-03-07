package com.exe202.skillnest.payloads.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CreateProjectRequest {
    // ========== EXISTING FIELDS (KHÔNG ĐƯỢC THAY ĐỔI) ==========
    @NotBlank(message = "Title is required")
    @Size(max = 250, message = "Title must not exceed 250 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String projectType = "FIXED_PRICE"; // FIXED_PRICE or HOURLY

    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String currency = "VND";

    private Set<String> skillIds; // Changed from Set<Long> to Set<String>, nullable

    // ========== PREVIOUS EXTENSION FIELDS ==========
    private String location;
    private String employmentType;
    private String salaryUnit; // "MONTH" or "YEAR"
    private List<String> requirements;

    // ========== NEW FIELDS FOR FORM UI ==========
    private Integer headcountMin; // Số lượng tuyển dụng tối thiểu (e.g., 2 from "2-3")
    private Integer headcountMax; // Số lượng tuyển dụng tối đa (e.g., 3 from "2-3")
    private LocalDate deadline; // Hạn nộp hồ sơ (e.g., "31/12/2024")
    private List<String> benefits; // Quyền lợi (mỗi dòng 1 benefit)
}
