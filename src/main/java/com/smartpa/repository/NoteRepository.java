package com.smartpa.repository;
import com.smartpa.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Page<Note> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    List<Note> findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndContentContainingIgnoreCase(
        Long uid1, String title, Long uid2, String content);
}
