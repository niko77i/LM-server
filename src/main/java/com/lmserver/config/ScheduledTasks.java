package com.lmserver.config;

import com.lmserver.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务调度 — 掉包检测 + 周清理，通过 NotificationService 发送邮件/Telegram。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final NotificationService notificationService;

    /** 每周日凌晨2点执行数据清理 */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyCleanup() {
        log.info("[定时任务] 每周清理开始");
        // TODO: 清理过期软删除记录
        log.info("[定时任务] 每周清理完成");
    }

    /** 每日上午9点执行掉包检测 */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkDelist() {
        log.info("[定时任务] 掉包检测开始");
        // TODO: 遍历活跃产品，Jsoup 检查 Google Play 下架
        // 发现掉包 → notificationService.sendEmail(to, subject, body)
        //           → notificationService.sendTelegram(message)
        log.info("[定时任务] 掉包检测完成");
    }

    /** 测试通知（手动触发） */
    public void testNotify(String email) {
        notificationService.sendEmail(email, "[LM-Server] 测试邮件",
                "这是一封来自 LM-Server 的测试邮件。\n定时任务通知系统正常运行。");
        notificationService.sendTelegram("<b>LM-Server</b> 测试通知\n定时任务系统正常运行。");
    }
}
