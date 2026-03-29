package com.exe202.skillnest.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPaymentResponse {
    private Long subPaymentId;
    private Long planId;
    private String planName;
    private BigDecimal price;
    private String qrCodeUrl;
    private String qrCodeBase64;
    private String paymentReference;
    private BankDetails bankDetails;
    private String message;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankDetails {
        private String bankName;
        private String accountNumber;
        private String accountName;
        private String transferNote;
    }
}
