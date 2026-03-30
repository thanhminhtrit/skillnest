package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.DisputeDTO;
import com.exe202.skillnest.entity.Dispute;
import org.springframework.stereotype.Component;

@Component
public class DisputeMapper {

    public DisputeDTO toDTO(Dispute entity) {
        if (entity == null) {
            return null;
        }

        return DisputeDTO.builder()
                .disputeId(entity.getDisputeId())
                .contractId(entity.getContract() != null ? entity.getContract().getContractId() : null)
                .raisedBy(entity.getOpenedBy() != null ? entity.getOpenedBy().getUserId() : null)
                .raisedByName(entity.getOpenedBy() != null ? entity.getOpenedBy().getFullName() : null)
                .reason(entity.getReason())
                .status(entity.getStatus())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
