package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Transaction;
import com.exe202.skillnest.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByContract_ContractId(Long contractId, Pageable pageable);

    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.fromUser.userId = :userId OR t.toUser.userId = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    List<Transaction> findByContract_ContractIdAndType(Long contractId, TransactionType type);
}

