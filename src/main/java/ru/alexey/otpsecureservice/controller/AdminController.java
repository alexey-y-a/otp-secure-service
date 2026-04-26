package ru.alexey.otpsecureservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.alexey.otpsecureservice.model.User;
import ru.alexey.otpsecureservice.repository.OtpCodeRepository;
import ru.alexey.otpsecureservice.repository.UserRepository;
import ru.alexey.otpsecureservice.service.OtpConfigService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final OtpConfigService otpConfigService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /api/admin/users - ADMIN access");
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /api/admin/users/{} - ADMIN access", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpCodeRepository.deleteAll(otpCodeRepository.findByUsernameAndStatus(
                user.getUsername(), ru.alexey.otpsecureservice.model.OtpStatus.ACTIVE));

        userRepository.delete(user);

        log.info("User {} and associated OTP codes deleted", user.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/config")
    public ResponseEntity<Map<String, String>> updateOtpConfig(
            @RequestParam int codeLength,
            @RequestParam int ttlMinutes) {

        log.info("PUT /api/admin/config - ADMIN access: length={}, ttl={}min", codeLength, ttlMinutes);

        if (codeLength < 4 || codeLength > 8) {
            throw new IllegalArgumentException("Code length must be between 4 and 8");
        }
        if (ttlMinutes < 1 || ttlMinutes > 30) {
            throw new IllegalArgumentException("TTL must be between 1 and 30 minutes");
        }

        otpConfigService.updateConfig(codeLength, ttlMinutes);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP config updated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Integer>> getOtpConfig() {
        log.info("GET /api/admin/config - ADMIN access");
        Map<String, Integer> config = new HashMap<>();
        config.put("codeLength", otpConfigService.getCodeLength());
        config.put("ttlMinutes", otpConfigService.getTtlMinutes());
        return ResponseEntity.ok(config);
    }
}