package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.DisputeDTO;
import com.exe202.skillnest.payloads.request.CreateDisputeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DisputeService {
    DisputeDTO createDispute(CreateDisputeRequest request, String email);
    Page<DisputeDTO> getMyDisputes(String email, Pageable pageable);
    DisputeDTO getDisputeById(Long disputeId, String email);
    DisputeDTO updateDisputeStatus(Long disputeId, String status, String email);
    Page<DisputeDTO> getDisputesByContractId(Long contractId, String email, Pageable pageable);
}
