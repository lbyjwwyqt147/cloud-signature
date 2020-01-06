package pers.liujunyi.cloud.signature.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/***
 *　DateTimeUtils
 *  时间格式化　工具类
 */
public final class DateTimeUtils {

    private static final String YMDHMS = "yyyy-MM-dd HH:mm:ss";
    private static final String YMD = "yyyy-MM-dd";

    private DateTimeUtils() { }

    /**
     * 将当前时间转为字符串格式　yyyy-MM-dd HH:mm:ss
     * @return 2018-10-28 17:07:05
     */
    public static String getCurrentDateTimeAsString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YMDHMS);
        return LocalDateTime.now().format(formatter);
    }

    /**
     * 获取当前年月日
     * @return 2018-10-28
     */
    public static  Date getCurrentDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YMD);
        LocalDate localDate = LocalDate.now();
        try {
            return sDateFormat.parse(localDate.format(formatter));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取当前年月日
     * @return 2018-10-28
     */
    public static  String getCurrentDateAsString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YMD);
        LocalDate localDate = LocalDate.now();
        return localDate.format(formatter);
    }

    /**
     * 当前时间基础上追加年份
     * @param year
     * @return
     */
    public static Date additionalYear(long year) {
        LocalDateTime localDateTime = LocalDateTime.now().plusYears(year);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date = Date.from(zdt.toInstant());
        return  date;
    }


    /**
     * 获取当前年份
     * @return
     */
    public static Integer getCurrentYear() {
        // 取当前日期：
        LocalDate today = LocalDate.now();
        return today.getDayOfYear();
    }

    /**
     * 获取过去几年的年份
     * @param number  过去几年值
     * @return
     */
    public static Integer getCurrentBeforeYear(int number) {
        // 取当前日期：
        LocalDate today = LocalDate.now();
        return today.getDayOfYear() - number;
    }

}
