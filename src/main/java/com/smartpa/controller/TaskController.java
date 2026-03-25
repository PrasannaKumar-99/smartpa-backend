package com.smartpa.controller;

import com.smartpa.dto.DTOs.*;
import com.smartpa.security.JwtUtil;
import com.smartpa.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/tasks") @RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(@RequestBody TaskRequest req, @RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("Created", taskService.createTask(req, uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAll(@RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("OK", taskService.getTasks(uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable Long id, @RequestBody TaskRequest req, @RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("Updated", taskService.updateTask(id, req, uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> complete(
            @PathVariable Long id,
            @RequestBody(required = false) TaskRequest req,
            @RequestHeader("Authorization") String auth) {
        try {
            String note = (req != null) ? req.getCompletionNote() : null;
            return ResponseEntity.ok(ApiResponse.success("Completed", taskService.completeTask(id, note, uid(auth))));
        } catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<TaskResponse>> reopen(@PathVariable Long id, @RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("Reopened", taskService.reopenTask(id, uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id, @RequestHeader("Authorization") String auth) {
        try { taskService.cancelTask(id, uid(auth)); return ResponseEntity.ok(ApiResponse.success("Cancelled", null)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, @RequestHeader("Authorization") String auth) {
        try { taskService.deleteTask(id, uid(auth)); return ResponseEntity.ok(ApiResponse.success("Deleted", null)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> stats(@RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("OK", taskService.getStats(uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    private Long uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }
}
