package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.PaymentAcceptanceResponse;
import com.exe202.skillnest.dto.PaymentRequestDTO;
import com.exe202.skillnest.dto.TransactionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    /**
     * Client accepts proposal and initiates payment request
     */
    PaymentAcceptanceResponse acceptProposalWithPayment(Long proposalId, Long clientId);

    /**
     * Admin/Manager verifies payment and creates contract
     */
    PaymentRequestDTO verifyPayment(Long paymentRequestId, Long verifierId);

    /**
     * Admin releases payment to student after project completion
     */
    TransactionDTO releasePaymentToStudent(Long contractId, Long adminId);

    /**
     * Admin refunds payment to client (in dispute cases)
     */
    TransactionDTO refundPaymentToClient(Long contractId, Long adminId, String reason);

    /**
     * Get all pending payment requests (Admin/Manager)
     */
    Page<PaymentRequestDTO> getPendingPayments(Pageable pageable);

    /**
     * Get payment request by ID
     */
    PaymentRequestDTO getPaymentRequestById(Long paymentRequestId);

    /**
     * Get payment request by proposal ID
     */
    PaymentRequestDTO getPaymentRequestByProposalId(Long proposalId);

    /**
     * Cancel payment request before verification
     */
    void cancelPaymentRequest(Long paymentRequestId, Long clientId);

    /**
     * Get transactions for a contract
     */
    Page<TransactionDTO> getContractTransactions(Long contractId, Pageable pageable);

    /**
     * Get all transactions (Admin only)
     */
    Page<TransactionDTO> getAllTransactions(Pageable pageable);
}

