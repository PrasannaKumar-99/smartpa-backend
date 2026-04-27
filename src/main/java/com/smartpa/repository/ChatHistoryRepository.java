package com.smartpa.repository;

import com.smartpa.model.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserIdOrderByTimestampDesc(Long userId);
    List<ChatHistory> findTop50ByUserIdOrderByTimestampDesc(Long userId);
    List<ChatHistory> findTop10ByUserIdOrderByTimestampDesc(Long userId);
    Page<ChatHistory> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    long countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM ChatHistory c WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
