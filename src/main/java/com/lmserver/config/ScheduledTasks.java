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
    private final com.lmserver.service.DelistChecker delistChecker;

    private final com.lmserver.mapper.gg.AccountsMapper accountsMapper;
    private final com.lmserver.mapper.fb.FbBmsMapper fbBmsMapper;
    private final com.lmserver.mapper.fb.FbAccountsMapper fbAccountsMapper;

    /** 每周日凌晨2点清理30天前软删除的记录 */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyCleanup() {
        log.info("[定时任务] 每周清理开始");
        var cutoff = java.time.LocalDateTime.now().minusDays(30);
        accountsMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lmserver.entity.gg.Accounts>()
                .isNotNull(com.lmserver.entity.gg.Accounts::getDeletedAt).lt(com.lmserver.entity.gg.Accounts::getDeletedAt, cutoff));
        fbBmsMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lmserver.entity.fb.FbBms>()
                .isNotNull(com.lmserver.entity.fb.FbBms::getDeletedAt).lt(com.lmserver.entity.fb.FbBms::getDeletedAt, cutoff));
        fbAccountsMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lmserver.entity.fb.FbAccounts>()
                .isNotNull(com.lmserver.entity.fb.FbAccounts::getDeletedAt).lt(com.lmserver.entity.fb.FbAccounts::getDeletedAt, cutoff));
        log.info("[定时任务] 每周清理完成");
    }

    /** 每小时执行掉包检测 */
    @Scheduled(cron = "0 0 * * * *")
    public void checkDelist() {
        log.info("[定时任务] 掉包检测开始");
        delistChecker.checkAll();
    }

    /** 测试通知（手动触发） */
    public void testNotify(String email) {
        notificationService.sendEmail(email, "[LM-Server] 测试邮件",
                "这是一封来自 LM-Server 的测试邮件。\n定时任务通知系统正常运行。");
        notificationService.sendTelegram("<b>LM-Server</b> 测试通知\n定时任务系统正常运行。");
    }
}
