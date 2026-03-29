package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.ProposalDTO;
import com.exe202.skillnest.entity.Project;
import com.exe202.skillnest.entity.Proposal;
import com.exe202.skillnest.entity.User;
import com.exe202.skillnest.enums.ProposalStatus;
import com.exe202.skillnest.enums.ProjectStatus;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.ConflictException;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.payloads.request.CreateProposalRequest;
import com.exe202.skillnest.repository.ProjectRepository;
import com.exe202.skillnest.repository.ProposalRepository;
import com.exe202.skillnest.repository.UserRepository;
import com.exe202.skillnest.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProposalDTO createProposal(CreateProposalRequest request, String email) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));

        // Check if project is open
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BadRequestException("Project is not open for proposals");
        }

        // Check if student already submitted proposal
        if (proposalRepository.existsByProjectProjectIdAndStudentUserId(project.getProjectId(), student.getUserId())) {
            throw new ConflictException("You have already submitted a proposal for this project");
        }

        Proposal proposal = Proposal.builder()
                .project(project)
                .student(student)
                .coverLetter(request.getCoverLetter())
                .proposedPrice(request.getProposedPrice())
                .currency(request.getCurrency())
                .durationDays(request.getDurationDays())
                .status(ProposalStatus.SUBMITTED)
                .build();

        proposal = proposalRepository.save(proposal);
        return convertToDTO(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProposalDTO> getProposalsForProject(Long projectId, String email, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Only project owner can see all proposals
        if (!project.getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You can only view proposals for your own projects");
        }

        return proposalRepository.findByProjectProjectId(projectId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProposalDTO> getMyProposals(String email, Pageable pageable) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return proposalRepository.findByStudentUserId(student.getUserId(), pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public ProposalDTO acceptProposal(Long proposalId, String email) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is the project owner
        if (!proposal.getProject().getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You can only accept proposals for your own projects");
        }

        // Check if proposal is in submitted status
        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new BadRequestException("Proposal is not in submitted status");
        }

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal = proposalRepository.save(proposal);
        return convertToDTO(proposal);
    }

    @Override
    @Transactional
    public ProposalDTO rejectProposal(Long proposalId, String email) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is the project owner
        if (!proposal.getProject().getClient().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You can only reject proposals for your own projects");
        }

        // Check if proposal is in submitted status
        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new BadRequestException("Proposal is not in submitted status");
        }

        proposal.setStatus(ProposalStatus.REJECTED);
        proposal = proposalRepository.save(proposal);
        return convertToDTO(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public ProposalDTO getProposalById(Long proposalId, String email) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user is either the student or the project owner
        boolean isStudent = proposal.getStudent().getUserId().equals(user.getUserId());
        boolean isClient = proposal.getProject().getClient().getUserId().equals(user.getUserId());

        if (!isStudent && !isClient) {
            throw new ForbiddenException("You can only view your own proposals or proposals for your projects");
        }

        return convertToDTO(proposal);
    }

    private ProposalDTO convertToDTO(Proposal proposal) {
        return ProposalDTO.builder()
                .proposalId(proposal.getProposalId())
                .projectId(proposal.getProject().getProjectId())
                .projectTitle(proposal.getProject().getTitle())
                .studentId(proposal.getStudent().getUserId())
                .studentName(proposal.getStudent().getFullName())
                .coverLetter(proposal.getCoverLetter())
                .proposedPrice(proposal.getProposedPrice())
                .currency(proposal.getCurrency())
                .durationDays(proposal.getDurationDays())
                .status(proposal.getStatus().name())
                .createdAt(proposal.getCreatedAt())
                .updatedAt(proposal.getUpdatedAt())
                .build();
    }
}
