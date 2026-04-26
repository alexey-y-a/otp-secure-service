package ru.alexey.otpsecureservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OtpSecureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtpSecureServiceApplication.class, args);
    }
}