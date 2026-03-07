package com.exe202.skillnest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 200)
    private String university;

    @Column(length = 200)
    private String major;

    @Column(name = "year_of_study", length = 50)
    private String year;

    @Column(columnDefinition = "DECIMAL(3,2)")
    private Double gpa;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 500)
    private String address;

    @ElementCollection
    @CollectionTable(name = "profile_skills", schema = "public", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill", length = 100)
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_interests", schema = "public", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "interest", length = 100)
    private List<String> interests = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_preferred_locations", schema = "public", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "location", length = 100)
    private List<String> preferredLocations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_preferred_job_types", schema = "public", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "job_type", length = 50)
    private List<String> preferredJobTypes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
