package ru.alexey.otpsecureservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "otp_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private int codeLength = 6;

    @Builder.Default
    private int ttlMinutes = 5;
}