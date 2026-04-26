package ru.alexey.otpsecureservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.alexey.otpsecureservice.model.OtpCode;
import ru.alexey.otpsecureservice.notification.NotificationFacade;
import ru.alexey.otpsecureservice.service.OtpService;
import ru.alexey.otpsecureservice.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final NotificationFacade notificationFacade;
    private final UserService userService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateOtp(
            @RequestParam String channel,
            @RequestParam String destination,
            @RequestParam(required = false) String operationId,
            Authentication auth) {

        String username = auth.getName();
        log.info("POST /api/otp/generate - user: {}, channel: {}, destination: {}, operationId: {}",
                username, channel, destination, operationId);

        OtpCode otp = otpService.generateOtp(username, destination, channel, operationId);

        notificationFacade.send(channel, destination, otp.getCode());

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP code sent via " + channel);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateOtp(
            @RequestParam String code,
            Authentication auth) {

        String username = auth.getName();
        log.info("POST /api/otp/validate - user: {}, code: {}", username, code);

        boolean valid = otpService.validateOtp(username, code);

        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", valid);

        if (valid) {
            log.info("OTP validated successfully for user: {}", username);
        } else {
            log.warn("OTP validation failed for user: {}", username);
        }

        return ResponseEntity.ok(response);
    }
}