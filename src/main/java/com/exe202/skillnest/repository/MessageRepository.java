package com.exe202.skillnest.repository;

import com.exe202.skillnest.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);
}

