package com.exe202.skillnest.entity;

import com.exe202.skillnest.enums.MatchingEntityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "ai_matching_history", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchingId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "subscription_id")
    private UserSubscription subscription;

    @Enumerated(STRING)
    private MatchingEntityType entityType;

    private Long entityId;

    private String query;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> results;

    private Integer matchCount;

    private Integer executionTimeMs;

    private BigDecimal apiCost;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
