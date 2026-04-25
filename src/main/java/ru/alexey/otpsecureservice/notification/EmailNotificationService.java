package ru.alexey.otpsecureservice.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void send(String email, String code) {
        log.info("Sending OTP to email: {}", email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your verification code is: " + code + "\nValid for 5 minutes.");
        mailSender.send(message);
        log.info("OTP sent to email: {}", email);
    }

    @Override
    public String getType() {
        return "EMAIL";
    }
}