package com.exe202.skillnest.dto;

import com.exe202.skillnest.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long transactionId;
    private Long contractId;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
}

