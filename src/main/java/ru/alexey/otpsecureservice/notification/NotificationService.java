package ru.alexey.otpsecureservice.notification;

public interface NotificationService {
    void send(String destination, String code);
    String getType();
}