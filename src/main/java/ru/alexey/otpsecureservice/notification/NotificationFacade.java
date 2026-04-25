package ru.alexey.otpsecureservice.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final Map<String, NotificationService> services = new ConcurrentHashMap<>();

    private final EmailNotificationService emailService;
    private final FileNotificationService fileService;
    private final SmsNotificationService smsService;
    private final TelegramNotificationService telegramService;

    @jakarta.annotation.PostConstruct
    public void init() {
        services.put(emailService.getType(), emailService);
        services.put(fileService.getType(), fileService);
        services.put(smsService.getType(), smsService);
        services.put(telegramService.getType(), telegramService);
        log.info("Notification services registered: {}", services.keySet());
    }

    public void send(String channel, String destination, String code) {
        NotificationService service = services.get(channel.toUpperCase());
        if (service == null) {
            log.error("Unknown notification channel: {}", channel);
            throw new IllegalArgumentException("Unknown channel: " + channel);
        }
        service.send(destination, code);
    }
}