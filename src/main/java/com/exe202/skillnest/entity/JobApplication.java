package com.exe202.skillnest.entity;

import com.exe202.skillnest.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * @deprecated This entity is from an earlier design phase.
 * The platform uses {@link Proposal} as the primary application mechanism.
 * Student applies to a Project by submitting a Proposal.
 * This entity is kept for backward compatibility with ProfileService stats
 * but should not be used for new features.
 * Future: migrate profile stats to use Proposal data instead.
 */
@Deprecated
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_title", nullable = false, length = 200)
    private String jobTitle;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "applied_date", nullable = false, updatable = false)
    private LocalDateTime appliedDate;
}
