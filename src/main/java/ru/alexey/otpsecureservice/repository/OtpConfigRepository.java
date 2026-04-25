package ru.alexey.otpsecureservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexey.otpsecureservice.model.OtpConfig;

public interface OtpConfigRepository extends JpaRepository<OtpConfig, Long> {
}