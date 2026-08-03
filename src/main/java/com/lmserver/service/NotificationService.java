package com.lmserver.service;

/**
 * 通知服务接口 — SMTP 邮件和 Telegram Bot 通知。
 */
public interface NotificationService {

    /** 发送邮件通知（掉包检测等） */
    void sendEmail(String to, String subject, String content);

    /** 发送 Telegram 消息 */
    void sendTelegram(String message);
}
