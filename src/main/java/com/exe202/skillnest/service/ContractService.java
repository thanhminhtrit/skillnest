package com.exe202.skillnest.service;

import com.exe202.skillnest.dto.ContractDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractService {
    ContractDTO createContract(Long proposalId, String email);
    ContractDTO activateContract(Long contractId, String email);
    ContractDTO completeContract(Long contractId, String email);
    ContractDTO cancelContract(Long contractId, String email);
    Page<ContractDTO> getMyContracts(String email, Pageable pageable);
    ContractDTO getContractById(Long contractId, String email);
}
