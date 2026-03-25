package com.smartpa.controller;

import com.smartpa.dto.DTOs.*;
import com.smartpa.security.JwtUtil;
import com.smartpa.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/notes") @RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getAll(@RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("OK", noteService.getAll(uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> create(@RequestBody NoteRequest req, @RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("Created", noteService.create(req, uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoteResponse>> update(@PathVariable Long id, @RequestBody NoteRequest req, @RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("Updated", noteService.update(id, req, uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, @RequestHeader("Authorization") String auth) {
        try { noteService.delete(id, uid(auth)); return ResponseEntity.ok(ApiResponse.success("Deleted", null)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> search(@RequestParam String q, @RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("OK", noteService.search(q, uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    private Long uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }
}
