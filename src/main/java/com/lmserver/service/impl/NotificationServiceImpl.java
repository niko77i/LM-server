package com.lmserver.service.impl;

import com.lmserver.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现 — Phase 5 占位，后续对接 SMTP 和 Telegram Bot API。
 * 当前仅记录日志，不发送实际通知。
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendEmail(String to, String subject, String content) {
        // TODO: Phase 5 对接 Spring Mail (JavaMailSender)
        log.info("[邮件通知] 收件人: {}, 主题: {}", to, subject);
    }

    @Override
    public void sendTelegram(String message) {
        // TODO: Phase 5 对接 Telegram Bot API (RestTemplate)
        log.info("[Telegram通知] {}", message.substring(0, Math.min(100, message.length())));
    }
}
