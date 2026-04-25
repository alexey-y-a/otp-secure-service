package ru.alexey.otpsecureservice.notification;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationService implements NotificationService {

    @Value("${smpp.host:localhost}")
    private String host;

    @Value("${smpp.port:2775}")
    private int port;

    @Value("${smpp.system_id:smppclient1}")
    private String systemId;

    @Value("${smpp.password:password}")
    private String password;

    @Value("${smpp.source_addr:OTPService}")
    private String sourceAddress;

    @PostConstruct
    public void init() {
        log.info("SMPP Service initialized with host: {}, port: {}", host, port);
    }

    @Override
    public void send(String phoneNumber, String code) {
        SMPPSession session = new SMPPSession();
        try {
            log.info("Connecting to SMPP server at {}:{}", host, port);
            session.connectAndBind(host, port, new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    "OTP",
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    null
            ));

            String message = "Your OTP code: " + code;

            var result = session.submitShortMessage(
                    "OTP",
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    phoneNumber,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    message.getBytes()
            );

            log.info("SMS sent to {} with messageId: {}, code: {}", phoneNumber, result.getMessageId(), code);

        } catch (Exception e) {
            log.error("Failed to send SMS via SMPP: {}", e.getMessage());
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception e) {
                log.warn("Error closing SMPP session: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getType() {
        return "SMS";
    }
}