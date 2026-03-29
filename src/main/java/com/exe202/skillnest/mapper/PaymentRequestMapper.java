package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.PaymentRequestDTO;
import com.exe202.skillnest.entity.PaymentRequest;
import com.exe202.skillnest.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestMapper {

    public PaymentRequestDTO toDTO(PaymentRequest entity) {
        if (entity == null) {
            return null;
        }

        // Extract client info directly from PaymentRequest.client
        Long clientId = null;
        String clientName = null;
        if (entity.getClient() != null) {
            User client = entity.getClient();
            clientId = client.getUserId();
            clientName = client.getFullName();
        }

        // Extract student info through proposal → student (separate relationship)
        Long studentId = null;
        String studentName = null;
        if (entity.getProposal() != null && entity.getProposal().getStudent() != null) {
            User student = entity.getProposal().getStudent();
            studentId = student.getUserId();
            studentName = student.getFullName();
        }

        return PaymentRequestDTO.builder()
                .paymentRequestId(entity.getPaymentRequestId())
                .proposalId(entity.getProposal() != null ? entity.getProposal().getProposalId() : null)
                .clientId(clientId)
                .clientName(clientName)
                .studentId(studentId)
                .studentName(studentName)
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

