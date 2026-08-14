package com.lmserver.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 高频轮询接口 SQL 日志屏蔽。
 *
 * <p>/api/delist/pending 每 30 秒被前端轮询一次，会重复打印 MyBatis 的 SQL 查询日志，
 * 干扰其他日志阅读。通过 MDC 标记 + logback TurboFilter，仅屏蔽该接口触发的
 * mapper SQL 日志，其他接口的 SQL 日志照常打印。</p>
 */
@Component
public class SqlLogFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "SKIP_SQL_LOG";
    private static final Set<String> SKIP_PATHS = Set.of("/api/delist/pending");

    /** 注册全局 TurboFilter：当 MDC 有跳过标记且 logger 属于 mapper 包时，丢弃日志。 */
    @PostConstruct
    public void registerTurboFilter() {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        ctx.addTurboFilter(new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, Logger logger, Level level,
                                      String format, Object[] params, Throwable t) {
                if ("1".equals(MDC.get(MDC_KEY))
                        && logger.getName().startsWith("com.lmserver.mapper")) {
                    return FilterReply.DENY;
                }
                return FilterReply.NEUTRAL;
            }
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        boolean skip = SKIP_PATHS.contains(request.getRequestURI());
        if (skip) {
            MDC.put(MDC_KEY, "1");
        }
        try {
            chain.doFilter(request, response);
        } finally {
            if (skip) {
                MDC.remove(MDC_KEY);
            }
        }
    }
}
