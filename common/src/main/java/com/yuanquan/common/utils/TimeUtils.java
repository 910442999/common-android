package com.yuanquan.common.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * timeCompare 判断2个时间大小
 */
public class TimeUtils {
    /**
     * 设置每个阶段时间
     */
    private static final int seconds_of_1minute = 60;

    private static final int seconds_of_30minutes = 30 * 60;

    private static final int seconds_of_1hour = 60 * 60;

    private static final int seconds_of_1day = 24 * 60 * 60;

    private static final int seconds_of_15days = seconds_of_1day * 15;

    private static final int seconds_of_30days = seconds_of_1day * 30;

    private static final int seconds_of_6months = seconds_of_30days * 6;

    private static final int seconds_of_1year = seconds_of_30days * 12;
    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm";
    public static final String yyyy_MM_dd = "yyyy-MM-dd";
    public static final String MM_dd = "MM-dd";
    public static final String MM_dd_HH_mm = "MM-dd HH:mm";
    public static final String HH_mm_ss = "HH:mm:ss";


    //时间转换 2018-12-27T16:00:00.000Z” 转换成“yyyy-MM-dd”
    public static String getPreCallbackTime(String timeData) {
        if (TextUtils.isEmpty(timeData)) {
            return "";
        }
        if (!timeData.contains("T")) {
            return timeData;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
        try {
            Date time = format.parse(timeData);
            String date = time.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
            Date datetime = (Date) sdf.parse(date);
            /*datetime = new java.sql.Date(datetime.getTime());*/
            timeData = new SimpleDateFormat("MM-dd HH:mm").format(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeData;
    }

    //时间转换 2018-12-27T16:00:00.000Z” 转换成“yyyy-MM-dd”
    public static String getPreCallbackTimeNew(String preCallbackTimeStart) {
        if (!preCallbackTimeStart.contains("T")) {
            return preCallbackTimeStart;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
        try {
            Date time = format.parse(preCallbackTimeStart);
            String date = time.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
            Date datetime = (Date) sdf.parse(date);
            /*datetime = new java.sql.Date(datetime.getTime());*/
            preCallbackTimeStart = new SimpleDateFormat("yyyy-MM-dd").format(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return preCallbackTimeStart;
    }

    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }

    /**
     * yyyy-MM-dd HH:mm:ss转换为yyyy-MM-dd
     *
     * @throws ParseException
     * @author 刘鹏
     */
    public static String transferFormat(String inTime, String old_pattern, String new_pattern) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat(old_pattern);
            Date date = sdf1.parse(inTime);
            SimpleDateFormat sdf2 = new SimpleDateFormat(new_pattern);
            String outTime = sdf2.format(date);
            return outTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * yyyy-MM-dd HH:mm:ss转换为yyyy-MM-dd
     *
     * @throws ParseException
     * @author 刘鹏
     */
    public static String transferFormat(String inTime, String pattern) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
            Date date = sdf1.parse(inTime);
            SimpleDateFormat sdf2 = new SimpleDateFormat(pattern);
            String outTime = sdf2.format(date);
            return outTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long stringToMillis(final String time) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 判断2个时间大小
     * yyyy-MM-dd HH:mm 格式（自己可以修改成想要的时间格式）
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static int timeCompare(String startTime, String endTime, String pattern) {
        int i = 0;
        //注意：传过来的时间格式必须要和这里填入的时间格式相同
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            Date date1 = dateFormat.parse(startTime);//开始时间
            Date date2 = dateFormat.parse(endTime);//结束时间
            // 1 结束时间小于开始时间 2 开始时间与结束时间相同 3 结束时间大于开始时间
            if (date2.getTime() < date1.getTime()) {
                //结束时间小于开始时间
                i = 1;
            } else if (date2.getTime() == date1.getTime()) {
                //开始时间与结束时间相同
                i = 2;
            } else if (date2.getTime() > date1.getTime()) {
                //结束时间大于开始时间
                i = 3;
            }
        } catch (Exception e) {

        }
        return i;
    }

    /**
     * 判断2个时间大小
     * yyyy-MM-dd HH:mm 格式（自己可以修改成想要的时间格式）
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static int timeCompare(Date startTime, Date endTime) {
        int i = 0;
        //注意：传过来的时间格式必须要和这里填入的时间格式相同
        // 1 结束时间小于开始时间 2 开始时间与结束时间相同 3 结束时间大于开始时间
        if (endTime.getTime() < startTime.getTime()) {
            //结束时间小于开始时间
            i = 1;
        } else if (endTime.getTime() == startTime.getTime()) {
            //开始时间与结束时间相同
            i = 2;
        } else if (endTime.getTime() > startTime.getTime()) {
            //结束时间大于开始时间
            i = 3;
        }
        return i;
    }

    /**
     * 格式化时间
     *
     * @param mTime
     * @return
     */
    public static String getTimeRange(String mTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /**获取当前时间*/
        Date curDate = new Date(System.currentTimeMillis());
        String dataStrNew = sdf.format(curDate);
        Date startTime = null;
        try {
            /**将时间转化成Date*/
            curDate = sdf.parse(dataStrNew);
            startTime = sdf.parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        /**除以1000是为了转换成秒*/
        long between = (curDate.getTime() - startTime.getTime()) / 1000;
        int elapsedTime = (int) (between);
        if (elapsedTime < seconds_of_1minute) {

            return "刚刚";
        }
        if (elapsedTime < seconds_of_30minutes) {

            return elapsedTime / seconds_of_1minute + "分钟前";
        }
        if (elapsedTime < seconds_of_1hour) {

            return "半小时前";
        }
        if (elapsedTime < seconds_of_1day) {

            return elapsedTime / seconds_of_1hour + "小时前";
        }
        if (elapsedTime < seconds_of_15days) {

            return elapsedTime / seconds_of_1day + "天前";
        }
        if (elapsedTime < seconds_of_30days) {

            return "半个月前";
        }
        if (elapsedTime < seconds_of_6months) {

            return elapsedTime / seconds_of_30days + "月前";
        }
        if (elapsedTime < seconds_of_1year) {

            return "半年前";
        }
        if (elapsedTime >= seconds_of_1year) {

            return elapsedTime / seconds_of_1year + "年前";
        }
        return mTime;
    }

    public static String stringForTimeMS(long timeS) {
        long seconds = timeS % 60;
        long minutes = timeS / 60 % 60;
        return minutes + ":" + seconds;
    }

    /**
     * 拼团日期差异
     *
     * @param endTime
     */
    public static String groupPurchaseDateDiff(Context context, String endTime) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sd = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数long diff;try {
        // 获得两个时间的毫秒时间差异
        try {
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String startTime = sd.format(curDate);
            long diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
            long day = diff / nd;// 计算差多少天
            long hour = diff % nd / nh;// 计算差多少小时
            long min = diff % nd % nh / nm;// 计算差多少分钟
            long sec = diff % nd % nh % nm / ns;// 计算差多少秒//输出结果
            String s = (hour > 0 ? (hour < 10 ? "0" + hour : hour) : "00") + ":" + (min > 0 ? (min < 10 ? "0" + min : min) : "00") + ":" + (sec > 0 ? (sec < 10 ? "0" + sec : sec) : "00");

            if (day > 0) {
//                return String.format(context.getResources().getString(R.string.group_purchase_end), day + "", s);
            } else {
//                return String.format(context.getResources().getString(R.string.group_purchase_end_hour), s);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    public static String toTimeStr(int secTotal) {

        String result = null;
        secTotal = secTotal / 1000;
        int hour = secTotal / 3600;

        result = String.valueOf(hour);

        return result;
    }


    //限时优惠 。时间差
    public static String groupPurchaseDateDiff1(Context context, String endTime) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sd = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数
        // 获得两个时间的毫秒时间差异
        try {
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String startTime = sd.format(curDate);
            long diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
            long day = diff / nd;// 计算差多少天
            long hour = diff % nd / nh;// 计算差多少小时
            long min = diff % nd % nh / nm;// 计算差多少分钟
            long sec = diff % nd % nh % nm / ns;// 计算差多少秒//输出结果
            //            long times = (day * 24) + hour;
            //            return times > 0 ? times : 0;

            String s = (hour > 0 ? (hour < 10 ? "0" + hour : hour) : "00") + ":" + (min > 0 ? (min < 10 ? "0" + min : min) : "00") + ":" + (sec > 0 ? (sec < 10 ? "0" + sec : sec) : "00");

            if (day > 0) {

//                return String.format(context.getResources().getString(R.string.hours_from_start), day + "", s);
            } else {
//                return String.format(context.getResources().getString(R.string.hours_from_start_hour), s);
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取系统时间戳
     *
     * @return
     */
    public static long getCurTimeLong() {
        long time = System.currentTimeMillis();
        return time;
    }

    /**
     * 获取当前时间
     *
     * @param pattern yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurDate(String pattern) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(new Date());
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param milSecond
     * @param pattern   yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 将字符串转为时间戳
     *
     * @param dateString
     * @param pattern    yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * date转string
     *
     * @param date
     * @param format
     * @return
     */
    public static String getDateTime(Date date, String format) {
        if (date == null) return "";
        String currentTime = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            currentTime = sdf.format(date);
            return currentTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTime;
    }

    /**
     * @param time   时间戳
     * @param format
     * @return
     */
    public static String getDateTime(String time, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            long lt = new Long(time);
            Date date = new Date(lt);
            String res = simpleDateFormat.format(date);
            return res;
        } catch (Exception e) {
            return "";
        }

    }

    public static String getTimedata(String user_time) {
        String re_time = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d;
        try {
            d = sdf.parse(user_time);
            long l = d.getTime();
            String str = String.valueOf(l);
            re_time = str.substring(0, 10);
        } catch (ParseException e) {
            // TODO Auto-generated catch block e.printStackTrace();
        }
        return re_time;
    }

    /**
     * 将某种格式的时间转为时间戳
     *
     * @param timeString
     * @return
     */
    public static String getTime(String timeString, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d;
        try {
            d = sdf.parse(timeString);
            return d.getTime() + "";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 时间戳转date
     *
     * @param strDate
     * @return
     */
    public static Date parse(String strDate) {
        Date date = new Date();
        try {
            Long ll = new Long(strDate);
            date = new Date(ll);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return date;
    }


    /**
     * @param birthday 时间戳
     * @return
     */
    public static String getAge(String birthday, String format) {
        try {
            long lt = new Long(getTime(birthday, format));
            Date date = new Date(lt);

            Calendar cal = Calendar.getInstance();
            if (cal.before(birthday)) { //出生日期晚于当前时间，无法计算
                return "0";
            }
            int yearNow = cal.get(Calendar.YEAR);  //当前年份
            int monthNow = cal.get(Calendar.MONTH);  //当前月份
            int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
            cal.setTime(date);
            int yearBirth = cal.get(Calendar.YEAR);
            int monthBirth = cal.get(Calendar.MONTH);
            int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
            int age = yearNow - yearBirth;   //计算整岁数
            if (monthNow <= monthBirth) {
                if (monthNow == monthBirth) {
                    if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
                } else {
                    age--;//当前月份在生日之前，年龄减一

                }
            }
            return age + "";
        } catch (Exception e) {
            return "";
        }
    }

//    /***
//     * @param timeps 时间戳
//     * @param time   时间戳
//     */
//    public static boolean isSameDay(String timeps, String time) {
//        try {
//            Long ll = new Long(timeps);
//            Date datefirst = new Date(ll);
//            Long ll_second = new Long(time);
//            Date dateSecond = new Date(ll_second);
//            Calendar cal1 = Calendar.getInstance();
//            cal1.setTime(datefirst);
//            Calendar cal2 = Calendar.getInstance();
//            cal2.setTime(dateSecond);
//            return isSameDay(cal1, cal2);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//        return false;
//    }
//
//    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
//        if (cal1 != null && cal2 != null) {
//            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
//        } else {
//            throw new IllegalArgumentException("The date must not be null");
//        }
//    }


    public static String getTime(String time) {
        String result = "";
        if (!TextUtils.isEmpty(time)) {
            String startTime = time.split("T")[0];
            if (!TextUtils.isEmpty(startTime)) {
                String[] timeArr = startTime.split("-");
                result = timeArr[0] + "." + timeArr[1] + "." + timeArr[2];
            }
        }
        return result;
    }


    public static String toTime(String s, long start) {
        long millisecond = start % 1000;
        millisecond /= 100;
        start /= 1000;
        long minute = start / 60;
        long second = start % 60;
        minute %= 60;

        Log.e(s + "==============", String.format("%02d:%02d:%01d", minute, second, millisecond));
        return String.format("%02d:%02d:%01d", minute, second, millisecond);
    }

    public static String stringToDate(String time) throws ParseException {

        if (TextUtils.isEmpty(time)) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(time);
        SimpleDateFormat format1 = new SimpleDateFormat("MM-dd");
        String s = format1.format(date);
        return s;

    }

    public static String stringToDate(String time, String pattern1, String pattern2) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern1);
            Date date = format.parse(time);
            SimpleDateFormat format1 = new SimpleDateFormat(pattern2);
            String s = format1.format(date);
            return s;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";

    }

    // 将时间戳转为字符串
    public static String getStrTime(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));
        return re_StrTime;
    }

    /**
     * 判断两个时间，是否大于5 分钟的工具类
     *
     * @param time1
     * @return
     */
    public static long twoTimeRemaining(String time1, String time2, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);
            return Math.abs(date1.getTime() - date2.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 判断两个时间，是否大于5 分钟的工具类
     *
     * @param time1
     * @param minute
     * @return
     */
    public static boolean isTimeMoreThanMinutes(String time1, String time2, int minute, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);
            long timeDiff = Math.abs(date1.getTime() - date2.getTime());
            return (timeDiff > minute * 60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param time
     * @return
     */
    public static String formatChatTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(time);
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            cal.setTime(date);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.DAY_OF_MONTH) == day) {
                // 时间为今天
                return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            } else {
                // 时间不为今天
                format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                return format.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将UTC时间转换成当前手机时区的时间
     *
     * @param utcTime UTC时间戳，单位为毫秒
     * @param format  时间格式，例如："yyyy-MM-dd HH:mm:ss"
     * @return 当前手机时区的时间字符串
     */

    public static String convertUtcToLocal(String utcTime, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        // 设置时区为UTC
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            // 将UTC时间字符串解析为Date对象
            Date utcDate = dateFormat.parse(utcTime);
            // 获取手机当前时区
            TimeZone localTimeZone = TimeZone.getDefault();
            // 设置SimpleDateFormat的时区为当前手机时区
            dateFormat.setTimeZone(localTimeZone);
            // 将UTC时间格式化为当前手机时区的时间字符串
            return dateFormat.format(utcDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertUtcToLocal(String utcTime, String pattern, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date utcDate = sdf.parse(utcTime);
            // 将时间戳转换为 "HH:mm" 格式的时间
            SimpleDateFormat outputFormat = new SimpleDateFormat(format);
            TimeZone localTimeZone = TimeZone.getDefault();
            outputFormat.setTimeZone(localTimeZone);
            return outputFormat.format(utcDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取当前utc的时间
     *
     * @param format 时间格式，例如："yyyy-MM-dd HH:mm:ss"
     * @return 当前手机时区的时间字符串
     */
    public static String getLocalUtc(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        // 获取当前时间
        Date now = new Date();
// 获取当前时区
        TimeZone timeZone = TimeZone.getDefault();
// 计算当前时区与UTC的时间差（以毫秒为单位）
        int offsetInMillis = timeZone.getOffset(now.getTime());
// 计算UTC时间（以毫秒为单位）
        long utcTimeInMillis = now.getTime() - offsetInMillis;
// 将UTC时间转换为日期对象
        Date utcDate = new Date(utcTimeInMillis);
        return dateFormat.format(utcDate);
    }

    /**
     * 获取时区
     */
    public static String getTimeZone(String id) {
        TimeZone timeZone = TimeZone.getTimeZone(id);
        return timeZone.getDisplayName();
    }

    /**
     * 获取时区
     */
    public static String getTimeZoneId() {
        TimeZone localTimeZone = TimeZone.getDefault();
        return localTimeZone.getID();
    }

    /**
     * 获取时区
     */
    public static TimeZone getTimeZone() {
        TimeZone localTimeZone = TimeZone.getDefault();
        return localTimeZone;
    }

    /**
     * 获取两个时间的差值
     *
     * @param startTime 开始时间的字符串表示，例如"2021-12-01 12:00:00"
     * @param endTime   结束时间的字符串表示，例如"2021-12-02 14:30:20"
     * @return 格式化后的时间差字符串
     */
    public static long getTimeDifference(String startTime, String endTime, String format) {
        if (startTime == null || endTime == null) return 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date startDate = dateFormat.parse(startTime);
            Date endDate = dateFormat.parse(endTime);
            long durationInMillis = endDate.getTime() - startDate.getTime();
            return durationInMillis;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据差值，并返回格式化后的字符串，例如"01:30:20"
     *
     * @param durationInMillis 格式化后的时间差字符串
     */
    public static String getTimeDifferenceString(long durationInMillis) {
        try {
            long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static long convertTimeStringToMillis(String timeString) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = dateFormat.parse(timeString);
            long millis = date.getTime();
            return millis;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 格式化日期字符串。
     * 如果日期与当前年份相同，不显示年份。如果不同，则显示年份。
     *
     * @param dateString         日期字符串
     * @param originalDateFormat 原始的日期字符串格式
     * @return 格式化后的日期字符串
     */
    public static String formatDateString(String dateString, String originalDateFormat, String originalDateFormat1, String originalDateFormat2) {
        // 设置解析器的时区为UTC
        SimpleDateFormat parser = new SimpleDateFormat(originalDateFormat, Locale.getDefault());
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            // 解析得到UTC格式的Date对象
            Date date = parser.parse(dateString);

            // 转换Date对象到系统默认时区
            SimpleDateFormat defaultTimeZoneFormatter = new SimpleDateFormat(originalDateFormat, Locale.getDefault());
            defaultTimeZoneFormatter.setTimeZone(TimeZone.getDefault());
            String defaultTimeZoneDateStr = defaultTimeZoneFormatter.format(date);

            // 重新解析默认时区的日期字符串
            Date defaultTimeZoneDate = defaultTimeZoneFormatter.parse(defaultTimeZoneDateStr);

            Calendar currentCalendar = Calendar.getInstance();
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(defaultTimeZoneDate);

            int currentYear = currentCalendar.get(Calendar.YEAR);
            int dateYear = dateCalendar.get(Calendar.YEAR);

            SimpleDateFormat formatter;
            if (currentYear == dateYear) {
                // 如果年份相同，不显示年份
                formatter = new SimpleDateFormat(originalDateFormat1, Locale.getDefault());
            } else {
                // 如果年份不同，显示年份
                formatter = new SimpleDateFormat(originalDateFormat2, Locale.getDefault());
            }

            // 使用系统默认时区格式化日期
            formatter.setTimeZone(TimeZone.getDefault());
            return formatter.format(defaultTimeZoneDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    //将给定时间转换成UTC时间
    public static String convertToUTC(String dateTime, String dateFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
            Date date = sdf.parse(dateTime);
            // 设置时区为UTC
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            // 将Date对象格式化为UTC时间字符串
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 获取月日，例如：12月25日
    public static String getMonthDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日", Locale.getDefault());
        return sdf.format(date);
    }

    // 获取周几，例如：星期三
    public static String getWeekDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(date);
    }

    // 获取上午或下午的时间，例如：上午 10:30 或 下午 2:45
    public static String getAmPmTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
