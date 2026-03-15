package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.TransactionDTO;
import com.exe202.skillnest.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDTO toDTO(Transaction entity) {
        if (entity == null) {
            return null;
        }

        return TransactionDTO.builder()
                .transactionId(entity.getTransactionId())
                .contractId(entity.getContract() != null ? entity.getContract().getContractId() : null)
                .fromUserId(entity.getFromUser() != null ? entity.getFromUser().getUserId() : null)
                .fromUserName(entity.getFromUser() != null ? entity.getFromUser().getFullName() : null)
                .toUserId(entity.getToUser() != null ? entity.getToUser().getUserId() : null)
                .toUserName(entity.getToUser() != null ? entity.getToUser().getFullName() : null)
                .type(entity.getType())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

