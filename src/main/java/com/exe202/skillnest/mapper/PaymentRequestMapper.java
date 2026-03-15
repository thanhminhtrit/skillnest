package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.PaymentRequestDTO;
import com.exe202.skillnest.entity.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestMapper {

    public PaymentRequestDTO toDTO(PaymentRequest entity) {
        if (entity == null) {
            return null;
        }

        return PaymentRequestDTO.builder()
                .paymentRequestId(entity.getPaymentRequestId())
                .proposalId(entity.getProposal() != null ? entity.getProposal().getProposalId() : null)
                .clientId(entity.getClient() != null ? entity.getClient().getUserId() : null)
                .clientName(entity.getClient() != null ? entity.getClient().getFullName() : null)
                .studentId(entity.getProposal() != null && entity.getProposal().getStudent() != null
                        ? entity.getProposal().getStudent().getUserId() : null)
                .studentName(entity.getProposal() != null && entity.getProposal().getStudent() != null
                        ? entity.getProposal().getStudent().getFullName() : null)
                .totalAmount(entity.getTotalAmount())
                .platformFee(entity.getPlatformFee())
                .studentAmount(entity.getStudentAmount())
                .status(entity.getStatus())
                .qrCodeUrl(entity.getQrCodeUrl())
                .paymentReference(entity.getPaymentReference())
                .bankTransferNote(entity.getBankTransferNote())
                .verifiedBy(entity.getVerifiedBy() != null ? entity.getVerifiedBy().getUserId() : null)
                .verifiedByName(entity.getVerifiedBy() != null ? entity.getVerifiedBy().getFullName() : null)
                .verifiedAt(entity.getVerifiedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

