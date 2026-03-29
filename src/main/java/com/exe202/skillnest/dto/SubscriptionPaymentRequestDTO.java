package com.exe202.skillnest.dto;

import com.exe202.skillnest.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPaymentRequestDTO {
    private Long subPaymentId;
    private Long userId;
    private String userName;
    private String planName;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentReference;
    private String qrCodeUrl;
    private Long verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
