package com.smartpa.repository;

import com.smartpa.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserIdOrderByTimestampDesc(Long userId);
    List<ChatHistory> findTop50ByUserIdOrderByTimestampDesc(Long userId);
    List<ChatHistory> findTop10ByUserIdOrderByTimestampDesc(Long userId);
    long countByUserId(Long userId);
}
