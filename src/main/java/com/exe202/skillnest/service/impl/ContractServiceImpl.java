package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.ContractDTO;
import com.exe202.skillnest.entity.Contract;
import com.exe202.skillnest.entity.Proposal;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.ContractStatus;
import com.exe202.skillnest.enums.ProposalStatus;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.mapper.ContractMapper;
import com.exe202.skillnest.repository.ContractRepository;
import com.exe202.skillnest.repository.ProposalRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final ContractMapper contractMapper;
    private final com.exe202.skillnest.service.NotificationService notificationService;

    @Override
    @Transactional
    public ContractDTO createContract(Long proposalId, String email) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is the project owner
        if (!proposal.getProject().getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only project owner can create contract");
        }

        // Check if proposal is accepted
        if (proposal.getStatus() != ProposalStatus.ACCEPTED) {
            throw new BadRequestException("Proposal must be accepted first");
        }

        throw new BadRequestException(
                "Direct contract creation is disabled. " +
                "Please use POST /api/payments/proposals/{proposalId}/accept to initiate payment flow. " +
                "Contract will be created automatically after payment verification."
        );
    }

    @Override
    @Transactional
    public ContractDTO activateContract(Long contractId, String email) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is participant
        if (!isParticipant(contract, user.getUserId())) {
            throw new ForbiddenException("You can only activate contracts you are part of");
        }

        // Check if contract is pending
        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new BadRequestException("Contract is not in pending status");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setStartAt(LocalDateTime.now());
        contract = contractRepository.save(contract);
        return contractMapper.toDTO(contract);
    }

    @Override
    @Transactional
    public ContractDTO completeContract(Long contractId, String email) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is the client
        if (!contract.getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Only client can complete contract");
        }

        // Check if contract is active
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BadRequestException("Contract is not active");
        }

        contract.setStatus(ContractStatus.COMPLETED);
        contract.setEndAt(LocalDateTime.now());
        contract = contractRepository.save(contract);

        // Notify student about completion
        notificationService.notify(
                contract.getStudent().getUserId(),
                com.exe202.skillnest.enums.NotificationType.CONTRACT_COMPLETED,
                "Contract completed",
                "Contract for " + contract.getProject().getTitle() + " has been completed. Don't forget to rate!",
                "CONTRACT",
                contract.getContractId()
        );

        return contractMapper.toDTO(contract);
    }

    @Override
    @Transactional
    public ContractDTO cancelContract(Long contractId, String email) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is participant
        if (!isParticipant(contract, user.getUserId())) {
            throw new ForbiddenException("You can only cancel contracts you are part of");
        }

        contract.setStatus(ContractStatus.CANCELLED);
        contract.setEndAt(LocalDateTime.now());
        contract = contractRepository.save(contract);

        // Notify the other party about cancellation
        Long otherPartyId = contract.getClient().getUserId().equals(user.getUserId())
                ? contract.getStudent().getUserId()
                : contract.getClient().getUserId();
        notificationService.notify(
                otherPartyId,
                com.exe202.skillnest.enums.NotificationType.CONTRACT_CANCELLED,
                "Contract cancelled",
                user.getFullName() + " cancelled the contract for " + contract.getProject().getTitle(),
                "CONTRACT",
                contract.getContractId()
        );

        return contractMapper.toDTO(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractDTO> getMyContracts(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return contractRepository.findByClientUserIdOrStudentUserId(user.getUserId(), user.getUserId(), pageable)
                .map(contractMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO getContractById(Long contractId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Contract contract = contractRepository.findByContractIdAndParticipant(contractId, user.getUserId())
                .orElseThrow(() -> new NotFoundException("Contract not found or you don't have access"));

        return contractMapper.toDTO(contract);
    }

    private boolean isParticipant(Contract contract, Long userId) {
        return contract.getClient().getUserId().equals(userId) ||
               contract.getStudent().getUserId().equals(userId);
    }
}
