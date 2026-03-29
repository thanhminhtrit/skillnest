package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.dto.PaymentAcceptanceResponse;
import com.exe202.skillnest.dto.PaymentRequestDTO;
import com.exe202.skillnest.dto.TransactionDTO;
import com.exe202.skillnest.entity.*;
import com.exe202.skillnest.enums.*;
import com.exe202.skillnest.exception.BadRequestException;
import com.exe202.skillnest.exception.ForbiddenException;
import com.exe202.skillnest.exception.NotFoundException;
import com.exe202.skillnest.mapper.PaymentRequestMapper;
import com.exe202.skillnest.mapper.TransactionMapper;
import com.exe202.skillnest.repository.*;
import com.exe202.skillnest.service.FileStorageService;
import com.exe202.skillnest.service.PaymentService;
import com.exe202.skillnest.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final TransactionRepository transactionRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ConversationRepository conversationRepository;
    private final PaymentRequestMapper paymentRequestMapper;
    private final TransactionMapper transactionMapper;
    private final QRCodeGenerator qrCodeGenerator;
    private final FileStorageService fileStorageService;

    @Value("${payment.platform-fee-percent:8}")
    private BigDecimal platformFeePercent;

    @Value("${payment.bank.name:Vietcombank}")
    private String bankName;

    @Value("${payment.bank.account-number:1234567890}")
    private String bankAccountNumber;

    @Value("${payment.bank.account-name:SKILLNEST PLATFORM}")
    private String bankAccountName;

    @Value("${payment.bank.code:VCB}")
    private String bankCode;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    @Transactional
    public PaymentAcceptanceResponse acceptProposalWithPayment(Long proposalId, Long clientId) {
        log.info("Client {} accepting proposal {} with payment", clientId, proposalId);

        // 1. Validate proposal
        Proposal proposal = proposalRepository.findByIdWithLock(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if (!proposal.getStatus().equals(ProposalStatus.SUBMITTED)) {
            throw new BadRequestException("Proposal is not in SUBMITTED status");
        }

        // 2. Validate client is the project owner
        Project project = proposal.getProject();
        if (!project.getClient().getUserId().equals(clientId)) {
            throw new ForbiddenException("Only project owner can accept proposals");
        }

        // 3. Validate client is not the student (sanity check)
        if (proposal.getStudent().getUserId().equals(clientId)) {
            throw new BadRequestException("Client cannot accept their own proposal");
        }

        // 3. Check if payment request already exists
        if (paymentRequestRepository.findByProposal_ProposalId(proposalId).isPresent()) {
            throw new BadRequestException("Payment request already exists for this proposal");
        }

        // 4. Calculate amounts - FEE-ONLY model: client pays 8% to platform, rest goes directly to student
        BigDecimal proposedPrice = proposal.getProposedPrice();
        if (proposedPrice == null || proposedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Proposed price is not set or invalid");
        }

        BigDecimal platformFee = proposedPrice.multiply(platformFeePercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = platformFee; // Client only transfers platform fee via QR
        BigDecimal studentAmount = proposedPrice.subtract(platformFee); // Student receives the rest directly from client

        // 5. Generate unique payment reference
        String paymentReference = generatePaymentReference();

        // 6. Generate QR code với số tiền = platformFee (cả URL và Base64)
        String qrCodeUrl = generateQRCode(platformFee, paymentReference);
        String qrCodeBase64 = convertQRCodeToBase64(platformFee, paymentReference);

        // 7. Create payment request
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .proposal(proposal)
                .client(client)
                .totalAmount(totalAmount) // Chỉ là phí nền tảng
                .platformFee(platformFee)
                .studentAmount(studentAmount) // Số tiền student sẽ nhận
                .status(PaymentStatus.PENDING_PAYMENT)
                .qrCodeUrl(qrCodeUrl)
                .paymentReference(paymentReference)
                .bankTransferNote("SKILLNEST " + paymentReference)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        paymentRequest = paymentRequestRepository.save(paymentRequest);

        // 8. Update proposal status to ACCEPTED
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalRepository.save(proposal);

        log.info("Payment request created: {} - Platform fee only: {}", paymentRequest.getPaymentRequestId(), platformFee);

        // 9. Build response
        return PaymentAcceptanceResponse.builder()
                .paymentRequestId(paymentRequest.getPaymentRequestId())
                .qrCodeUrl(qrCodeUrl)
                .qrCodeBase64(qrCodeBase64) // Thêm QR code dưới dạng base64
                .paymentReference(paymentReference)
                .totalAmount(totalAmount) // Phí nền tảng
                .platformFee(platformFee)
                .studentAmount(studentAmount)
                .currency(proposal.getCurrency())
                .bankDetails(PaymentAcceptanceResponse.BankDetails.builder()
                        .bankName(bankName)
                        .accountNumber(bankAccountNumber)
                        .accountName(bankAccountName)
                        .transferNote("SKILLNEST " + paymentReference)
                        .build())
                .message(String.format(
                        "Agreed price: %s VND. Platform fee (8%%): %s VND. " +
                        "Please transfer %s VND to our account. " +
                        "After verification, contract will be created. " +
                        "You will then pay %s VND directly to the student.",
                        proposedPrice.toPlainString(),
                        platformFee.toPlainString(),
                        platformFee.toPlainString(),
                        studentAmount.toPlainString()
                ))
                .build();
    }

    @Override
    @Transactional
    public PaymentRequestDTO verifyPayment(Long paymentRequestId, Long verifierId) {
        log.info("Verifier {} verifying payment request {}", verifierId, paymentRequestId);

        // 1. Get payment request
        PaymentRequest paymentRequest = paymentRequestRepository.findByIdWithLock(paymentRequestId)
                .orElseThrow(() -> new NotFoundException("Payment request not found"));

        if (!paymentRequest.getStatus().equals(PaymentStatus.PENDING_PAYMENT)) {
            throw new BadRequestException("Payment is not in PENDING_PAYMENT status");
        }

        User verifier = userRepository.findById(verifierId)
                .orElseThrow(() -> new NotFoundException("Verifier not found"));

        // 2. Update payment request status
        paymentRequest.setStatus(PaymentStatus.PAID);
        paymentRequest.setVerifiedBy(verifier);
        paymentRequest.setVerifiedAt(LocalDateTime.now());
        paymentRequestRepository.save(paymentRequest);

        // 3. Create contract
        Proposal proposal = paymentRequest.getProposal();
        Project project = proposal.getProject();

        Contract contract = Contract.builder()
                .project(project)
                .proposal(proposal)
                .client(paymentRequest.getClient())
                .student(proposal.getStudent())
                .agreedPrice(proposal.getProposedPrice())
                .currency(proposal.getCurrency())
                .status(ContractStatus.ACTIVE)
                .startAt(LocalDateTime.now())
                .build();

        contract = contractRepository.save(contract);

        // 4. Create platform fee payment transaction (fee-only model)
        Transaction escrowTransaction = Transaction.builder()
                .contract(contract)
                .fromUser(paymentRequest.getClient())
                .toUser(null) // Platform receives fee
                .type(TransactionType.PLATFORM_FEE_PAYMENT)
                .amount(paymentRequest.getTotalAmount())
                .description("Platform fee (8%) received from client. Agreed price: " +
                        paymentRequest.getTotalAmount().add(paymentRequest.getStudentAmount()).toPlainString() +
                        " VND. Student will receive: " + paymentRequest.getStudentAmount().toPlainString() +
                        " VND directly from client.")
                .build();

        transactionRepository.save(escrowTransaction);

        // 5. Update project status
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);

        // 6. Create conversation for the contract
        Conversation conversation = Conversation.builder()
                .contract(contract)
                .build();
        conversationRepository.save(conversation);

        log.info("Payment verified and contract created: {}", contract.getContractId());

        return paymentRequestMapper.toDTO(paymentRequest);
    }

    @Override
    @Transactional
    public TransactionDTO releasePaymentToStudent(Long contractId, Long adminId) {
        PaymentRequest paymentRequest = paymentRequestRepository.findByProposal_ProposalId(
                contractRepository.findById(contractId)
                        .orElseThrow(() -> new NotFoundException("Contract not found"))
                        .getProposal().getProposalId())
                .orElseThrow(() -> new NotFoundException("Payment request not found"));

        throw new BadRequestException(
                "This platform operates on a fee-only model. " +
                "The student receives payment (" + paymentRequest.getStudentAmount().toPlainString() +
                ") directly from the client. " +
                "Platform does not hold or release project funds."
        );
    }

    @Override
    @Transactional
    public TransactionDTO refundPaymentToClient(Long contractId, Long adminId, String reason) {
        throw new BadRequestException(
                "Platform fee is non-refundable once verified. " +
                "For disputes, both parties' information will be disclosed for direct resolution. " +
                "Contact support for exceptional cases."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDTO> getPendingPayments(Pageable pageable) {
        return paymentRequestRepository.findByStatus(PaymentStatus.PENDING_PAYMENT, pageable)
                .map(paymentRequestMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentRequestDTO getPaymentRequestById(Long paymentRequestId) {
        PaymentRequest paymentRequest = paymentRequestRepository.findByIdWithDetails(paymentRequestId)
                .orElseThrow(() -> new NotFoundException("Payment request not found"));
        return paymentRequestMapper.toDTO(paymentRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentRequestDTO getPaymentRequestByProposalId(Long proposalId) {
        PaymentRequest paymentRequest = paymentRequestRepository.findByProposalIdWithDetails(proposalId)
                .orElseThrow(() -> new NotFoundException("Payment request not found for this proposal"));
        return paymentRequestMapper.toDTO(paymentRequest);
    }

    @Override
    @Transactional
    public void cancelPaymentRequest(Long paymentRequestId, Long clientId) {
        PaymentRequest paymentRequest = paymentRequestRepository.findById(paymentRequestId)
                .orElseThrow(() -> new NotFoundException("Payment request not found"));

        if (!paymentRequest.getClient().getUserId().equals(clientId)) {
            throw new ForbiddenException("Only the client who created the payment can cancel it");
        }

        if (!paymentRequest.getStatus().equals(PaymentStatus.PENDING_PAYMENT)) {
            throw new BadRequestException("Only pending payments can be cancelled");
        }

        paymentRequest.setStatus(PaymentStatus.CANCELLED);
        paymentRequestRepository.save(paymentRequest);

        // Reset proposal status
        Proposal proposal = paymentRequest.getProposal();
        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposalRepository.save(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getContractTransactions(Long contractId, Pageable pageable) {
        return transactionRepository.findByContract_ContractId(contractId, pageable)
                .map(transactionMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::toDTO);
    }

    // Helper methods

    private String generatePaymentReference() {
        String reference;
        do {
            reference = "SKILLNEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (paymentRequestRepository.existsByPaymentReference(reference));
        return reference;
    }

    private String generateQRCode(BigDecimal amount, String paymentReference) {
        try {
            String transferNote = "SKILLNEST " + paymentReference;
            byte[] qrCodeBytes = qrCodeGenerator.generateBankTransferQR(
                    bankCode, bankAccountNumber, bankAccountName, amount, transferNote
            );

            String qrFileName = paymentReference + ".png";
            return fileStorageService.storeBytes(qrCodeBytes, qrFileName, "qr-codes");
        } catch (Exception e) {
            log.error("Failed to generate QR code", e);
            throw new BadRequestException("Failed to generate QR code: " + e.getMessage());
        }
    }

    private String convertQRCodeToBase64(BigDecimal amount, String paymentReference) {
        try {
            String transferNote = "SKILLNEST " + paymentReference;
            byte[] qrCodeBytes = qrCodeGenerator.generateBankTransferQR(
                    bankCode,
                    bankAccountNumber,
                    bankAccountName,
                    amount,
                    transferNote
            );

            // Convert byte array to Base64 string with data URI prefix
            String base64Image = java.util.Base64.getEncoder().encodeToString(qrCodeBytes);
            return "data:image/png;base64," + base64Image;

        } catch (Exception e) {
            log.error("Failed to generate QR code base64", e);
            throw new BadRequestException("Failed to generate QR code: " + e.getMessage());
        }
    }
}
