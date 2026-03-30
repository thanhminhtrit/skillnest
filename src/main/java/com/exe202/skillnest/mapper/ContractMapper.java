package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.ContractDTO;
import com.exe202.skillnest.entity.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {
    public ContractDTO toDTO(Contract entity) {
        if (entity == null) return null;
        return ContractDTO.builder()
                .contractId(entity.getContractId())
                .projectId(entity.getProject().getProjectId())
                .projectTitle(entity.getProject().getTitle())
                .proposalId(entity.getProposal().getProposalId())
                .clientId(entity.getClient().getUserId())
                .clientName(entity.getClient().getFullName())
                .studentId(entity.getStudent().getUserId())
                .studentName(entity.getStudent().getFullName())
                .agreedPrice(entity.getAgreedPrice())
                .currency(entity.getCurrency())
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
