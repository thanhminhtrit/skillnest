package com.exe202.skillnest.config;

import com.exe202.skillnest.entity.Role;
import com.exe202.skillnest.entity.SubscriptionPlan;
import com.exe202.skillnest.repository.RoleRepository;
import com.exe202.skillnest.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final SubscriptionPlanRepository planRepository;

    @Override
    public void run(ApplicationArguments args) {
        // Seed roles
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    Role.builder().name("STUDENT").build(),
                    Role.builder().name("CLIENT").build(),
                    Role.builder().name("MANAGER").build(),
                    Role.builder().name("ADMIN").build()
            ));
        }

        if (planRepository.count() > 0) return;

        planRepository.saveAll(List.of(
                SubscriptionPlan.builder()
                        .name("FREE")
                        .displayName("Gói Miễn Phí")
                        .price(BigDecimal.ZERO)
                        .postLimit(1)
                        .aiMatchingLimit(3)
                        .durationDays(30)
                        .isActive(true)
                        .build(),
                SubscriptionPlan.builder()
                        .name("BASIC")
                        .displayName("Gói Cơ Bản")
                        .price(new BigDecimal("199000"))
                        .postLimit(15)
                        .aiMatchingLimit(30)
                        .durationDays(30)
                        .isActive(true)
                        .build(),
                SubscriptionPlan.builder()
                        .name("PRO")
                        .displayName("Gói Chuyên Nghiệp")
                        .price(new BigDecimal("399000"))
                        .postLimit(null)
                        .aiMatchingLimit(100)
                        .durationDays(30)
                        .isActive(true)
                        .build()
        ));
    }
}
