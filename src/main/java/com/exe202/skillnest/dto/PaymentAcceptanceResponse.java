package com.exe202.skillnest.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAcceptanceResponse {
    private Long paymentRequestId;
    private String qrCodeUrl;
    private String qrCodeBase64; // Thêm trường này để chứa QR code dưới dạng base64
    private String paymentReference;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private BigDecimal studentAmount;
    private String currency;
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
