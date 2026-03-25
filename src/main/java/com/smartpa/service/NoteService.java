package com.smartpa.service;

import com.smartpa.dto.DTOs.*;
import com.smartpa.model.Note;
import com.smartpa.model.User;
import com.smartpa.repository.NoteRepository;
import com.smartpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepo;
    private final UserRepository userRepo;

    public NoteResponse create(NoteRequest req, Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return toRes(noteRepo.save(Note.builder().title(req.getTitle()).content(req.getContent()).user(user).build()));
    }

    public List<NoteResponse> getAll(Long userId) {
        return noteRepo.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toRes).collect(Collectors.toList());
    }

    public NoteResponse update(Long id, NoteRequest req, Long userId) {
        Note n = noteRepo.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        if (!n.getUser().getId().equals(userId)) throw new RuntimeException("Unauthorized");
        n.setTitle(req.getTitle()); n.setContent(req.getContent());
        return toRes(noteRepo.save(n));
    }

    public void delete(Long id, Long userId) {
        Note n = noteRepo.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        if (!n.getUser().getId().equals(userId)) throw new RuntimeException("Unauthorized");
        noteRepo.delete(n);
    }

    public List<NoteResponse> search(String q, Long userId) {
        return noteRepo.findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndContentContainingIgnoreCase(
                userId, q, userId, q).stream().map(this::toRes).collect(Collectors.toList());
    }

    private NoteResponse toRes(Note n) {
        return NoteResponse.builder().id(n.getId()).title(n.getTitle())
            .content(n.getContent()).createdAt(n.getCreatedAt()).updatedAt(n.getUpdatedAt()).build();
    }
}
