package com.exe202.skillnest.entity;

import com.exe202.skillnest.enums.ProjectStatus;
import com.exe202.skillnest.enums.ProjectType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false)
    private ProjectType projectType = ProjectType.FIXED_PRICE;

    @Column(name = "budget_min", precision = 12, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 12, scale = 2)
    private BigDecimal budgetMax;

    @Column(nullable = false, length = 10)
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.OPEN;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "project_skills",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    // ========== NEW FIELDS FOR JOB DETAIL ==========
    @Column(length = 200)
    private String location;

    @Column(name = "employment_type", length = 50)
    private String employmentType; // "Thực tập", "Part-time", "Full-time"

    @Column(name = "salary_unit", length = 20)
    private String salaryUnit; // "MONTH" or "YEAR"

    @ElementCollection
    @CollectionTable(
            name = "project_requirements",
            schema = "public",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "requirement", columnDefinition = "TEXT")
    private List<String> requirements = new ArrayList<>();

    // ========== NEW FIELDS FOR RECRUITMENT FORM ==========
    @Column(name = "headcount_min")
    private Integer headcountMin; // Số lượng tuyển dụng tối thiểu

    @Column(name = "headcount_max")
    private Integer headcountMax; // Số lượng tuyển dụng tối đa

    @Column(name = "deadline")
    private LocalDate deadline; // Hạn nộp hồ sơ

    @ElementCollection
    @CollectionTable(
            name = "project_benefits",
            schema = "public",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "benefit", columnDefinition = "TEXT")
    private List<String> benefits = new ArrayList<>();
}
