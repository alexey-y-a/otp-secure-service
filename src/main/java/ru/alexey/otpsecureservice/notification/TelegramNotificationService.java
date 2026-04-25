package ru.alexey.otpsecureservice.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.alexey.otpsecureservice.notification.NotificationService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class TelegramNotificationService implements NotificationService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    private static final String API_URL = "https://api.telegram.org/bot";

    @Override
    public void send(String chatId, String code) {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Telegram bot token not configured. Skipping Telegram notification.");
            return;
        }

        try {
            String message = "Your OTP code is: *" + code + "*\nValid for 5 minutes.";
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

            String url = String.format("%s%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                    API_URL, botToken, chatId, encodedMessage);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Telegram message sent to chatId: {}", chatId);
            } else {
                log.error("Telegram API error: status={}, body={}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "TELEGRAM";
    }
}