package ru.alexey.otpsecureservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.alexey.otpsecureservice.dto.AuthResponse;
import ru.alexey.otpsecureservice.dto.RegisterRequest;
import ru.alexey.otpsecureservice.model.Role;
import ru.alexey.otpsecureservice.model.User;
import ru.alexey.otpsecureservice.repository.UserRepository;
import ru.alexey.otpsecureservice.security.JwtUtil;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username {} already exists", request.getUsername());
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        boolean isFirstUser = userRepository.count() == 0;
        Set<Role> roles = isFirstUser ? Set.of(Role.ADMIN) : Set.of(Role.USER);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);
        log.info("User {} registered with roles: {}", request.getUsername(), roles);

        List<String> roleNames = roles.stream().map(Role::name).toList();
        String token = jwtUtil.generateToken(user.getUsername(), roleNames);

        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(String username, String password) {
        log.info("Login attempt for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed: user {} not found", username);
                    return new RuntimeException("Неверный логин или пароль");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed: invalid password for user {}", username);
            throw new RuntimeException("Неверный логин или пароль");
        }

        List<String> roleNames = user.getRoles().stream().map(Role::name).toList();
        String token = jwtUtil.generateToken(user.getUsername(), roleNames);

        log.info("User {} logged in successfully", username);
        return new AuthResponse(token, user.getUsername());
    }
}