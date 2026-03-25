package com.smartpa.controller;

import com.smartpa.dto.DTOs.*;
import com.smartpa.security.JwtUtil;
import com.smartpa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("OK", userService.getProfile(uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @RequestBody UpdateProfileRequest req,
            @RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("Updated", userService.updateProfile(uid(auth), req))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest req,
            @RequestHeader("Authorization") String auth) {
        try { userService.changePassword(uid(auth), req); return ResponseEntity.ok(ApiResponse.success("Password changed", null)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(@RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("OK", userService.getActivity(uid(auth)))); }
        catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    private Long uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }
}
