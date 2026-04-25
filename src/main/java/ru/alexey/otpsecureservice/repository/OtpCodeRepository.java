package ru.alexey.otpsecureservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexey.otpsecureservice.model.OtpCode;
import ru.alexey.otpsecureservice.model.OtpStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    List<OtpCode> findByUsernameAndStatus(String username, OtpStatus status);
    List<OtpCode> findByExpiryDateBeforeAndStatus(LocalDateTime now, OtpStatus status);
}