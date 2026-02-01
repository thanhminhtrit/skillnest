package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.ProposalDTO;
import com.exe202.skillnest.payloads.request.CreateProposalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProposalService {
    ProposalDTO createProposal(CreateProposalRequest request, String email);
    Page<ProposalDTO> getProposalsForProject(Long projectId, String email, Pageable pageable);
    Page<ProposalDTO> getMyProposals(String email, Pageable pageable);
    ProposalDTO acceptProposal(Long proposalId, String email);
    ProposalDTO rejectProposal(Long proposalId, String email);
    ProposalDTO getProposalById(Long proposalId, String email);
}
