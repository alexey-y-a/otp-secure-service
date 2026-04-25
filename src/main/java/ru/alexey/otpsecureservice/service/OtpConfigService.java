package ru.alexey.otpsecureservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.alexey.otpsecureservice.model.OtpConfig;
import ru.alexey.otpsecureservice.model.OtpStatus;
import ru.alexey.otpsecureservice.repository.OtpCodeRepository;
import ru.alexey.otpsecureservice.repository.OtpConfigRepository;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class OtpConfigService {

    private final OtpConfigRepository configRepository;
    private final OtpCodeRepository otpCodeRepository;

    private OtpConfig currentConfig;

    @PostConstruct
    public void init() {
        currentConfig = configRepository.findAll().stream().findFirst()
                .orElseGet(() -> configRepository.save(OtpConfig.builder().build()));
        log.info("OTP Config loaded: length={}, ttl={}min", currentConfig.getCodeLength(), currentConfig.getTtlMinutes());
    }

    public int getCodeLength() {
        return currentConfig.getCodeLength();
    }

    public int getTtlMinutes() {
        return currentConfig.getTtlMinutes();
    }

    public void updateConfig(int codeLength, int ttlMinutes) {
        currentConfig.setCodeLength(codeLength);
        currentConfig.setTtlMinutes(ttlMinutes);
        configRepository.save(currentConfig);
        log.info("OTP Config updated: length={}, ttl={}min", codeLength, ttlMinutes);
    }

    @Scheduled(fixedRate = 60000)
    public void expireOldCodes() {
        int updated = otpCodeRepository.findByExpiryDateBeforeAndStatus(java.time.LocalDateTime.now(), OtpStatus.ACTIVE)
                .stream()
                .peek(otp -> {
                    otp.setStatus(OtpStatus.EXPIRED);
                    otpCodeRepository.save(otp);
                })
                .mapToInt(o -> 1)
                .sum();

        if (updated > 0) {
            log.info("Expired {} OTP codes", updated);
        }
    }
}