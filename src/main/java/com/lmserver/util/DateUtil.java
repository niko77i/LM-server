package com.lmserver.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类。
 */
public final class DateUtil {

    public static final DateTimeFormatter STANDARD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtil() {}

    public static String now() {
        return LocalDateTime.now().format(STANDARD);
    }

    public static String today() {
        return LocalDate.now().format(DATE_ONLY);
    }

    public static String format(LocalDateTime dt) {
        return dt != null ? dt.format(STANDARD) : "";
    }

    public static String formatDate(LocalDate d) {
        return d != null ? d.format(DATE_ONLY) : "";
    }
}
