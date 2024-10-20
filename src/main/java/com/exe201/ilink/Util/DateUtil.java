package com.exe201.ilink.Util;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@UtilityClass
public class DateUtil {
    private final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String formatTimestamp(Date date) {
        return formatTimestamp(date, DEFAULT_DATE_FORMAT);
    }

    public static String formatTimestamp(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Lấy khoảng thời gian trước 1 tháng của startDate và endDate
    public static LocalDate[] getPreviousMonthRange(Date startDate, Date endDate) {
        LocalDate start = toLocalDate(startDate).minusMonths(1); // Lùi lại 1 tháng
        LocalDate end = toLocalDate(endDate).minusMonths(1); // Lùi lại 1 tháng
        return new LocalDate[] {start, end};
    }

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
