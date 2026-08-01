package com.lmserver.service.impl;

import com.lmserver.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 通知服务实现 — SMTP 邮件 + Telegram Bot。
 * 配置来自 GG-Server config.json。
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${notification.telegram.bot-token}")
    private String botToken;

    @Value("${notification.telegram.chat-id}")
    private String chatId;

    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async("ggAsyncExecutor")
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setFrom(fromEmail, "LM-Server");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            mailSender.send(msg);
            log.info("[邮件] 已发送 → {} : {}", to, subject);
        } catch (Exception e) {
            log.error("[邮件] 发送失败: {}", e.getMessage());
        }
    }

    @Override
    @Async("ggAsyncExecutor")
    public void sendTelegram(String message) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", message,
                "parse_mode", "HTML",
                "disable_web_page_preview", true
            );
            restTemplate.postForEntity(url, body, String.class);
            log.info("[Telegram] 已发送");
        } catch (Exception e) {
            log.error("[Telegram] 发送失败: {}", e.getMessage());
        }
    }
}
