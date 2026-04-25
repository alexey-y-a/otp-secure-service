package ru.alexey.otpsecureservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alexey.otpsecureservice.dto.AuthRequest;
import ru.alexey.otpsecureservice.dto.AuthResponse;
import ru.alexey.otpsecureservice.dto.RegisterRequest;
import ru.alexey.otpsecureservice.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - username: {}", request.getUsername());
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("POST /api/auth/login - username: {}", request.getUsername());
        AuthResponse response = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}