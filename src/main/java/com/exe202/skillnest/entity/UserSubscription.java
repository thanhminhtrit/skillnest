package com.exe202.skillnest.entity;

import com.exe202.skillnest.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import static com.exe202.skillnest.enums.SubscriptionStatus.ACTIVE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "user_subscriptions", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    @Builder.Default
    private Integer postsUsed = 0;

    @Builder.Default
    private Integer aiMatchingUsed = 0;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(STRING)
    @Builder.Default
    private SubscriptionStatus status = ACTIVE;

    @Builder.Default
    private Boolean autoRenew = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean canCreatePost() {
        return status == ACTIVE &&
               LocalDateTime.now().isBefore(endDate) &&
               (plan.getPostLimit() == null || postsUsed < plan.getPostLimit());
    }

    public boolean canUseAiMatching() {
        return status == ACTIVE &&
               LocalDateTime.now().isBefore(endDate) &&
               (plan.getAiMatchingLimit() == null || aiMatchingUsed < plan.getAiMatchingLimit());
    }

    public void incrementPostUsage() { this.postsUsed++; }

    public void incrementAiMatchingUsage() { this.aiMatchingUsed++; }
}
