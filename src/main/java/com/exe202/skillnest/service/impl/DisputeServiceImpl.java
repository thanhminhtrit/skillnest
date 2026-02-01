package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.DisputeDTO;
import com.exe202.skillnest.entity.Contract;
import com.exe202.skillnest.entity.Dispute;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.DisputeStatus;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.payloads.request.CreateDisputeRequest;
import com.exe202.skillnest.repository.ContractRepository;
import com.exe202.skillnest.repository.DisputeRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DisputeDTO createDispute(CreateDisputeRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Contract contract = contractRepository.findByContractIdAndParticipant(request.getContractId(), user.getUserId())
                .orElseThrow(() -> new NotFoundException("Contract not found or you don't have access"));

        Dispute dispute = Dispute.builder()
                .contract(contract)
                .openedBy(user)
                .reason(request.getReason())
                .status(DisputeStatus.OPEN)
                .build();

        dispute = disputeRepository.save(dispute);
        return convertToDTO(dispute);
    }

    @Override
    public Page<DisputeDTO> getMyDisputes(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return disputeRepository.findByParticipant(user.getUserId(), pageable)
                .map(this::convertToDTO);
    }

    @Override
    public DisputeDTO getDisputeById(Long disputeId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Dispute dispute = disputeRepository.findByDisputeIdAndParticipant(disputeId, user.getUserId())
                .orElseThrow(() -> new NotFoundException("Dispute not found or you don't have access"));

        return convertToDTO(dispute);
    }

    @Override
    @Transactional
    public DisputeDTO updateDisputeStatus(Long disputeId, String status, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("Dispute not found"));

        // For MVP, allow participants to update status (in production, this should be admin only)
        if (!isParticipant(dispute, user.getUserId())) {
            throw new ForbiddenException("You don't have permission to update this dispute");
        }

        DisputeStatus newStatus = DisputeStatus.valueOf(status);
        dispute.setStatus(newStatus);

        if (newStatus == DisputeStatus.RESOLVED || newStatus == DisputeStatus.CLOSED) {
            dispute.setResolvedAt(LocalDateTime.now());
        }

        dispute = disputeRepository.save(dispute);
        return convertToDTO(dispute);
    }

    @Override
    public Page<DisputeDTO> getDisputesByContractId(Long contractId, String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Contract contract = contractRepository.findByContractIdAndParticipant(contractId, user.getUserId())
                .orElseThrow(() -> new NotFoundException("Contract not found or you don't have access"));

        return disputeRepository.findByContractContractId(contractId, pageable)
                .map(this::convertToDTO);
    }

    private boolean isParticipant(Dispute dispute, Long userId) {
        return dispute.getContract().getClient().getUserId().equals(userId) ||
               dispute.getContract().getStudent().getUserId().equals(userId);
    }

    private DisputeDTO convertToDTO(Dispute dispute) {
        return DisputeDTO.builder()
                .disputeId(dispute.getDisputeId())
                .contractId(dispute.getContract().getContractId())
                .openedBy(dispute.getOpenedBy().getUserId())
                .openedByName(dispute.getOpenedBy().getFullName())
                .reason(dispute.getReason())
                .status(dispute.getStatus().name())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }
}

