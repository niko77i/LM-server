package com.lmserver.service;

/**
 * 通知服务接口 — 邮件和 Telegram 通知。
 * Phase 5: SMTP 和 Telegram Bot API 待对接。
 */
public interface NotificationService {

    /** 发送邮件通知（掉包检测等） */
    void sendEmail(String to, String subject, String content);

    /** 发送 Telegram 消息 */
    void sendTelegram(String message);
}
