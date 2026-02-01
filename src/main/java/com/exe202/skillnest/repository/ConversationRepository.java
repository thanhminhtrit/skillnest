package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByContractContractId(Long contractId);

    @Query("SELECT c FROM Conversation c WHERE c.conversationId = :conversationId AND (c.contract.client.userId = :userId OR c.contract.student.userId = :userId)")
    Optional<Conversation> findByConversationIdAndParticipant(Long conversationId, Long userId);
}

