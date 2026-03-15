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
public class PaymentRequestDTO {
    private Long paymentRequestId;
    private Long proposalId;
    private Long clientId;
    private String clientName;
    private Long studentId;
    private String studentName;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private BigDecimal studentAmount;
    private PaymentStatus status;
    private String qrCodeUrl;
    private String paymentReference;
    private String bankTransferNote;
    private Long verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

