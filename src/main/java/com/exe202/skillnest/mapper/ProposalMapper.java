package com.exe202.skillnest.mapper;

import com.exe202.skillnest.dto.ProposalDTO;
import com.exe202.skillnest.entity.Proposal;
import org.springframework.stereotype.Component;

@Component
public class ProposalMapper {
    public ProposalDTO toDTO(Proposal entity) {
        if (entity == null) return null;
        return ProposalDTO.builder()
                .proposalId(entity.getProposalId())
                .projectId(entity.getProject().getProjectId())
                .projectTitle(entity.getProject().getTitle())
                .studentId(entity.getStudent().getUserId())
                .studentName(entity.getStudent().getFullName())
                .coverLetter(entity.getCoverLetter())
                .proposedPrice(entity.getProposedPrice())
                .currency(entity.getCurrency())
                .durationDays(entity.getDurationDays())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
