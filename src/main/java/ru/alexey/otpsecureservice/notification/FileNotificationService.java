package ru.alexey.otpsecureservice.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

@Slf4j
@Service
public class FileNotificationService implements NotificationService {

    private static final String FILE_PATH = "otp_codes.log";

    @Override
    public void send(String username, String code) {
        try {
            String line = String.format("[%s] User: %s | Code: %s%n", LocalDateTime.now(), username, code);
            Files.write(Paths.get(FILE_PATH), line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("OTP saved to file for user: {}", username);
        } catch (IOException e) {
            log.error("Failed to write OTP to file: {}", e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "FILE";
    }
}