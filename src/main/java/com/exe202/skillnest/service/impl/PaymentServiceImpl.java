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

    @Value("${azure.storage.qr-base-url:https://skillnest.blob.core.windows.net/qrcodes/}")
    private String qrBaseUrl;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    @Transactional
    public PaymentAcceptanceResponse acceptProposalWithPayment(Long proposalId, Long clientId) {
        log.info("Client {} accepting proposal {} with payment", clientId, proposalId);

        // 1. Validate proposal
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if (!proposal.getStatus().equals(ProposalStatus.SUBMITTED)) {
            throw new BadRequestException("Proposal is not in SUBMITTED status");
        }

        // 2. Validate client is the project owner
        Project project = proposal.getProject();
        if (!project.getClient().getUserId().equals(clientId)) {
            throw new ForbiddenException("Only project owner can accept proposals");
        }

        // 3. Check if payment request already exists
        if (paymentRequestRepository.findByProposal_ProposalId(proposalId).isPresent()) {
            throw new BadRequestException("Payment request already exists for this proposal");
        }

        // 4. Calculate amounts - CHỈ LẤY 8% PHÍ DUY TRÌ NỀN TẢNG TỪ BUDGETMAX
        BigDecimal budgetMax = project.getBudgetMax();
        if (budgetMax == null || budgetMax.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Project budgetMax is not set or invalid");
        }

        // Chỉ tính phí nền tảng 8% từ budgetMax
        BigDecimal platformFee = budgetMax.multiply(platformFeePercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = platformFee; // Client chỉ phải trả phí nền tảng
        BigDecimal studentAmount = proposal.getProposedPrice(); // Student nhận đúng số tiền đã đề xuất

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
                .message("Please transfer " + platformFee + " VND (8% platform maintenance fee) to proceed. Contract will be created after payment verification.")
                .build();
    }

    @Override
    @Transactional
    public PaymentRequestDTO verifyPayment(Long paymentRequestId, Long verifierId) {
        log.info("Verifier {} verifying payment request {}", verifierId, paymentRequestId);

        // 1. Get payment request
        PaymentRequest paymentRequest = paymentRequestRepository.findById(paymentRequestId)
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

        // 4. Create escrow deposit transaction
        Transaction escrowTransaction = Transaction.builder()
                .contract(contract)
                .fromUser(paymentRequest.getClient())
                .toUser(null) // Platform holds the money
                .type(TransactionType.ESCROW_DEPOSIT)
                .amount(paymentRequest.getTotalAmount())
                .description("Client payment deposited to platform escrow for contract #" + contract.getContractId())
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
        log.info("Admin {} releasing payment for contract {}", adminId, contractId);

        // 1. Validate contract
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (!contract.getStatus().equals(ContractStatus.COMPLETED)) {
            throw new BadRequestException("Contract must be COMPLETED to release payment");
        }

        // 2. Check if payment already released
        if (!transactionRepository.findByContract_ContractIdAndType(contractId, TransactionType.PAYOUT).isEmpty()) {
            throw new BadRequestException("Payment already released for this contract");
        }

        // 3. Get payment request
        PaymentRequest paymentRequest = paymentRequestRepository.findByProposal_ProposalId(contract.getProposal().getProposalId())
                .orElseThrow(() -> new NotFoundException("Payment request not found"));

        if (!paymentRequest.getStatus().equals(PaymentStatus.PAID)) {
            throw new BadRequestException("Payment is not in PAID status");
        }

        // 4. Create payout transaction to student
        Transaction payoutTransaction = Transaction.builder()
                .contract(contract)
                .fromUser(null) // Platform releases the money
                .toUser(contract.getStudent())
                .type(TransactionType.PAYOUT)
                .amount(paymentRequest.getStudentAmount())
                .description("Payment released to student for completed contract #" + contractId)
                .build();

        payoutTransaction = transactionRepository.save(payoutTransaction);

        // 5. Create platform fee transaction
        Transaction feeTransaction = Transaction.builder()
                .contract(contract)
                .fromUser(null)
                .toUser(null)
                .type(TransactionType.PLATFORM_FEE)
                .amount(paymentRequest.getPlatformFee())
                .description("Platform commission (8%) for contract #" + contractId)
                .build();

        transactionRepository.save(feeTransaction);

        // 6. Update payment request status
        paymentRequest.setStatus(PaymentStatus.RELEASED);
        paymentRequestRepository.save(paymentRequest);

        log.info("Payment released to student: {}", payoutTransaction.getTransactionId());

        return transactionMapper.toDTO(payoutTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO refundPaymentToClient(Long contractId, Long adminId, String reason) {
        log.info("Admin {} refunding payment for contract {}", adminId, contractId);

        // 1. Validate contract
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        // 2. Check if already refunded or released
        if (!transactionRepository.findByContract_ContractIdAndType(contractId, TransactionType.REFUND).isEmpty()) {
            throw new BadRequestException("Payment already refunded for this contract");
        }

        if (!transactionRepository.findByContract_ContractIdAndType(contractId, TransactionType.PAYOUT).isEmpty()) {
            throw new BadRequestException("Payment already released to student, cannot refund");
        }

        // 3. Get payment request
        PaymentRequest paymentRequest = paymentRequestRepository.findByProposal_ProposalId(contract.getProposal().getProposalId())
                .orElseThrow(() -> new NotFoundException("Payment request not found"));

        // 4. Create refund transaction
        Transaction refundTransaction = Transaction.builder()
                .contract(contract)
                .fromUser(null) // Platform refunds
                .toUser(contract.getClient())
                .type(TransactionType.REFUND)
                .amount(paymentRequest.getTotalAmount())
                .description("Refund to client for contract #" + contractId + ". Reason: " + reason)
                .build();

        refundTransaction = transactionRepository.save(refundTransaction);

        // 5. Update payment request status
        paymentRequest.setStatus(PaymentStatus.REFUNDED);
        paymentRequestRepository.save(paymentRequest);

        // 6. Update contract status
        contract.setStatus(ContractStatus.CANCELLED);
        contractRepository.save(contract);

        log.info("Payment refunded to client: {}", refundTransaction.getTransactionId());

        return transactionMapper.toDTO(refundTransaction);
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
        PaymentRequest paymentRequest = paymentRequestRepository.findById(paymentRequestId)
                .orElseThrow(() -> new NotFoundException("Payment request not found"));
        return paymentRequestMapper.toDTO(paymentRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentRequestDTO getPaymentRequestByProposalId(Long proposalId) {
        PaymentRequest paymentRequest = paymentRequestRepository.findByProposal_ProposalId(proposalId)
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
                    bankCode,
                    bankAccountNumber,
                    bankAccountName,
                    amount,
                    transferNote
            );

            // Upload to Azure Blob Storage and return URL
            String qrFileName = paymentReference + ".png";
            return qrBaseUrl + qrFileName;

            // TODO: Implement Azure Blob Storage upload
            // return azureBlobStorageService.uploadQRCode(qrCodeBytes, qrFileName);

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
