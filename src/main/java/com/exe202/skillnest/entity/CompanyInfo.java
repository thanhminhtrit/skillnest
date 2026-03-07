package com.exe202.skillnest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_info", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // Client user

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String size; // e.g., "100-500 nhân viên"

    @Column(length = 200)
    private String industry; // e.g., "Công nghệ thông tin"

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
