package ru.alexey.otpsecureservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alexey.otpsecureservice.model.OtpCode;
import ru.alexey.otpsecureservice.model.OtpStatus;
import ru.alexey.otpsecureservice.repository.OtpCodeRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpRepository;
    private final OtpConfigService configService;

    private final Random random = new Random();

    public OtpCode generateOtp(String username, String destination, String channel, String operationId) {
        int length = configService.getCodeLength();
        int ttlMinutes = configService.getTtlMinutes();

        String code = String.format("%0" + length + "d", random.nextInt((int) Math.pow(10, length)));

        OtpCode otp = OtpCode.builder()
                .code(code)
                .username(username)
                .destination(destination)
                .channel(channel)
                .operationId(operationId)
                .expiryDate(LocalDateTime.now().plusMinutes(ttlMinutes))
                .status(OtpStatus.ACTIVE)
                .used(false)
                .build();

        OtpCode saved = otpRepository.save(otp);
        log.info("Generated OTP for user {} via {}: {}", username, channel, code);

        return saved;
    }

    public boolean validateOtp(String username, String code) {
        var otpOpt = otpRepository.findByUsernameAndCodeAndStatus(username, code, OtpStatus.ACTIVE);

        if (otpOpt.isEmpty()) {
            log.warn("Invalid OTP attempt for user {}: code {} not found or not active", username, code);
            return false;
        }

        OtpCode otp = otpOpt.get();

        if (otp.getExpiryDate().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
            log.warn("OTP expired for user {}: {}", username, code);
            return false;
        }

        if (otp.isUsed()) {
            log.warn("OTP already used for user {}: {}", username, code);
            return false;
        }

        otp.setUsed(true);
        otp.setStatus(OtpStatus.USED);
        otpRepository.save(otp);

        log.info("OTP validated successfully for user {}: {}", username, code);
        return true;
    }
}